package com.example.xshare.logic.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCreator {

    public static void zipFolder(Path sourceFolderPath) throws IOException {
        // Get the folder's parent directory and file name (for ZIP name)
        Path parentDir = sourceFolderPath.getParent();
        String zipFileName = sourceFolderPath.getFileName().toString() + ".zip";

        // Create the ZIP file path
        Path zipFilePath = parentDir.resolve(zipFileName);

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            // Walk through the directory and add each file to the ZIP output stream
            Files.walk(sourceFolderPath).forEach(path -> {
                // Define the ZIP entry
                ZipEntry zipEntry = new ZipEntry(sourceFolderPath.relativize(path).toString());
                try {
                    if (Files.isDirectory(path)) {
                        // Add directory entry
                        if (!zipEntry.getName().endsWith("/")) {
                            zipEntry = new ZipEntry(zipEntry.getName() + "/");
                        }
                        zipOut.putNextEntry(zipEntry);
                        zipOut.closeEntry();
                    } else {
                        // Add file entry
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    }
                } catch (IOException e) {
                    System.err.println("Error zipping file: " + path + " - " + e);
                }
            });
        }

        System.out.println("Folder successfully zipped to: " + zipFilePath.toString());
    }
}
