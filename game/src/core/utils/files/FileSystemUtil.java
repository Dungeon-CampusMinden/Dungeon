package core.utils.files;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Objects;

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

  public static void visitResources(final String path, final FileSystemUtilVisitor visitor)
      throws IOException {
    // Try first to visit test resources, cause some tests need it in Crafting and DrawComponent
    // class
    try {
      visitJUnitResourcesViaWalkFileTree(path, visitor);
      return;
    } catch (IOException ignore) {
    }

    // Fall back to normal file system
    Files.walkFileTree(Paths.get(path), visitor);
  }
}
