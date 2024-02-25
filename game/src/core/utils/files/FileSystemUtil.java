package core.utils.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class contains a search (visit) method to recursively visit resources, also called assets.
 *
 * <p>Techniques for all methods: Recursively depth-first search in alphabetical order, not
 * following symlinks.
 */
public class FileSystemUtil {

  private FileSystemUtil() {}

  /**
   * This method tries to recursively visit resources using the test resources directory. It checks
   * if the method is called in a JUnit test and throws an exception if not. Then it walks the file
   * tree starting from the specified path, using the provided file visitor.
   *
   * @param path the path to the resources
   * @param visitor the file visitor to use
   * @throws Exception if the method is not called in a JUnit test
   */
  private static void visitJUnitResourcesViaWalkFileTree(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    Files.walkFileTree(
        Paths.get(
            Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path))
                .toURI()
                .normalize()),
        visitor);
  }

  /**
   * This method tries to recursively visit resources using the context class loader. If it fails,
   * an exception is thrown. Then it walks the file tree starting from the specified path, using the
   * provided file visitor.
   *
   * @param path the path to the resources
   * @param visitor the file visitor to use
   * @throws Exception if the method is not called in a JUnit test
   */
  private static void visitResourcesViaGetResourceAsStream(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (final InputStream is = loader.getResourceAsStream(path)) {
      assert is != null;
      try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
          final BufferedReader br = new BufferedReader(isr)) {
        List<String> list = br.lines().toList();
        if (list.isEmpty()) {
          throw new IOException("Empty: " + path);
        }
        list.stream()
            .map(l -> path + "/" + l)
            .forEach(
                p -> {
                  try {
                    final Path path2 =
                        Paths.get(
                            Objects.requireNonNull(loader.getResource(p.replace("//", "/")))
                                .toURI());
                    visitor.visitFile(path2, null);
                    if (Files.isDirectory(path2)) {
                      visitResourcesViaGetResourceAsStream(p, visitor);
                    }
                  } catch (Exception e) {
                    // A generic exception is thrown if something goes wrong within the visitor or
                    // recursive call
                    throw new RuntimeException(e);
                  }
                });
      }
    }
  }

  /**
   * This method tries to recursively visit resources using the game jar file.
   *
   * @param path the path to the game jar file
   * @param visitor the file visitor to visit the resources
   * @throws Exception if an error occurs during the process
   */
  private static void visitGameJarResourcesViaNewFileSystemNull(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    //noinspection InstantiationOfUtilityClass
    final FileSystemUtil util = new FileSystemUtil();
    final URI jarUri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(jarUri, null)) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  /**
   * This method tries to recursively visit resources using the dungeon jar file or in the assets
   * directory if it is started from an IDE.
   *
   * @param path the path to the game jar file
   * @param visitor the file visitor to visit the resources
   * @throws Exception if an error occurs during the process
   */
  private static void visitDungeonJarResourcesViaNewFileSystemNull(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    final Object util =
        Class.forName("contrib.crafting.Crafting").getDeclaredConstructor().newInstance();
    final URI jarUri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(jarUri, null)) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  /**
   * This method also tries to recursively visit resources using the dungeon jar file or in the
   * assets directory if it is started from an IDE, but uses a different approach.
   *
   * @param path the path to the game jar file
   * @param visitor the file visitor to visit the resources
   * @throws Exception if an error occurs during the process
   */
  private static void visitDungeonJarResourcesViaNewFileSystemEmptyMap(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    // we need at least one class form dungeon to instantiate here
    final Object util =
        Class.forName("contrib.crafting.Crafting").getDeclaredConstructor().newInstance();
    final URI uri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  /**
   * This method tries to visit resources using different methods in a specific order, and if none
   * of them work, it throws an exception as a last resort. The order is: JUnit, ContextClassLoader,
   * GameJar, DungeonJar, DungeonFiles. If none of them work, you can be sure that the path to the
   * resources does not exist.
   *
   * <p>Techniques: Recursively depth-first search in alphabetical order, not following symlinks.
   *
   * @param path the path to the resources
   * @param visitor the file visitor to use
   * @throws Exception if none of the lookup methods work
   */
  public static void visitResources(final String path, final SimpleFilePathVisitorI visitor)
      throws Exception {
    try {
      visitJUnitResourcesViaWalkFileTree(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in JUnit, try via ContextClassLoader next

    try {
      visitResourcesViaGetResourceAsStream(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in ContextClassLoader, try via GameJar next

    try {
      visitGameJarResourcesViaNewFileSystemNull(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in GameJar, try via DungeonJar next

    try {
      visitDungeonJarResourcesViaNewFileSystemNull(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in DungeonJar, try via DungeonFiles next

    visitDungeonJarResourcesViaNewFileSystemEmptyMap(path, visitor);
    // No exception: Successful with DungeonFiles, normal end of method
    // Exception: Not found in DungeonFiles also, throw an exception if none of the methods work
  }
}
