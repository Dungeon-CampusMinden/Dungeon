package core.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** This class contains utility methods for working with files and directories. */
public class FilesystemUtil {
  private static boolean isStartedInJarFile(final Object instance) {
    try {
      return Objects.requireNonNull(
              instance.getClass().getResource(instance.getClass().getSimpleName() + ".class"))
          .toURI()
          .getScheme()
          .equals("jar");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isStartedInJUnitTest() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (element.getClassName().startsWith("org.junit.")) {
        return true;
      }
    }
    return false;
  }

  private static URI getUriToJarFileEntry(final Object instance) {
    try {
      return new URI(
          Objects.requireNonNull(
              Objects.requireNonNull(
                      instance
                          .getClass()
                          .getResource(instance.getClass().getSimpleName() + ".class"))
                  .toURI()
                  .toURL()
                  .toExternalForm()));
    } catch (URISyntaxException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Searches for asset files in the given directory or JAR file. This method can determine if the
   * application is running in a JAR file or in a normal filesystem.
   *
   * @param pathToDirectory the path to the directory to search
   * @param instance the instance to search for
   * @return a map of file endings to lists of file paths
   */
  public static Map<String, List<String>> searchAssetFiles(
      final String pathToDirectory, final Object instance) {
    return searchAssetFilesWithEnding(pathToDirectory, "", instance);
  }

  /**
   * Searches for asset files with a specific ending in the given directory or JAR file. This method
   * can determine if the application is running in a JAR file or in a normal filesystem.
   *
   * @param pathToDirectory the path to the directory or JAR file
   * @param ending the ending of the asset files to search for
   * @param instance the instance representing the directory or JAR file
   * @return a map containing directory paths as keys and a list of matching file names as values
   */
  public static Map<String, List<String>> searchAssetFilesWithEnding(
      final String pathToDirectory, final String ending, final Object instance) {
    Map<String, List<String>> dirSubdirMap = new HashMap<>();

    try {
      if (isStartedInJUnitTest()) {
        // inside JUnit test
        Files.walkFileTree(
            Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread().getContextClassLoader().getResource(pathToDirectory))
                    .toURI()
                    .normalize()),
            new MyFileVisitor(ending, dirSubdirMap));
      } else if (isStartedInJarFile(instance)) {
        // inside JAR
        try (FileSystem fileSystem =
            FileSystems.newFileSystem(getUriToJarFileEntry(instance), Collections.emptyMap())) {
          Files.walkFileTree(
              fileSystem.getPath(pathToDirectory), new MyFileVisitor(ending, dirSubdirMap));
        }
      } else {
        // normal filesystem, e.g. in IDE
        Files.walkFileTree(Paths.get(pathToDirectory), new MyFileVisitor(ending, dirSubdirMap));
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return dirSubdirMap;
  }

  private static class MyFileVisitor extends SimpleFileVisitor<Path> {
    private final String ending;
    private final Map<String, List<String>> dirSubdirMap;

    public MyFileVisitor(final String ending, final Map<String, List<String>> dirSubdirMap) {
      this.ending = ending;
      this.dirSubdirMap = dirSubdirMap;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (!Files.isDirectory(file) && file.toString().endsWith(ending)) {
        String parentDirName = file.getParent().getFileName().toString();
        String halfAbsPath = file.toString();
        dirSubdirMap.computeIfAbsent(parentDirName, k -> new ArrayList<>()).add(halfAbsPath);
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
