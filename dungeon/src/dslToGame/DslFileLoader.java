package dslToGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DslFileLoader {

    private static final String DSL_FILE_ENDING = "dng";
    private static final String SCRIPT_FOLDER = "scripts";

    public static Set<File> dslFiles() {
        ClassLoader classLoader = DslFileLoader.class.getClassLoader();

        File directory = new File(classLoader.getResource(SCRIPT_FOLDER).getFile());
        Set<File> files = new HashSet<>();

        // Recursively find files with the specified file ending
        findFilesWithEnding(directory, files);

        return files;
    }

    private static void findFilesWithEnding(File currentDirectory, Set<File> files) {
        if (currentDirectory.exists() && currentDirectory.isDirectory()) {
            // Get all files in the current directory
            File[] allFiles = currentDirectory.listFiles();

            if (allFiles != null) {
                // Iterate through the files
                for (File file : allFiles) {
                    if (file.isFile() && file.getName().endsWith(DSL_FILE_ENDING)) {
                        files.add(file);
                    } else if (file.isDirectory()) {
                        findFilesWithEnding(file, files);
                    }
                }
            }
        }
    }

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
