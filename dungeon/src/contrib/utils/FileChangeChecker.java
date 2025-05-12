package contrib.utils;

import core.utils.components.path.IPath;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/** Utility class for detecting file changes over time. */
public class FileChangeChecker {

  /**
   * Internal cache of file states from previous checks. Keyed by stringified path (via {@link
   * IPath#pathString()}).
   */
  private static final Map<String, FileSnapshot> fileStates = new HashMap<>();

  /**
   * Checks if the file at the given path has changed since the last call.
   *
   * <p>Two criteria are used for detecting change: 1. last modified timestamp (in milliseconds) 2.
   * The file size in bytes
   *
   * <p>If either of these differs from the previously recorded state, the method returns {@code
   * true} and updates the cached state.
   *
   * @param iPath The file reference to check, wrapped in an {@link IPath} used throughout the
   *     framework.
   * @return {@code true} if the file has changed since the last check (or it is the first), {@code
   *     false} otherwise.
   * @throws IOException if the file does not exist or cannot be accessed.
   */
  public static boolean hasFileChanged(IPath iPath) throws IOException {
    String pathString = iPath.pathString();
    Path path = Paths.get(pathString);

    if (!Files.exists(path)) {
      throw new IOException("File does not exist: " + path);
    }

    FileSnapshot current =
        new FileSnapshot(Files.getLastModifiedTime(path).toMillis(), Files.size(path));

    FileSnapshot previous = fileStates.get(pathString);
    boolean changed = !current.equals(previous);

    fileStates.put(pathString, current);
    return changed;
  }

  /**
   * Immutable snapshot of file metadata at a given point in time.
   *
   * <p>Used internally to compare last-modified time and file size.
   */
  private static class FileSnapshot {
    private final long lastModified;
    private final long size;

    public FileSnapshot(long lastModified, long size) {
      this.lastModified = lastModified;
      this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof FileSnapshot)) return false;
      FileSnapshot other = (FileSnapshot) obj;
      return this.lastModified == other.lastModified && this.size == other.size;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(lastModified) ^ Long.hashCode(size);
    }
  }
}
