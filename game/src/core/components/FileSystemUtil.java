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

public class FileSystemUtil {

  private FileSystemUtil() {}

  private static boolean isStartedInJUnitTest() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (element.getClassName().startsWith("org.junit.")) {
        return true;
      }
    }
    return false;
  }

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
                                .toExternalForm()
                                .substring(6)
                                .replace("%20", " "));
                    visitor.visitFile(path2, null);
                    if (Files.isDirectory(path2)) {
                      visitResources(p, visitor);
                    }
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                });
      }
    }
  }

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

  public static void visitResources(final String path, final SimpleFileVisitor<Path> visitor)
      throws Exception {
    try {
      visitResourcesViaJUnit(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in JUnit, try via ContextClassLoader next

    try {
      visitResourcesViaContextClassLoader(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in ContextClassLoader, try via GameJar next

    try {
      visitResourcesViaGameJarFile(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in GameJar, try via DungeonJar next

    try {
      visitResourcesViaDungeonJarFile(path, visitor);
      return;
    } catch (Exception ignore) {
    }
    // Not found in DungeonJar, try via DungeonFiles next

    visitResourcesViaDungeonFiles(path, visitor);
    // Not found in DungeonFiles also, throw an exception as last resort
  }
}
