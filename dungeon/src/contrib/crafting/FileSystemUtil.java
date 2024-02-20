package contrib.crafting;

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

  public static void visitResources(final String path, final SimpleFileVisitor<Path> visitor)
      throws Exception {
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
                            loader
                                .getResource(p.replace("//", "/"))
                                .toExternalForm()
                                .substring(6)
                                .replace("%20", " "));
                    System.out.println("path2 = " + path2);
                    visitor.visitFile(path2, null);
                    if (Files.isDirectory(path2)) {
                      visitResources(p, visitor);
                    }
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                });
      }
    } catch (IOException e) {
      //noinspection InstantiationOfUtilityClass
      final FileSystemUtil util = new FileSystemUtil();
      final String jarPath1 =
          Objects.requireNonNull(
                  util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
              .toExternalForm();
      final String jarPath2 =
          jarPath1.substring(0, jarPath1.lastIndexOf('!')).replace("game", "dungeon");
      System.out.println("jarPath2 = " + jarPath2);
      try (FileSystem fileSystem =
          FileSystems.newFileSystem(URI.create(jarPath2), Collections.emptyMap())) {
        Files.walkFileTree(fileSystem.getPath(path), visitor);
      }
    }
  }
}
