package sec.assignment.app.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Responsible for finding non-empty  files in a directory specified.
 */
public class FileFinder {

    public FileFinder() {
    }

    /**
     * Finds and adds non-empty files.
     *
     * @param filePath the file path
     * @param nonEmptyFiles list to add non-empty files to
     */
    public void findNonEmptyFiles(Path filePath, List<File> nonEmptyFiles)  {
        try
        {
            Files.walkFileTree(Paths.get(filePath.toString()), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                {
                    // Check whether file is not empty and add to list

                    File visitedFile = file.toFile();
                    if(visitedFile.length() > 0 ) { // Valid
                        nonEmptyFiles.add(visitedFile); // Add to non-empty list of files
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
