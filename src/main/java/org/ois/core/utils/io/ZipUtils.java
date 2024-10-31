package org.ois.core.utils.io;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for creating ZIP archives.
 * Provides methods to zip files and directories, with options for entry name filtering.
 */
public class ZipUtils {

    /**
     * Interface for converting a file or directory to a ZIP entry name.
     */
    public interface ZipEntryConvertor {
        /**
         * Gets the entry name for a given file or directory if it is not filtered.
         *
         * @param item the file or directory to convert
         * @return the entry name, or null if the item should be filtered
         */
        String getEntryNameIfNotFiltered(File item);
    }

    /**
     * Zips the specified items into a ZIP archive at the target path.
     * Uses the file name as the entry name.
     *
     * @param archiveTargetPath the path where the ZIP archive will be created
     * @param itemsToZip the paths of the items to be zipped
     * @throws IOException if an I/O error occurs during zipping
     */
    public static void zipItems(Path archiveTargetPath, Path... itemsToZip) throws IOException {
        zipItems(archiveTargetPath, File::getName,itemsToZip);
    }

    /**
     * Zips the specified items into a ZIP archive at the target path, allowing for custom entry name conversion.
     *
     * @param archiveTargetPath the path where the ZIP archive will be created
     * @param itemConvertor the converter used to determine entry names for the items
     * @param itemsToZip the paths of the items to be zipped
     * @throws IOException if an I/O error occurs during zipping
     */
    public static void zipItems(Path archiveTargetPath, ZipEntryConvertor itemConvertor, Path... itemsToZip) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(archiveTargetPath.toString());
            ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            Set<String> addedEntries = new HashSet<>();

            for (Path itemPath : itemsToZip) {
                File item = itemPath.toFile();
                if (item.exists()) {
                    if (item.isDirectory()) {
                        zipDirectory(item, itemConvertor, itemConvertor.getEntryNameIfNotFiltered(item), zipOut, buffer, addedEntries);
                    } else {
                        zipFile(item, itemConvertor, "", zipOut, buffer, addedEntries);
                    }
                }
            }
        }
    }

    /**
     * Recursively zips a directory and its contents.
     *
     * @param dir the directory to zip
     * @param itemConvertor the converter used to determine entry names for the items
     * @param baseName the base name for entries within the directory
     * @param zipOut the ZIP output stream to write to
     * @param buffer a byte array used for reading file data
     * @param addedEntries a set of already added entries to prevent duplicates
     * @throws IOException if an I/O error occurs during zipping
     */
    private static void zipDirectory(File dir, ZipEntryConvertor itemConvertor, String baseName, ZipOutputStream zipOut, byte[] buffer, Set<String> addedEntries) throws IOException {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String dirConvertedName = itemConvertor.getEntryNameIfNotFiltered(file);
                    if (dirConvertedName == null || dirConvertedName.isBlank()) {
                        // Filtered
                        continue;
                    }
                    zipDirectory(file, itemConvertor, baseName + "/" + dirConvertedName, zipOut, buffer, addedEntries);
                } else {
                    zipFile(file, itemConvertor, baseName, zipOut, buffer, addedEntries);
                }
            }
        }
    }

    /**
     * Zips a single file.
     *
     * @param file the file to zip
     * @param itemConvertor the converter used to determine the entry name for the file
     * @param baseName the base name for the entry
     * @param zipOut the ZIP output stream to write to
     * @param buffer a byte array used for reading file data
     * @param addedEntries a set of already added entries to prevent duplicates
     * @throws IOException if an I/O error occurs during zipping
     */
    private static void zipFile(File file, ZipEntryConvertor itemConvertor, String baseName, ZipOutputStream zipOut, byte[] buffer, Set<String> addedEntries)
            throws IOException {
        String fileConvertedName = itemConvertor.getEntryNameIfNotFiltered(file);
        if (fileConvertedName == null || fileConvertedName.isBlank()) {
            // Filter
            return;
        }
        String entryName = baseName.isBlank() ? fileConvertedName : baseName + "/" + fileConvertedName;
        if (addedEntries.contains(entryName)) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zipOut.putNextEntry(zipEntry);

            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }
            addedEntries.add(entryName);
        }
    }
}
