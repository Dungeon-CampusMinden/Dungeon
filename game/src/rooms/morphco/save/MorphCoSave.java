package rooms.morphco.save;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.ServerProcess;
import engine.network.server.CharacterSelection;
import engine.utils.logging.DungeonLogger;
import feature.entities.CharacterClass;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.CircumfixPuzzle;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Server-authoritative checkpoint of the Morph & Co. room.
 *
 * <p>The server writes a checkpoint after every milestone. It contains the marking of the progress
 * net, the observation flags, the assembled circumfixes, the active play time, the run identity and
 * the tracking decision. Clients never write a savegame.
 */
public final class MorphCoSave {
  private static final Path PATH = Path.of("morph-co-save.json");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MorphCoSave.class);
  private static final JsonMapper JSON =
      JsonMapper.builder()
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
          .build();

  private MorphCoSave() {}

  /**
   * Reads the checkpoint.
   *
   * @return the valid checkpoint, or empty for a missing or malformed save
   */
  public static Optional<SaveData> read() {
    if (!Files.isRegularFile(PATH)) return Optional.empty();
    try {
      return Optional.ofNullable(JSON.readValue(Files.readString(PATH), SaveData.class));
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not read Morph & Co. savegame: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Checks whether the menu can offer to continue.
   *
   * @return whether a valid checkpoint exists
   */
  public static boolean exists() {
    return read().isPresent();
  }

  /**
   * Replaces the checkpoint atomically where the filesystem supports it.
   *
   * @param data checkpoint to write
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
      throw new IllegalStateException("Could not write Morph & Co. savegame.", exception);
    }
  }

  /** Removes the checkpoint for a new game or a completed room. */
  public static void delete() {
    try {
      Files.deleteIfExists(PATH);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not delete Morph & Co. savegame.", exception);
    }
  }

  /**
   * Updates the tracking decision of an existing checkpoint without creating a new one.
   *
   * @param consent nullable run-level tracking decision
   */
  public static void updateTrackingConsent(Boolean consent) {
    read()
        .ifPresent(
            data ->
                write(
                    new SaveData(
                        data.milestones(),
                        data.flags(),
                        data.assembled(),
                        data.leverPulls(),
                        data.affixAttempts(),
                        data.runId(),
                        data.activeMs(),
                        consent,
                        data.playerName(),
                        data.characterAssignments())));
  }

  /**
   * Captures the selected classes of all named players of this run.
   *
   * @return immutable character assignments
   */
  public static Map<String, CharacterClass> currentCharacterAssignments() {
    return CharacterSelection.assignments();
  }

  /**
   * Returns the name of the host of the run.
   *
   * <p>A server started by the host's client knows the host's name from its launch. A server
   * started on its own takes the first connected player: the players come from an unordered set,
   * and the host joined first and has the lowest entity ID.
   *
   * @return player name, or {@code null} before a player has joined a server started on its own
   */
  public static String currentPlayerName() {
    String hostName = System.getProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY);
    if (hostName != null && !hostName.isBlank()) return hostName;
    return Game.allPlayers()
        .sorted(Comparator.comparingInt(Entity::id))
        .flatMap(player -> player.fetch(PlayerComponent.class).stream())
        .map(PlayerComponent::playerName)
        .filter(name -> name != null && !name.isBlank())
        .findFirst()
        .orElse(null);
  }

  /**
   * A checkpoint.
   *
   * @param milestones completed milestones of the progress net
   * @param flags observation flags of the room state
   * @param assembled assembled circumfixes
   * @param leverPulls pulls on the stuck lever
   * @param affixAttempts checked answers of the affix step
   * @param runId stable playthrough identifier
   * @param activeMs active play time captured with this checkpoint
   * @param trackingConsent nullable tracking decision, null while undecided
   * @param playerName name of the host player, restored in the menu; may be null
   * @param characterAssignments named character classes reserved for this run
   */
  public record SaveData(
      Set<MorphCoMilestone> milestones,
      Set<String> flags,
      Set<CircumfixPuzzle.Circumfix> assembled,
      int leverPulls,
      int affixAttempts,
      UUID runId,
      long activeMs,
      Boolean trackingConsent,
      String playerName,
      Map<String, CharacterClass> characterAssignments) {

    /**
     * Validates the checkpoint and copies its collections.
     *
     * @param milestones completed milestones of the progress net
     * @param flags observation flags of the room state
     * @param assembled assembled circumfixes
     * @param leverPulls pulls on the stuck lever
     * @param affixAttempts checked answers of the affix step
     * @param runId stable playthrough identifier
     * @param activeMs active play time captured with this checkpoint
     * @param trackingConsent nullable tracking decision, null while undecided
     * @param playerName name of the host player, restored in the menu; may be null
     * @param characterAssignments named character classes reserved for this run
     */
    public SaveData {
      if (milestones == null || flags == null || assembled == null || runId == null) {
        throw new IllegalArgumentException("Incomplete Morph & Co. checkpoint.");
      }
      if (activeMs < 0) throw new IllegalArgumentException("Negative active play time");
      if (milestones.contains(MorphCoMilestone.PRODUCTION_RESTARTED)) {
        throw new IllegalArgumentException("A finished run cannot be continued.");
      }
      for (MorphCoMilestone milestone : milestones) {
        if (milestone == null || !milestones.containsAll(milestone.prerequisites())) {
          throw new IllegalArgumentException("Inconsistent milestones in checkpoint.");
        }
      }
      milestones = Set.copyOf(milestones);
      flags = Set.copyOf(flags);
      assembled = Set.copyOf(assembled);
      if (characterAssignments == null) characterAssignments = Map.of();
      Map<String, CharacterClass> copy = new LinkedHashMap<>();
      characterAssignments.forEach(
          (name, characterClass) -> {
            if (name == null || name.isBlank() || name.contains("_") || characterClass == null) {
              throw new IllegalArgumentException("Invalid saved character assignment.");
            }
            copy.put(name, characterClass);
          });
      characterAssignments = Map.copyOf(copy);
    }
  }
}
