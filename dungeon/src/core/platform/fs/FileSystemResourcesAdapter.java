package core.platform.fs;

import core.platform.ResourcesAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Loads resources from the local filesystem.
 *
 * Useful while assets still live in folders like:
 * - <repoRoot>/dungeon/assets/...
 * - <repoRoot>/assets/...
 * - <moduleDir>/assets/...
 */
public final class FileSystemResourcesAdapter implements ResourcesAdapter {
  private final List<Path> roots;

  public FileSystemResourcesAdapter(List<Path> roots) {
    this.roots = List.copyOf(Objects.requireNonNull(roots));
  }

  /** Creates an adapter that searches in CWD, parent, and grandparent. */
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
