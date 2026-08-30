package engine.tracking;

import java.nio.file.Path;
import java.util.Objects;

/** Internal signal for a failed local outbox operation. */
final class TrackingPersistenceException extends RuntimeException {
  private final Path outboxPath;

  TrackingPersistenceException(String message, Path outboxPath, Throwable cause) {
    super(message, cause);
    this.outboxPath = Objects.requireNonNull(outboxPath, "outboxPath");
  }

  Path outboxPath() {
    return outboxPath;
  }
}
