package org.ois.core.utils.io;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class FileUtils {
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

    public static boolean deleteDirectoryContent(Path directoryPath) {
        File directory = directoryPath.toFile();
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }
        return deleteContent(directory);
    }

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

    public static boolean createFileIfNotExists(Path path) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            return false;
        }
        return file.createNewFile();
    }
}
