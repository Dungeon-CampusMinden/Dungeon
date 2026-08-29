package engine.tracking;

import java.nio.file.Path;
import java.util.Objects;

/** Internal signal for a failed local outbox or sidecar operation. */
final class TrackingPersistenceException extends RuntimeException {
  private final Path path;
  private final Path outboxPath;

  TrackingPersistenceException(String message, Path path, Path outboxPath, Throwable cause) {
    super(message, cause);
    this.path = Objects.requireNonNull(path, "path");
    this.outboxPath = Objects.requireNonNull(outboxPath, "outboxPath");
  }

  Path path() {
    return path;
  }

  Path outboxPath() {
    return outboxPath;
  }
}
