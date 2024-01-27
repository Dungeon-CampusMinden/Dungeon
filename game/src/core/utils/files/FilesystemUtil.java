package core.utils.files;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** This class contains utility methods for working with files and directories. */
public class FilesystemUtil {
  public static final Object DUMMY_INST = new DummyClazzInOwnCodeBase();

  private static boolean isStartedInJarFile() {
    try {
      return Objects.requireNonNull(
              DUMMY_INST.getClass().getResource(DUMMY_INST.getClass().getSimpleName() + ".class"))
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

  private static URI getUriToJarFileEntry() {
    try {
      return new URI(
          Objects.requireNonNull(
              Objects.requireNonNull(
                      DUMMY_INST
                          .getClass()
                          .getResource(DUMMY_INST.getClass().getSimpleName() + ".class"))
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
   * @return a map of file endings to lists of file paths
   */
  public static Map<String, List<String>> searchAssetFiles(final String pathToDirectory) {
    return searchAssetFilesWithEnding(pathToDirectory, "");
  }

  /**
   * Searches for asset files with a specific ending in the given directory or JAR file. This method
   * can determine if the application is running in a JAR file or in a normal filesystem.
   *
   * @param pathToDirectory the path to the directory or JAR file
   * @param ending the ending of the asset files to search for
   * @return a map containing directory paths as keys and a list of matching file names as values
   */
  public static Map<String, List<String>> searchAssetFilesWithEnding(
      final String pathToDirectory, final String ending) {
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
            new DefaultSimpleFileVisitor(ending, dirSubdirMap));
      } else if (isStartedInJarFile()) {
        // inside JAR file
        try (FileSystem fileSystem =
            FileSystems.newFileSystem(getUriToJarFileEntry(), Collections.emptyMap())) {
          Files.walkFileTree(
              fileSystem.getPath(pathToDirectory),
              new DefaultSimpleFileVisitor(ending, dirSubdirMap));
        }
      } else {
        // normal filesystem, e.g. in IDE
        Files.walkFileTree(
            Paths.get(pathToDirectory), new DefaultSimpleFileVisitor(ending, dirSubdirMap));
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return dirSubdirMap;
  }
}
