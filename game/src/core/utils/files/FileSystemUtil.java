package core.utils.files;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.Collections;
import java.util.Objects;

/** This class contains utility methods for working with files and directories. */
public class FileSystemUtil {
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

  public static void searchAssetFilesInSubdirectories(
      final String pathToDirectory, final SimpleFileVisitor<Path> visitor) {
    try {
      if (isStartedInJUnitTest()) {
        // inside JUnit test
        Files.walkFileTree(
            Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread().getContextClassLoader().getResource(pathToDirectory))
                    .toURI()
                    .normalize()),
            visitor);
      } else if (isStartedInJarFile()) {
        // inside JAR file
        try (FileSystem fileSystem =
            FileSystems.newFileSystem(getUriToJarFileEntry(), Collections.emptyMap())) {
          Files.walkFileTree(fileSystem.getPath(pathToDirectory), visitor);
        }
      } else {
        // normal filesystem, e.g. in IDE
        Files.walkFileTree(Paths.get(pathToDirectory), visitor);
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
