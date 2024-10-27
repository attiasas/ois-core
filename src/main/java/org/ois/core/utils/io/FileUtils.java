package org.ois.core.utils.io;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for file and directory operations.
 * Provides methods to copy files, create directories, delete contents, and handle files more effectively.
 */
public class FileUtils {
    /**
     * Copies a file from an InputStream to a target path.
     *
     * @param src the InputStream of the source file
     * @param target the target path to copy the file to
     * @param failIfCantCreate whether to throw an exception if the file cannot be created
     * @return true if the file was copied successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public static boolean copyFile(InputStream src, Path target, boolean failIfCantCreate) throws IOException {
        try {
            Files.copy(src, target);
            return true;
        } catch (Exception e) {
            if (failIfCantCreate){
                throw e;
            }
            return false;
        }
    }

    /**
     * Copies a file from one path to another.
     *
     * @param src the path of the source file
     * @param target the target path to copy the file to
     * @param failIfCantCreate whether to throw an exception if the file cannot be created
     * @return true if the file was copied successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public static boolean copyFile(Path src, Path target, boolean failIfCantCreate) throws IOException {
        try {
            Files.copy(src, target);
            return true;
        } catch (Exception e) {
            if (failIfCantCreate){
                throw e;
            }
            return false;
        }
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param dirPath the path of the directory to create
     * @param failIfCantCreate whether to throw an exception if the directory cannot be created
     * @return true if the directory was created, false if it already exists
     */
    public static boolean createDirIfNotExists(Path dirPath, boolean failIfCantCreate) {
        File dir = dirPath.toFile();
        if (dir.exists()) {
            return false;
        }
        boolean created = dir.mkdir();
        if (!created && failIfCantCreate) {
            throw new RuntimeException("Can't create directory at " + dirPath);
        }
        return created;
    }

    /**
     * Copies all files and subdirectories from the source path to the target path, excluding specified patterns.
     *
     * @param sourcePath the path to copy from
     * @param targetPath the path to copy to
     * @param excludePatterns the patterns of files/directories to exclude from copying
     * @throws IOException if an I/O error occurs
     */
    public static void copyDirectoryContent(Path sourcePath, Path targetPath, String... excludePatterns) throws IOException {
        // Copy all files and subdirectories from source to target
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(sourcePath, options, Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            private final List<PathMatcher> exclusions = Arrays.stream(excludePatterns).map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern)).collect(Collectors.toList());
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                for (PathMatcher matcher : this.exclusions) {
                    if (matcher.matches(dir) && !sourcePath.equals(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Deletes all content within the specified directory.
     *
     * @param directoryPath the path of the directory to clear
     * @return true if the content was deleted successfully, false if the directory does not exist
     */
    public static boolean deleteDirectoryContent(Path directoryPath) {
        File directory = directoryPath.toFile();
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }
        return deleteContent(directory);
    }

    /**
     * Deletes the contents of a directory recursively.
     *
     * @param directory the directory to delete content from
     * @return true if all contents were deleted successfully, false otherwise
     */
    private static boolean deleteContent(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }
        boolean result = true;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContent(file);
            }
            result &= file.delete();
        }
        return result;
    }

    /**
     * Creates a file if it does not already exist.
     *
     * @param path the path of the file to create
     * @return true if the file was created, false if it already exists
     * @throws IOException if an I/O error occurs
     */
    public static boolean createFileIfNotExists(Path path) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            return false;
        }
        return file.createNewFile();
    }
}
