package rooms.soulweaver.save;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import rooms.soulweaver.state.SoulweaverPhase;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Stores the act checkpoint, active play time, run identity and tracking decision. */
public final class SoulweaverSave {
  private static final Path PATH = Path.of("soulweaver-save.json");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SoulweaverSave.class);
  private static final JsonMapper JSON =
      JsonMapper.builder()
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
          .build();

  private SoulweaverSave() {}

  /**
   * @return the valid checkpoint, or empty for a missing or malformed save
   */
  public static Optional<SaveData> read() {
    if (!Files.isRegularFile(PATH)) return Optional.empty();
    try {
      return Optional.ofNullable(JSON.readValue(Files.readString(PATH), SaveData.class));
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not read Soulweaver savegame: {}", exception.getMessage());
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
      throw new IllegalStateException("Could not write Soulweaver savegame.", exception);
    }
  }

  /** Removes the checkpoint for a new game or a completed room. */
  public static void delete() {
    try {
      Files.deleteIfExists(PATH);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not delete Soulweaver savegame.", exception);
    }
  }

  /**
   * Updates an existing checkpoint without creating a new one.
   *
   * @param consent nullable run-level tracking decision
   */
  public static void updateTrackingConsent(Boolean consent) {
    read()
        .ifPresent(
            data -> write(new SaveData(data.phase(), data.runId(), data.activeMs(), consent)));
  }

  /**
   * @param phase Act II, III or IV, restarted from its beginning
   * @param runId stable playthrough identifier
   * @param activeMs active play time captured with this checkpoint
   * @param trackingConsent nullable decision, null while undecided
   */
  public record SaveData(
      SoulweaverPhase phase, UUID runId, long activeMs, Boolean trackingConsent) {
    /**
     * Validates that the checkpoint identifies a resumable act and an existing run.
     *
     * @param phase act to restart
     * @param runId playthrough identifier
     * @param activeMs active play time captured with this checkpoint
     * @param trackingConsent nullable consent decision
     */
    public SaveData {
      if (activeMs < 0) throw new IllegalArgumentException("Negative active play time");
      if (phase == null
          || phase == SoulweaverPhase.VARIABLES
          || phase == SoulweaverPhase.COMPLETE
          || runId == null)
        throw new IllegalArgumentException("A checkpoint of Act II, III or IV is required.");
    }
  }
}
