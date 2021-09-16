package sec.assignment.app.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sec.assignment.app.controller.FileComparer;
import sec.assignment.app.controller.FileFinder;
import sec.assignment.app.controller.FileLogger;
import sec.assignment.app.model.ComparisonResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCompareApp
{
    private TableView<ComparisonResult> resultTable = new TableView<>();
    private ProgressBar progressBar = new ProgressBar();

    private final int NUM_THREADS_AVAILABLE = (Runtime.getRuntime().availableProcessors() / 2 );

    /** Thread pool */
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS_AVAILABLE);
//    private Set<Files> files = new HashSet<>();

//    /** Blocking queue*/
//    private BlockingQueue<>


    private FileComparer fileComparer = new FileComparer(/*executor*/);
    private FileFinder fileFinder = new FileFinder(executor);
    private FileLogger fileLogger = new FileLogger();


    // TODO: Create new executor service when shutdown later in appropriate place

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
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("."));
        dc.setTitle("Choose directory");
        File directory = dc.showDialog(stage);

        System.out.println("Comparing files within " + directory + "...");

        // Extremely fake way of demonstrating how to use the progress bar (noting that it can
        // actually only be set to one value, from 0-1, at a time.)
        progressBar.setProgress(0.25);
        progressBar.setProgress(0.5);
        progressBar.setProgress(0.6);
        progressBar.setProgress(0.85);
        progressBar.setProgress(1.0);

        // Extremely fake way of demonstrating how to update the table (noting that this shouldn't
        // just happen once at the end, but progressively as each result is obtained.)
        List<ComparisonResult> newResults = new ArrayList<>();
        newResults.add(new ComparisonResult("Example File 1", "Example File 2", 0.75));
        newResults.add(new ComparisonResult("Example File 1", "Example File 3", 0.31));
        newResults.add(new ComparisonResult("Example File 2", "Example File 3", 0.45));

        resultTable.getItems().setAll(newResults);

        resultTable.getItems().clear();

        List<File> filesFound = Collections.synchronizedList(new ArrayList<>());


        CompletableFuture
                .runAsync(()-> {
//                    executor.execute(()-> {
                        fileFinder.findNonEmptyFiles(directory.toPath(), filesFound);

                        System.out.println("Reached finding non empty files");
//                    });

                })
                .thenRun(()-> {
//                    System.out.println(filesFound);
                    // TODO: Do compare here
                    //TODO: Cater for size 0 or 1

                    System.out.println("Reached .thenRun");
                    int size = filesFound.size();
                    System.out.println("Printing size: " + size);

//                    for (int ii = 0; ii < size; ii++) {
//                        for (int j = ii+1; j < size; j++) {
//
//                        }
//                    }


                    for(int ii=0; ii<size; ii++) {
                        for (int j=ii+1;j<size;j++) {

                            int compareOne = ii;
                            int compareTwo = j;
                            executor.execute(()-> {
                                File file1 = filesFound.get(compareOne);
                                File file2 = filesFound.get(compareTwo);
                                char[] charArrOne = null;
                                char[] charArrTwo = null;
                                double similarity;

                                try {
                                    String fileOneContent = Files.readString(file1.toPath(), StandardCharsets.UTF_8);
                                    System.out.println(fileOneContent);
                                    charArrOne = fileOneContent.toCharArray();


                                    String fileTwoContent = Files.readString(file2.toPath(), StandardCharsets.UTF_8);
                                    charArrTwo = fileTwoContent.toCharArray();

                                    similarity = fileComparer.calSimilarity(charArrOne, charArrTwo);

                                    ComparisonResult result = new ComparisonResult(
                                            file1.toString(),
                                            file2.toString(),
                                            similarity
                                    );

                                    if(similarity > 0.5) {
                                        this.setResultToScreen(result);
                                    }

                                    fileLogger.putResult(result);
                                } catch (IOException | InterruptedException e) {
                                    // TODO: Handle error later
                                    e.printStackTrace();
                                }
//
                            });
                        }
                    }

                });


//                .exceptionally();

//         progressBar.setProgress(0.0); // Reset progress bar after successful comparison


    } // END OF crossCompare

    private void stopComparison()
    {
        // TODO: Implement this feature
        System.out.println("Stopping comparison...");

//        executor = Executors.newFixedThreadPool(NUM_THREADS_AVAILABLE);
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
