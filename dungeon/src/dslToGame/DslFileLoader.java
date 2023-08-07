package dslToGame;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DslFileLoader {

    private static final String DSL_FILE_ENDING = "dng";
    private static final String JAR_FILE_ENDING = "jar";
    private static final String SCRIPT_FOLDER = "scripts";

    public static Set<Path> processArguments(String[] args) {
        Set<Path> paths = new HashSet<>();
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (DslFileLoader.is(path, JAR_FILE_ENDING)) {
                // get skripts out of jar
            } else if (DslFileLoader.is(path, DSL_FILE_ENDING)) paths.add(path);
        }
        return paths;
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
