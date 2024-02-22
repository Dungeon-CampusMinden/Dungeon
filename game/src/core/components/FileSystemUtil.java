package core.components;

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
 */
public class FileSystemUtil {

  private FileSystemUtil() {}

  /**
   * Defines a method that checks if the current thread is running inside a JUnit test by inspecting
   * the stack trace. If any of the stack trace elements' class names start with "org.junit.", the
   * method returns true; otherwise, it returns false.
   *
   * @return true if the current thread is running inside a JUnit test, false otherwise
   */
  private static boolean isStartedInJUnitTest() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (element.getClassName().startsWith("org.junit.")) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method tries to recursively visit resources using the test resources directory. It checks
   * if the method is called in a JUnit test and throws an exception if not. Then it walks the file
   * tree starting from the specified path, using the provided file visitor.
   *
   * @param path the path to the resources
   * @param visitor the file visitor to use
   * @throws Exception if the method is not called in a JUnit test
   */
  private static void visitResourcesViaJUnit(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
    if (!isStartedInJUnitTest()) {
      throw new IllegalStateException(
          "FileSystemUtil.visitResourcesViaJUnit can only be used in JUnit tests");
    }
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
  private static void visitResourcesViaContextClassLoader(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
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
                      visitResourcesViaContextClassLoader(p, visitor);
                    }
                  } catch (Exception e) {
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
  private static void visitResourcesViaGameJarFile(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
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
  private static void visitResourcesViaDungeonJarFile(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
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
  private static void visitResourcesViaDungeonFiles(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
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
   * @param path the path to the resources
   * @param visitor the file visitor to use
   * @throws Exception if none of the lookup methods work
   */
  public static void visitResources(final String path, final SimpleFileVisitor<Path> visitor)
      throws Exception {
    try {
      // tries to test if the method is called in a JUnit test, and if we can get results from it
      visitResourcesViaJUnit(path, visitor);
      return; // successful with JUnit, no need to continue
    } catch (Exception ignore) {
    }
    // Not found in JUnit, try via ContextClassLoader next

    try {
      // tries to test if we can get results from the ContextClassLoader
      visitResourcesViaContextClassLoader(path, visitor);
      return; // successful with ContextClassLoader, no need to continue
    } catch (Exception ignore) {
    }
    // Not found in ContextClassLoader, try via GameJar next

    try {
      // tries to test if we can get results from the GameJar file
      visitResourcesViaGameJarFile(path, visitor);
      return; // successful with GameJar, no need to continue
    } catch (Exception ignore) {
    }
    // Not found in GameJar, try via DungeonJar next

    try {
      // tries to test if we can get results from the DungeonJar file
      visitResourcesViaDungeonJarFile(path, visitor);
      return; // successful with DungeonJar, no need to continue
    } catch (Exception ignore) {
    }
    // Not found in DungeonJar, try via DungeonFiles next

    // also tries to test if we can get results from the Dungeon files
    visitResourcesViaDungeonFiles(path, visitor);

    // No exception: Successful with DungeonFiles, normal end of method
    // Exception: Not found in DungeonFiles also, throw an exception as last resort
  }
}
