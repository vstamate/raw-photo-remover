package com.valentinstamate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Main {

    private static final Set<String> RAW_FORMATS = Set.of(".raw", ".arw");

    public static void main(String[] args) throws Exception {
        String currentDir = Path.of("").toAbsolutePath().toString();

        IO.println("Searching for files in " + currentDir + "...");

        try (Stream<Path> stream = Files.list(Path.of("."))) {
            Map<String, Set<String>> filesMap = new HashMap<>();

            List<Path> files = stream
                    .filter(p -> !Files.isDirectory(p))
                    .toList();

            files.forEach(path -> {
                String filename = path.getFileName().toString().substring(2);
                String key = extractFilename(filename);

                filesMap.computeIfAbsent(key, _ -> new HashSet<>())
                        .add(filename);
            });

            Set<String> filesToDelete = filesMap.values().stream()
                    .map(mappedFiles -> {
                        Set<String> rawFiles = mappedFiles.stream()
                                .filter(filename -> {
                                    String extension = extractExtension(filename);
                                    return RAW_FORMATS.contains(extension.toLowerCase());
                                })
                                .collect(Collectors.toSet());

                        Set<String> compressedFiles = new HashSet<>(mappedFiles);
                        compressedFiles.removeAll(rawFiles);

                        if (compressedFiles.isEmpty()) {
                            return rawFiles;
                        }

                        return Set.<String>of();
                    })
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            if (!filesToDelete.isEmpty()) {
                IO.println("Found the following files to delete:");
                filesToDelete.forEach(IO::println);
            } else {
                IO.println("No file to delete found.");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            IO.println("Do you want to continue? (y/n)");
            String input = scanner.nextLine().trim();

            if (!input.equals("y")) {
                return;
            }

            Set<String> filePathsToDelete = filesToDelete.stream()
                    .map(filename -> String.format("%s\\%s", currentDir, filename))
                    .collect(Collectors.toSet());

            for (String filePath : filePathsToDelete) {
                IO.print("Deleting " + filePath);
                boolean deleted = Files.deleteIfExists(Path.of(filePath));
                IO.println(" " + deleted);
            }
        }
    }

    private static String extractFilename(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0
                ? filename.substring(0, dotIndex)
                : filename;
    }

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0
                ? filename.substring(dotIndex)
                : "";
    }
}
