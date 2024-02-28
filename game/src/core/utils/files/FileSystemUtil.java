package core.utils.files;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class contains a search (visit) method to recursively visit resources, also called assets.
 *
 * <p>Techniques for all methods: Recursively depth-first search in alphabetical order.
 */
public class FileSystemUtil {

  private FileSystemUtil() {}

  private static void visitJUnitResourcesViaWalkFileTree(
      final String path, final SimpleFilePathVisitorI visitor) throws Exception {
    Files.walkFileTree(
        Paths.get(
            Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path))
                .toURI()
                .normalize()),
        visitor);
  }

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

  private static void visitResourcesViaNewFileSystemNull(
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

  private static void visitResourcesViaNewFileSystemNull(
      final String path, final SimpleFilePathVisitorI visitor, final String fqClassName)
      throws Exception {
    final Object util = Class.forName(fqClassName).getDeclaredConstructor().newInstance();
    final URI uri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, null)) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  private static void visitResourcesViaNewFileSystemEmptyMap(
      final String path, final SimpleFilePathVisitorI visitor, final String fqClassName)
      throws Exception {
    final Object util = Class.forName(fqClassName).getDeclaredConstructor().newInstance();
    final URI uri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
      Files.walkFileTree(fileSystem.getPath(path), visitor);
    }
  }

  /**
   * This method tries to visit resources using different methods in a specific order.
   *
   * <p>Techniques: Recursively depth-first search in alphabetical order.
   *
   * @param path a {@link String} path to the resources
   * @param visitor a {@link SimpleFilePathVisitorI} to use for the visit
   * @param fqClassNames the fully qualified class names of the classes to search in, or null
   * @throws FileNotFoundException if none of the lookup methods work
   */
  public static void visitResources(
      final String path, final SimpleFilePathVisitorI visitor, final String... fqClassNames)
      throws FileNotFoundException {
    try {
      visitJUnitResourcesViaWalkFileTree(path, visitor);
      return;
    } catch (Exception ignore) {
    }

    try {
      visitResourcesViaGetResourceAsStream(path, visitor);
      return;
    } catch (Exception ignore) {
    }

    try {
      visitResourcesViaNewFileSystemNull(path, visitor);
      return;
    } catch (Exception ignore) {
    }

    if (fqClassNames != null) {
      for (final var fqClassName : fqClassNames) {
        try {
          visitResourcesViaNewFileSystemNull(path, visitor, fqClassName);
          return;
        } catch (Exception ignore) {
        }
        try {
          visitResourcesViaNewFileSystemEmptyMap(path, visitor, fqClassName);
          return;
        } catch (Exception ignore) {
        }
      }
    }

    // Not found in any of the methods, throw an exception, can go sure the path does not exist
    throw new FileNotFoundException(path);
  }
}
