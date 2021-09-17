package sec.assignment.app.controller;

import sec.assignment.app.model.ComparisonResult;
import sec.assignment.app.view.FileCompareApp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class responsible for comparing files, updating UI and writing comparison to file.
 */
public class FileComparison {

//    private ExecutorService cpuPool;
    private List<File> filesList;
    private FileCompareApp ui;
    private LCSComparison comparator;
    private FileLogger logger;
//    private int counter = 0; // Initialise counter as zero
    private AtomicInteger counter = new AtomicInteger(0);

    public FileComparison(
            List<File> filesList,
            FileCompareApp fileCompareApp,
//            ExecutorService cpuBound,
            LCSComparison lcsComparison, FileLogger fileLogger)
    {
        this.filesList = filesList;
        this.ui = fileCompareApp;
//        this.cpuPool = cpuBound;
        this.comparator = lcsComparison;
        this.logger = fileLogger;
    }


    /**
     * Compares a list of files, performs UI update and file I/O
     */
    public void compareFiles() {
        int size = filesList.size();

        for (int ii = 0; ii < size; ii++) {
            for (int j = ii+1; j < size; j++) {

                File one = filesList.get(ii);
                File two = filesList.get(j);

                char[] fOne;
                char[] fTwo;

                double similarity;

                try {
                    String fOneContent = readFileToString(one.getPath());
                    String fTwoContent = readFileToString(two.getPath());

                    fOne = fOneContent.toCharArray();
                    fTwo = fTwoContent.toCharArray();

                    similarity = comparator.calSimilarity(fOne, fTwo);

                    ComparisonResult result;
                    result = new ComparisonResult(one.toString(), two.toString(), similarity);

                    if (similarity > 0.5) {
                        ui.setResultToScreen(result);
                    }

                    logger.putResult(result);

                } catch (IOException e ) {
                    System.out.println("IO Error: " + e.getMessage());
                }
                catch (LoggerException e) {
                    System.out.println(e.getMessage());
                }

                counter.incrementAndGet(); // completed on comparison fully
                ui.setProgress(
                        ((double)counter.get()) / filesList.size()
                );
            }
        }
    }

    /**
     * Converts a file into a string.
     * @param path File path
     * @return file content as a string
     * @throws IOException I/O error
     */
    private String readFileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
