package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlRecord;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;

/** Crash-tolerant persistence for one self-contained tracking session file. */
final class TrackingOutbox {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TrackingOutbox.class);

  private final Path path;
  private boolean failed;
  private Throwable failureCause;

  private TrackingOutbox(Path path) {
    this.path = path;
  }

  static TrackingOutbox create(Path directory, TrackingSessionDescriptor descriptor) {
    Path path = directory.resolve(descriptor.sessionId() + ".jsonl").toAbsolutePath().normalize();
    TrackingOutbox outbox = new TrackingOutbox(path);
    try {
      Files.createDirectories(path.getParent());
      Files.createFile(path);
    } catch (IOException | RuntimeException exception) {
      throw outbox.failure("Could not create new tracking outbox " + path, exception);
    }
    outbox.append(new TrackingJsonlRecord.Session(descriptor), "session header");
    return outbox;
  }

  Path path() {
    return path;
  }

  boolean failed() {
    return failed;
  }

  void append(TrackingEvent event) {
    append(new TrackingJsonlRecord.Event(event), "event sequence " + event.sessionSequence());
  }

  void append(TrackingSessionFinish finish) {
    append(new TrackingJsonlRecord.Finish(finish), "session finish");
  }

  private void append(TrackingJsonlRecord record, String description) {
    if (failed) {
      throw new TrackingPersistenceException(
          "Tracking outbox is unavailable after an earlier persistence failure " + path,
          path,
          failureCause);
    }
    byte[] bytes;
    try {
      bytes =
          (TrackingJson.writeJsonlRecord(record) + System.lineSeparator())
              .getBytes(StandardCharsets.UTF_8);
    } catch (RuntimeException exception) {
      throw failure("Could not encode " + description + " for " + path, exception);
    }
    long originalSize;
    try {
      originalSize = Files.size(path);
    } catch (IOException | RuntimeException exception) {
      throw failure("Could not inspect tracking outbox " + path, exception);
    }
    try (FileChannel channel =
        FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
      channel.force(true);
    } catch (IOException | RuntimeException exception) {
      rollback(originalSize, exception);
      throw failure("Could not append " + description + " to " + path, exception);
    }
  }

  private void rollback(long originalSize, Throwable appendFailure) {
    try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
      channel.truncate(originalSize);
      channel.force(true);
    } catch (IOException | RuntimeException rollbackFailure) {
      appendFailure.addSuppressed(rollbackFailure);
      LOGGER.error("Could not roll back failed tracking append at {}", path, rollbackFailure);
    }
  }

  private TrackingPersistenceException failure(String message, Throwable cause) {
    failed = true;
    if (failureCause == null) {
      failureCause = cause;
    }
    return new TrackingPersistenceException(message, path, cause);
  }
}
