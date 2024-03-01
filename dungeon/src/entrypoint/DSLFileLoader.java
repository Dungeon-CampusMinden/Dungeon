package entrypoint;

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
 * <p>Use {@link #processArguments(String[])} to read all DSL files in the given paths. Basically,
 * use this function to parse the command line arguments and extract all DSL file paths.
 *
 * <p>Note: Always use "/" as the file separator; do not use the typical Windows "\\" file
 * separator.
 */
public class DSLFileLoader {

  private static final String DSL_FILE_ENDING = ".dng";
  private static final String JAR_FILE_ENDING = ".jar";
  private static final String SCRIPT_FOLDER = "scripts/";

  /**
   * Load DSL files from the given paths.
   *
   * <p>This function attempts to parse each given String into a {@link Path} and then checks
   * whether it represents a DSL file or a jar.
   *
   * <p>If it's a DSL file, the function adds the file's path to the return set.
   *
   * <p>If it's a jar, it loads each DSL file from the "/script" directory within the jar.
   *
   * <p>Non-DSL files will be ignored.
   *
   * <p>Note: Always use "/" as the file separator; do not use the typical Windows "\\" file
   * separator.
   *
   * @param args Strings that could be paths; typically, these are the command line arguments.
   * @return Set containing all paths to DSL files.
   */
  public static Set<Path> processArguments(String[] args) {
    Set<Path> foundPaths = new HashSet<>();
    for (String arg : args) {
      Path path = Paths.get(arg);
      if (Files.exists(path)) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(JAR_FILE_ENDING)) {
          Set<Path> jarPaths = findDSLFilesInJar(arg);
          foundPaths.addAll(jarPaths);
        } else if (fileName.endsWith(DSL_FILE_ENDING)) {
          foundPaths.add(path.toAbsolutePath());
        }
      }
    }

    return foundPaths;
  }

  /**
   * Search for files in the "/script" directory of the given jar file.
   *
   * @param jarPath Path to the jar file.
   * @return Collection of Path objects representing all DSL files in the ".jar/scripts" directory.
   */
  private static Set<Path> findDSLFilesInJar(String jarPath) {
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
      e.printStackTrace(); // WTF?
    }

    return dngPaths;
  }

  /**
   * Reads the content of a file specified by the given path.
   *
   * <p>If the path points to a JAR file, it reads its content using {@link
   * #fileToStringFromJar(Path)}, else its using {@link #fileToString(File)}.
   *
   * @param path Path to file to read.
   * @return Read-in string.
   */
  public static String fileToString(Path path) {
    if (path.toString().contains(JAR_FILE_ENDING)) return fileToStringFromJar(path);
    else return fileToString(path.toFile());
  }

  /**
   * Read the given file as a string.
   *
   * <p>Note: This only works if the program is running in the IDE, not in the jar.
   *
   * @param file File to read.
   * @return Read-in string.
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

  /**
   * Read the dng files in the given JAR file as a string.
   *
   * @param path Path to JAR file.
   * @return Read-in string.
   */
  public static String fileToStringFromJar(Path path) {

    String jarFilePath =
        path.toString().split(JAR_FILE_ENDING)[0] + JAR_FILE_ENDING.replace("\\", "/");
    String jarFileContent =
        path.toString().split(JAR_FILE_ENDING)[1].replace("\\", "/").substring(1);

    try (JarFile jarFile = new JarFile(jarFilePath)) {
      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String entryName = entry.getName();

        if (entryName.equals(jarFileContent)) {

          try (InputStream inputStream = jarFile.getInputStream(entry);
              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
              stringBuilder.append(line);
            }
            return stringBuilder.toString();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
