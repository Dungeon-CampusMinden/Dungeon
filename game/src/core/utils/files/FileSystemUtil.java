package core.utils.files;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains a search (visit) method to recursively visit resources, also called assets.
 *
 * <p>Techniques for all methods: Recursively depth-first search in alphabetical order.
 */
public class FileSystemUtil {

  private FileSystemUtil() {}

  public static void visitJUnitResourcesViaWalkFileTree(
      final String path, final FileSystemUtilVisitor visitor) throws IOException {
    try {
      Files.walkFileTree(
          Paths.get(
              Objects.requireNonNull(
                      Thread.currentThread().getContextClassLoader().getResource(path))
                  .toURI()
                  .normalize()),
          visitor);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  private static final HashSet<String> paths = new HashSet<>();
  private static int max_paths = 0;
  private static final Logger LOGGER = Logger.getLogger(FileSystemUtil.class.getSimpleName());

  public static void visitResources(final String path, final FileSystemUtilVisitor visitor)
      throws IOException {
    // Try first to visit test resources, cause some tests need it in Crafting and DrawComponent
    // class
    try {
      visitJUnitResourcesViaWalkFileTree(path, visitor);
      paths.add(path);
      if (paths.size() > max_paths) {
        max_paths = paths.size();
        LOGGER.log(Level.WARNING, "Warnung: Paths: Max: " + max_paths);
        LOGGER.log(Level.WARNING, "Warnung: Paths: " + paths);
      }
      return;
    } catch (IOException ignore) {
    }

    // Fall back to normal file system
    Files.walkFileTree(Paths.get(path), visitor);
  }
}
