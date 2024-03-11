package core.utils;

import core.utils.components.path.IPath;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;

public class ResourceWalker {
  public static Map<String, List<Path>> walk(IPath path, Function<Path, Boolean> acceptor)
      throws Exception {
    final Map<String, List<Path>> subdirectoryMap = new HashMap<>();
    final SimpleFileVisitor<Path> visitor =
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

    final Object util =
        Class.forName(ResourceWalker.class.getName()).getDeclaredConstructor().newInstance();
    final URI uri =
        Objects.requireNonNull(
                util.getClass().getResource(util.getClass().getSimpleName() + ".class"))
            .toURI();
    Map<String, String> env =
        Map.of(
            "create", "true"
            // other args here ...
            );
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
