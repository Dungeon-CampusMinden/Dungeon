package core.utils;

import core.utils.components.path.IPath;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;

/** This utility class can be used to walk through a file system. */
public class ResourceWalker {

  /**
   * This utility method can be used to walk through a file system.
   *
   * @param path The path to walk.
   * @param acceptor The acceptor function that decides if the path should be processed and added to
   *     the result map.
   * @return The subdirectory map of the paths found.
   * @throws Exception If an error occurs during the walk.
   */
  public static Map<String, List<Path>> walk(IPath path, Function<Path, Boolean> acceptor)
      throws Exception {
    Map<String, List<Path>> subdirectoryMap = new HashMap<>();

    SimpleFileVisitor<Path> visitor =
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            if (acceptor.apply(file)) {
              subdirectoryMap
                  .computeIfAbsent(
                      file.getParent().getFileName().toString(), k -> new ArrayList<>())
                  .add(file);
            }
            return FileVisitResult.CONTINUE;
          }
        };

    Object util =
        Class.forName(ResourceWalker.class.getName()).getDeclaredConstructor().newInstance();
    URI uri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    Map<String, String> env = Map.of("create", "true");
    try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
      Files.walkFileTree(fs.getPath(path.pathString()), visitor);
    } catch (Exception e) {
      // Not in zip file ...
      // Try regular file system.
      Path entryPath =
          Paths.get(
              Objects.requireNonNull(
                      Thread.currentThread().getContextClassLoader().getResource(path.pathString()))
                  .toURI());
      Files.walkFileTree(entryPath, visitor);
    }

    return subdirectoryMap;
  }
}
