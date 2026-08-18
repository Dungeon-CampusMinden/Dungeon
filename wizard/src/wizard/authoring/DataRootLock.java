package wizard.authoring;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Process-wide ownership of one Wizard data root for the complete host lifetime. */
final class DataRootLock implements AutoCloseable {
  private final FileChannel channel;
  private final FileLock lock;

  private DataRootLock(final FileChannel channel, final FileLock lock) {
    this.channel = channel;
    this.lock = lock;
  }

  static DataRootLock acquire(final Path dataRoot) {
    Path root = dataRoot.toAbsolutePath().normalize();
    FileChannel channel = null;
    try {
      Files.createDirectories(root);
      channel =
          FileChannel.open(
              root.resolve("authoring-host.lock"),
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE);
      FileLock lock;
      try {
        lock = channel.tryLock();
      } catch (OverlappingFileLockException exception) {
        lock = null;
      }
      if (lock == null) {
        channel.close();
        throw new IllegalStateException(
            "Another Dungeon Wizard host is already using this data directory: " + root);
      }
      return new DataRootLock(channel, lock);
    } catch (IOException exception) {
      if (channel != null) {
        try {
          channel.close();
        } catch (IOException ignored) {
          // Preserve the actionable acquisition failure.
        }
      }
      throw new IllegalStateException(
          "Dungeon Wizard data directory cannot be locked: " + root, exception);
    }
  }

  @Override
  public void close() {
    try {
      lock.close();
    } catch (IOException ignored) {
      // Process shutdown releases the operating-system lock as a final fallback.
    }
    try {
      channel.close();
    } catch (IOException ignored) {
      // Process shutdown closes remaining channels.
    }
  }
}
