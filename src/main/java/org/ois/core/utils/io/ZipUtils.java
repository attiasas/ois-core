package org.ois.core.utils.io;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public interface ZipEntryConvertor {
        String getEntryNameIfNotFiltered(File item);
    }

    public static void zipItems(Path archiveTargetPath, Path... itemsToZip) throws IOException {
        zipItems(archiveTargetPath, File::getName,itemsToZip);
    }

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
