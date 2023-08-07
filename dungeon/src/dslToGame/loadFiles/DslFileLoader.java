package dslToGame.loadFiles;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides functions for loading DSL files.
 *
 * <p>Use {@link #processArguments(String[])} to read in all DSL files in the given paths.
 * Basically, use this function to parse the command line arguments and extract all DSL file paths.
 */
public class DslFileLoader {

    private static final String DSL_FILE_ENDING = "dng";
    private static final String JAR_FILE_ENDING = "jar";
    private static final String SCRIPT_FOLDER = "/scripts";

    /**
     * Load DSL files from the given paths.
     *
     * <p>This function will try to parse each given String to a Path and then check if it's a DSL
     * file or a jar.
     *
     * <p>If it's a jar, it will load each DSL file from inside the jar's "/script" directory.
     *
     * @param args Strings that could be paths, basically use the command line arguments.
     * @return Set containing all paths to DSL files.
     * @throws IOException if an I/O error occurs while reading the files.
     */
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

    private static Set<Path> findDSLFilesInJar(Path jarPath) throws IOException {
        Set<Path> dngFiles = new HashSet<>();
        ClassLoader classLoader = DslFileLoader.class.getClassLoader();
        try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarPath, classLoader)) {
            Path scriptsFolderPath = jarFileSystem.getPath(SCRIPT_FOLDER);

            if (Files.exists(scriptsFolderPath) && Files.isDirectory(scriptsFolderPath)) {
                Files.walkFileTree(
                        scriptsFolderPath,
                        new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                if (file.toString().toLowerCase().endsWith(DSL_FILE_ENDING)) {
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

    /**
     * Check if the given Path is a File with the specified file ending.
     *
     * @param path The Path to check.
     * @param ending The expected file ending.
     * @return true if the file ends with the given ending (is of that type), otherwise false.
     */
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
