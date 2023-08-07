package dslToGame.loadFiles;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class DslFileLoader {

    private static final String DSL_FILE_ENDING = "dng";
    private static final String JAR_FILE_ENDING = "jar";
    private static final String SCRIPT_FOLDER = "/scripts";

    public static Set<Path> processArguments(String[] args) throws IOException {
        Set<Path> paths = new HashSet<>();
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (DslFileLoader.is(path, JAR_FILE_ENDING)) {
                paths.addAll(findDSLFilesInJar(path));
            } else if (DslFileLoader.is(path, DSL_FILE_ENDING)) paths.add(path);
        }
        return paths;
    }

    public static Set<Path> findDSLFilesInJar(Path jarPath) throws IOException {
        Set<Path> dngFiles = new HashSet<>();
        ClassLoader classLoader = DslFileLoader.class.getClassLoader();
        try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarPath, classLoader)) {
            Path scriptsFolderPath = jarFileSystem.getPath(SCRIPT_FOLDER);

            if (Files.exists(scriptsFolderPath) && Files.isDirectory(scriptsFolderPath)) {
                Files.walkFileTree(
                        scriptsFolderPath,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                if (file.toString().toLowerCase().endsWith(".dng")) {
                                    dngFiles.add(file);
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                                // Handle errors if necessary
                                return FileVisitResult.CONTINUE;
                            }
                        });
            }
        }

        return dngFiles;
    }

    private static boolean is(Path path, String ending) {
        if (Files.exists(path))
            if (Files.isRegularFile(path)) {
                String fileExtension = getFileExtension(path);
                if (fileExtension != null) return fileExtension.equalsIgnoreCase(ending);
            }
        return false;
    }

    private static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return null;
    }
}
