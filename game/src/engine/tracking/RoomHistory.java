package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Client-local room history. It stores one boolean per room, but no player identity. */
final class RoomHistory {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(RoomHistory.class);
  private static final Path DEFAULT_PATH =
      Path.of("tracking-room-history.properties").toAbsolutePath();

  private RoomHistory() {}

  static synchronized boolean playedBefore(String roomId) {
    return Boolean.parseBoolean(load().getProperty(roomId, "false"));
  }

  static synchronized void markPlayed(String roomId) {
    Properties history = load();
    history.setProperty(roomId, Boolean.TRUE.toString());
    Path temporary = DEFAULT_PATH.resolveSibling(DEFAULT_PATH.getFileName() + ".tmp");
    try {
      Path parent = DEFAULT_PATH.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (OutputStream output = Files.newOutputStream(temporary)) {
        history.store(output, "Dungeon room history. Contains no player identity.");
      }
      try {
        Files.move(
            temporary,
            DEFAULT_PATH,
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException unsupportedAtomicMove) {
        Files.move(temporary, DEFAULT_PATH, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not update local room history at {}", DEFAULT_PATH, exception);
    }
  }

  private static Properties load() {
    Properties history = new Properties();
    try {
      if (!Files.isRegularFile(DEFAULT_PATH)) {
        return history;
      }
      InputStream input = Files.newInputStream(DEFAULT_PATH);
      try (input) {
        history.load(input);
      }
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not read local room history at {}", DEFAULT_PATH, exception);
      return new Properties();
    }
    return history;
  }
}
