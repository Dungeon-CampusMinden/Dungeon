package rooms.programming.save;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import rooms.programming.state.ProgrammingPhase;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Stores only the act checkpoint and the run's tracking decision. */
public final class ProgrammingSave {
  private static final Path PATH = Path.of("programming-save.json");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ProgrammingSave.class);
  private static final JsonMapper JSON =
      JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

  private ProgrammingSave() {}

  /**
   * @return the valid checkpoint, or empty for a missing or malformed save
   */
  public static Optional<SaveData> read() {
    if (!Files.isRegularFile(PATH)) return Optional.empty();
    try {
      return Optional.ofNullable(JSON.readValue(Files.readString(PATH), SaveData.class));
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not read Programming savegame: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  /**
   * @return whether the menu can offer Continue
   */
  public static boolean exists() {
    return read().isPresent();
  }

  /**
   * Replaces the checkpoint atomically where supported by the filesystem.
   *
   * @param data act and run metadata
   */
  public static void write(SaveData data) {
    Path absolute = PATH.toAbsolutePath();
    Path temporary = absolute.resolveSibling(absolute.getFileName() + ".tmp");
    try {
      Files.writeString(temporary, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(data));
      try {
        Files.move(
            temporary,
            absolute,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Could not write Programming savegame.", exception);
    }
  }

  /** Removes the checkpoint for a new game or a completed room. */
  public static void delete() {
    try {
      Files.deleteIfExists(PATH);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not delete Programming savegame.", exception);
    }
  }

  /**
   * Updates an existing checkpoint without creating a new one.
   *
   * @param consent nullable run-level tracking decision
   */
  public static void updateTrackingConsent(Boolean consent) {
    read().ifPresent(data -> write(new SaveData(data.phase(), data.runId(), consent)));
  }

  /**
   * @param phase Act II, III or IV, restarted from its beginning
   * @param runId stable playthrough identifier
   * @param trackingConsent nullable decision, null while undecided
   */
  public record SaveData(ProgrammingPhase phase, UUID runId, Boolean trackingConsent) {
    public SaveData {
      if (phase == null
          || phase == ProgrammingPhase.VARIABLES
          || phase == ProgrammingPhase.COMPLETE
          || runId == null)
        throw new IllegalArgumentException("A checkpoint of Act II, III or IV is required.");
    }
  }
}
