package wizard.authoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Same-directory temporary writes with an atomic replacement where the filesystem supports it. */
final class AtomicFiles {
  private AtomicFiles() {}

  static void replace(final Path target, final byte[] bytes) throws IOException {
    Path normalized = target.toAbsolutePath().normalize();
    Files.createDirectories(normalized.getParent());
    Path temporary =
        Files.createTempFile(normalized.getParent(), normalized.getFileName().toString(), ".tmp");
    try {
      Files.write(temporary, bytes);
      Files.move(
          temporary,
          normalized,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
  }
}
