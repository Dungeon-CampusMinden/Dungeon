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
    final String jarPath1 =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toExternalForm();
    String jarPath2 = jarPath1.substring(0, jarPath1.lastIndexOf('!'));
    if (jarPath2.contains("/dungeon/")) {
      jarPath2 = jarPath2.replace("/dungeon/", "/game/");
      jarPath2 = jarPath2.replace("dungeon.jar", "game.jar");
    }
    try (FileSystem fileSystem =
        FileSystems.newFileSystem(URI.create(jarPath2), Collections.emptyMap())) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  private static void visitResourcesViaDungeonJarFile(
      final String path, final SimpleFileVisitor<Path> visitor) throws Exception {
    //noinspection InstantiationOfUtilityClass
    final FileSystemUtil util = new FileSystemUtil();
    final String jarPath1 =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toExternalForm();
    String jarPath2 = jarPath1.substring(0, jarPath1.lastIndexOf('!'));
    if (jarPath2.contains("/game/")) {
      jarPath2 = jarPath2.replace("/game/", "/dungeon/");
      jarPath2 = jarPath2.replace("game.jar", "dungeon.jar");
    }
    try (FileSystem fileSystem =
        FileSystems.newFileSystem(URI.create(jarPath2), Collections.emptyMap())) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  public static void visitResources(final String path, final SimpleFileVisitor<Path> visitor)
      throws Exception {
    try {
      visitResourcesViaJUnit(path, visitor);
      return;
    } catch (Exception e) {
      // Not found in JUnit, try via ContextClassLoader next
    }
    try {
      visitResourcesViaContextClassLoader(path, visitor);
      return;
    } catch (Exception e) {
      // Not found in ContextClassLoader, try via GameJar next
    }
    try {
      visitResourcesViaGameJarFile(path, visitor);
      return;
    } catch (Exception e) {
      // Not found in GameJar, try via DungeonJar next
    }
    visitResourcesViaDungeonJarFile(path, visitor);
    //   Not found in DungeonJar, throw exception to the caller
  }
}
