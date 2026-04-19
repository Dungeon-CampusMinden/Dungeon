package core.resources;

import core.platform.adapters.ResourcesAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A ResourcesAdapter implementation that loads resources from the filesystem.
 *
 * <p>FileSystemResourcesAdapter provides access to resources located in the filesystem. It supports
 * multiple root directories, checking each in order for the requested resource. Absolute paths are
 * also supported and will be used directly without prefixing a root.
 *
 * <p>Key features:
 * <ul>
 *   <li>Supports multiple root directories for resource lookup
 *   <li>Checks for resource existence using Files API
 *   <li>Opens input streams for resource reading
 *   <li>Normalizes paths to use forward slashes and remove leading "./"
 *   <li>Provides meaningful error messages for missing resources
 * </ul>
 *
 * <p>Path normalization ensures compatibility across platforms: backslashes are replaced with
 * forward slashes, and the leading "./" is removed. Absolute paths are supported as-is, while
 * relative paths are resolved against each configured root directory in order.
 */
public final class FileSystemResourcesAdapter implements ResourcesAdapter {
  private final List<Path> roots;

  /**
   * Constructs a FileSystemResourcesAdapter that provides access to resources located in the
   * specified root directories. The adapter ensures that the provided list of roots is not null and
   * creates an unmodifiable copy of it for internal use.
   *
   * @param roots a list of root directories {@link Path} where the adapter should search for
   *              resources. Must not be null.
   * @throws NullPointerException if {@code roots} is null
   */
  public FileSystemResourcesAdapter(List<Path> roots) {
    this.roots = List.copyOf(Objects.requireNonNull(roots));
  }

  /**
   * Automatically detects the current working directory and its immediate parent directories
   * (up to two levels) and initializes a {@link FileSystemResourcesAdapter} with these directories
   * as its root paths.
   *
   * <p>The detected paths provide a basis for locating resources within the filesystem. The method
   * ensures that the current working directory and, if available, its parent and grandparent
   * directories are included in the root paths list.
   *
   * @return a {@link FileSystemResourcesAdapter} instance initialized with the current working
   *         directory and its parent directories as root paths.
   */
  public static FileSystemResourcesAdapter autoDetect() {
    final Path cwd = Paths.get("").toAbsolutePath().normalize();

    final List<Path> roots = new ArrayList<>();
    roots.add(cwd);

    final Path parent = cwd.getParent();
    if (parent != null) roots.add(parent);

    final Path grandParent = parent != null ? parent.getParent() : null;
    if (grandParent != null) roots.add(grandParent);

    return new FileSystemResourcesAdapter(roots);
  }

  @Override
  public boolean exists(String path) {
    final Path resolved = resolve(path);
    return resolved != null && Files.isRegularFile(resolved);
  }

  @Override
  public InputStream open(String path) throws IOException {
    final Path resolved = resolve(path);
    if (resolved == null) {
      throw new IOException("Invalid resource path: " + path);
    }
    if (!Files.isRegularFile(resolved)) {
      throw new IOException("Resource not found on filesystem: " + resolved);
    }
    return Files.newInputStream(resolved, StandardOpenOption.READ);
  }

  private Path resolve(String rawPath) {
    if (rawPath == null) return null;
    final String p = normalize(rawPath);
    if (p.isBlank()) return null;

    // Absolute path? use directly.
    final Path direct = Paths.get(p);
    if (direct.isAbsolute()) return direct.normalize();

    // Try all roots.
    for (Path root : roots) {
      final Path candidate = root.resolve(p).normalize();
      if (Files.isRegularFile(candidate)) return candidate;
    }

    // Not found: return a sensible “best guess” for error messages.
    return roots.isEmpty() ? direct.normalize() : roots.getFirst().resolve(p).normalize();
  }

  private static String normalize(String path) {
    String p = path.replace('\\', '/');
    while (p.startsWith("./")) p = p.substring(2);
    // IMPORTANT: do NOT strip leading "/" here (would break absolute unix paths)
    return p;
  }
}
