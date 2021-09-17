package sec.assignment.app.controller;

import sec.assignment.app.model.ComparisonResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;


/**
 * Logs files for each comparison done.
 */
public class FileLogger {

    private BlockingQueue<ComparisonResult> results  = new LinkedBlockingQueue<>(1000);
//private BlockingQueue<ComparisonResult> results  = new SynchronousQueue<>();
    private final String FILE_NAME = "results.csv";

    private ExecutorService ioPool;

    public FileLogger(ExecutorService pool) {

        this.ioPool = pool;

        // ioPool executes
        ioPool.execute(()->{
            while (true) {
                try {
                    FileWriter writer = new FileWriter(FILE_NAME,true);
                    ComparisonResult compareResult = results.take();

                    String fileOne = compareResult.getFile1()   ;
                    String fileTwo = compareResult.getFile2();
                    double similarity = compareResult.getSimilarity();

                    writer.write(fileOne + "\t\t\t" + fileTwo + "\t\t\t" + similarity + "\n");
                    writer.flush();

                } catch (IOException e) {
                    System.out.println("IO Fail: " + e.getMessage());
                }catch (InterruptedException e2) {
                    System.out.println("Failed taking from result queue: " + e2.getMessage());
                }
            }

        });
    }


    public void putResult(ComparisonResult result) throws LoggerException {
        try {
            this.results.put(result);
        } catch (InterruptedException e) {
//            throw new InterruptedException("Failed adding result in file logger + \nCause:" + e.getMessage() + "\n");
            throw new LoggerException("Failed adding result in file logger + \nCause:" + e.getMessage() + "\n");
        } catch (NullPointerException e) {
            throw new LoggerException("Element cannot be null when putting into the queue");
        }
    }


}
