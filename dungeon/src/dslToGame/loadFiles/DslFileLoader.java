package dslToGame.loadFiles;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides functions for loading DSL files.
 *
 * <p>Use {@link #processArguments(String[])} to read in all DSL files in the given paths.
 * Basically, use this function to parse the command line arguments and extract all DSL file paths.
 */
public class DslFileLoader {

    private static final String DSL_FILE_ENDING = ".dng";
    private static final String JAR_FILE_ENDING = ".jar";
    private static final String SCRIPT_FOLDER = "scripts/";

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
        Set<Path> foundPaths = new HashSet<>();

        for (String arg : args) {
            Path path = Paths.get(arg);

            if (Files.exists(path)) {
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(JAR_FILE_ENDING)) {
                    Set<Path> jarPaths = findDSLFilesInJar(arg);
                    foundPaths.addAll(jarPaths);
                } else if (fileName.endsWith(DSL_FILE_ENDING)) {
                    foundPaths.add(path);
                }
            }
        }

        return foundPaths;
    }

    private static Set<Path> findDSLFilesInJar(String jarPath) throws IOException {
        Set<Path> dngPaths = new HashSet<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(SCRIPT_FOLDER) && entryName.endsWith(DSL_FILE_ENDING)) {
                    Path entryPath = Paths.get(jarPath + File.separator + entryName);
                    dngPaths.add(entryPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dngPaths;
    }

    /**
     * Read in the given file as string.
     *
     * @param file file to read in.
     * @return read in string.
     */
    public static String fileToString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
