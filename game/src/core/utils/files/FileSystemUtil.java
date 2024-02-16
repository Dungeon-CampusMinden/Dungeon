package core.utils.files;

import core.utils.components.path.IPath;
import java.io.File;
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

  private static boolean isStartedInJarFile() {
    //noinspection InstantiationOfUtilityClass
    final FileSystemUtil helper = new FileSystemUtil();
    try {
      return Objects.requireNonNull(
              helper.getClass().getResource(helper.getClass().getSimpleName() + ".class"))
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
    //noinspection InstantiationOfUtilityClass
    final FileSystemUtil helper = new FileSystemUtil();
    try {
      return new URI(
          Objects.requireNonNull(
              Objects.requireNonNull(
                      helper.getClass().getResource(helper.getClass().getSimpleName() + ".class"))
                  .toURI()
                  .toURL()
                  .toExternalForm()));
    } catch (URISyntaxException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method searches for all asset files in the subdirectories of the specified directory. It
   * is not necessary to explicitly specify the asset storage location, as this method automatically
   * determines the storage location and how the game was started. Furthermore, a {@link
   * SimpleFileVisitor} must be passed, which is called for each file or folder found. You can then
   * specify yourself what should happen to the files found, for example mapping them into another
   * structure or reading them to use them in the game.
   *
   * @param pathToDirectory is a {@link IPath} relative to the root asset directory of the game to
   *     search.
   * @param visitor is a {@link SimpleFileVisitor} for {@link Path}, which is called for each file
   *     or folder found.
   */
  public static void searchAssetFilesInSubdirectories(
      final IPath pathToDirectory, final SimpleFileVisitor<Path> visitor) {
    if (isStartedInJUnitTest()) {
      // inside JUnit test
      try {
        Files.walkFileTree(
            Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread()
                            .getContextClassLoader()
                            .getResource(pathToDirectory.pathString()))
                    .toURI()
                    .normalize()),
            visitor);
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(e);
      }
    } else if (isStartedInJarFile()) {
      // inside JAR file
      try (FileSystem fileSystem =
          FileSystems.newFileSystem(getUriToJarFileEntry(), Collections.emptyMap())) {
        Files.walkFileTree(fileSystem.getPath(pathToDirectory.pathString()), visitor);
        /*
        NoSuchFileException occurs here, even if was not started inside jar file
         */
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      // normal filesystem, e.g. in IDE
      try {
        Files.walkFileTree(Paths.get(pathToDirectory.pathString()), visitor);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Returns a simple {@link File} that is located within the resource directory.
   *
   * @param filePath the path to the resource file, relative to the asset directory root
   * @return a simple {@link File} that is located within the resource directory
   */
  public static File getSingleFile(final IPath filePath) {
    if (isStartedInJUnitTest()) {
      // inside JUnit test
      try {
        return new File(
            Objects.requireNonNull(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResource(filePath.pathString()))
                .toURI()
                .normalize());
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    } else if (isStartedInJarFile()) {
      // inside JAR file
      try (FileSystem fileSystem =
          FileSystems.newFileSystem(getUriToJarFileEntry(), Collections.emptyMap())) {
        return fileSystem.getPath(filePath.pathString()).toFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      // normal filesystem, e.g. in IDE
      return new File(filePath.pathString());
    }
  }
}
