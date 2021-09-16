package sec.assignment.app.controller;

import sec.assignment.app.model.ComparisonResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileLogger {
    // TODO: Implement and use a blocking queue here

    private BlockingQueue<ComparisonResult> results  = new LinkedBlockingQueue<>(100);
    private final String FILE_NAME = "results.csv";

    public FileLogger() {

        // Own thread responsible for handling writing to prevent race condition.
        new Thread(()-> {

//            Path path = Paths.get(FILE_NAME) {
//                if (Files.exists(path)) {
//                    File delete = path.toFile();
//                    delete.delete();
//                }
//            }

            while (true) {
                try {
                    FileWriter writer = new FileWriter(FILE_NAME,true);
                    ComparisonResult compareResult = results.take();

                    String fileOne = compareResult.getFile1();
                    String fileTwo = compareResult.getFile2();
                    double similarity = compareResult.getSimilarity();

                    writer.write(fileOne + "\t" + fileTwo + "\t" + similarity);

                } catch (IOException | InterruptedException e) {
                    // TODO: Handle appropriate error later
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void putResult(ComparisonResult result) throws InterruptedException {
        try {
            this.results.put(result);
        } catch (InterruptedException e) {
            throw new InterruptedException("Failed adding result in file logger + \nCause:" + e.getMessage() + "\n");
        }
    }


}
