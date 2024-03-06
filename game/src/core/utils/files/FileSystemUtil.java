package core.utils.files;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Objects;

/**
 * This class contains a search (visit) method to recursively visit resources and test resources.
 *
 * <p>Techniques for all methods: Recursively depth-first search in alphabetical order.
 */
public final class FileSystemUtil {

  private FileSystemUtil() {}

  public static void visitResources(final String path, final SimpleFileVisitor<Path> visitor)
      throws IOException {
    try {
      Files.walkFileTree(
          Paths.get(
              Objects.requireNonNull(
                      Thread.currentThread().getContextClassLoader().getResource(path))
                  .toURI()),
          visitor);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
}
