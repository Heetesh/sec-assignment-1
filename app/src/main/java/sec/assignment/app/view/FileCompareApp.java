package sec.assignment.app.view;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sec.assignment.app.controller.FileComparison;
import sec.assignment.app.controller.LCSComparison;
import sec.assignment.app.controller.FileFinder;
import sec.assignment.app.controller.FileLogger;
import sec.assignment.app.model.ComparisonResult;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class FileCompareApp
{
    private TableView<ComparisonResult> resultTable = new TableView<>();
    private ProgressBar progressBar = new ProgressBar();

    // Leaving some threads to the poor OS
    private final int NUM_THREADS_AVAILABLE = (int) (Runtime.getRuntime().availableProcessors() / 1.5);

    /** Thread pool */
//    private ExecutorService ioPool = Executors.newFixedThreadPool(1);
    private ExecutorService ioPool = Executors.newSingleThreadExecutor();
    private ExecutorService cpuBound = findAndSetThreads();

//    private boolean running = false;

//    private int progressCounter = 0;
//    private Set<Files> files = new HashSet<>();

//    /** Blocking queue*/
//    private BlockingQueue<>

    public FileCompareApp(){
        findAndSetThreads();
    }

    private ExecutorService findAndSetThreads() {
        int availThreads = (int) (Runtime.getRuntime().availableProcessors() / 1.5);
        if (availThreads < 1) {
            return cpuBound = Executors.newSingleThreadExecutor();
        } else {
            return cpuBound = Executors.newFixedThreadPool(availThreads);
        }
    }


    //    private LCSComparison fileComparer = new LCSComparison(/*executor*/);
    private FileFinder fileFinder = new FileFinder();
    private FileLogger fileLogger = new FileLogger(ioPool);


    public void start(Stage stage)
    {
        stage.setTitle("File Compare-R");
        stage.setMinWidth(600);

        // Create toolbar
        Button compareBtn = new Button("Compare...");
        Button stopBtn = new Button("Stop");
        ToolBar toolBar = new ToolBar(compareBtn, stopBtn);

        // Set up button event handlers.
        compareBtn.setOnAction(event -> crossCompare(stage));
        stopBtn.setOnAction(event -> stopComparison());

        // Initialise progressbar
        progressBar.setProgress(0.0);

        TableColumn<ComparisonResult,String> file1Col = new TableColumn<>("File 1");
        TableColumn<ComparisonResult,String> file2Col = new TableColumn<>("File 2");
        TableColumn<ComparisonResult,String> similarityCol = new TableColumn<>("Similarity");

        // The following tell JavaFX how to extract information from a ComparisonResult
        // object and put it into the three table columns.
        file1Col.setCellValueFactory(
                (cell) -> new SimpleStringProperty(cell.getValue().getFile1()) );

        file2Col.setCellValueFactory(
                (cell) -> new SimpleStringProperty(cell.getValue().getFile2()) );

        similarityCol.setCellValueFactory(
                (cell) -> new SimpleStringProperty(
                        String.format("%.1f%%", cell.getValue().getSimilarity() * 100.0)) );

        // Set and adjust table column widths.
        file1Col.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.40));
        file2Col.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.40));
        similarityCol.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.20));

        // Add the columns to the table.
        resultTable.getColumns().add(file1Col);
        resultTable.getColumns().add(file2Col);
        resultTable.getColumns().add(similarityCol);

        // Add the main parts of the UI to the window.
        BorderPane mainBox = new BorderPane();
        mainBox.setTop(toolBar);
        mainBox.setCenter(resultTable);
        mainBox.setBottom(progressBar);
        Scene scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private void crossCompare(Stage stage)
    {

        resultTable.getItems().clear(); // Clear the table
//        this.progressBar.setProgress(0.0);

        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("."));
        dc.setTitle("Choose directory");
        File directory = dc.showDialog(stage);

        System.out.println("Comparing files within " + directory + "...");


        List<File> filesFound = Collections.synchronizedList(new ArrayList<>());

//        Callable<List<File>> filesRead;

        CompletableFuture
                // First we find all non-empty files
                .runAsync(()-> {
                    fileFinder.findNonEmptyFiles(directory.toPath(), filesFound);

                }, cpuBound) // Use executor pool to run this and the following chain task

                // Then we compare and do necessary tasks
                .thenRunAsync(()-> {

                    FileComparison comparison = new FileComparison(
                            filesFound,
                            this,
//                            cpuBound,
                            new LCSComparison(),
                            fileLogger
                    );

                    comparison.compareFiles();
                },cpuBound)
                .thenRunAsync(()-> {
                    System.out.println("Finished all chains");
                },cpuBound);// END OF thenRun

         progressBar.setProgress(0.0); // Reset progress bar after successful comparison
    } // END OF crossCompare

    private void stopComparison()
    {
        System.out.println("Stopping comparison...");

        this.resultTable.getItems().clear();

        ioPool.shutdown();
        cpuBound.shutdown();

//        progressCounter = 0; // Reset progress

        ioPool = Executors.newFixedThreadPool(1);
        cpuBound = findAndSetThreads();

        progressBar.setProgress(0.0);

    }

    /** Sets the progress bar progress
     * @param progress the progress
     * */
    public void setProgress(double progress) {
        Platform.runLater(()-> this.progressBar.setProgress(progress));
    }

    public void setResultToScreen(ComparisonResult result) {
        Platform.runLater(()-> { // Platform run later because it will be accessed by other threads
            this.resultTable.getItems().add(result);
        });
    }
}
