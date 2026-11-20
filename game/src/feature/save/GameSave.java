package feature.save;

import engine.network.messages.s2c.ItemState;
import engine.utils.logging.DungeonLogger;
import feature.entities.CharacterClass;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Content of a save file of the general save system, see {@link GameSaves}.
 *
 * @param version file format version
 * @param runId identity of the playthrough, kept across continues
 * @param host name of the player that hosted the game, offered by "Continue", or null
 * @param activeMs active play time
 * @param level level the players were in
 * @param characters character class of every named player
 * @param flags existing game flags
 * @param players state of the players
 * @param prefabs state of the prefab instances of the level, by instance name
 * @param worldItems items lying in the level
 * @param levelState state of the level handler, or null if it has none
 */
public record GameSave(
    int version,
    UUID runId,
    String host,
    long activeMs,
    LevelSave level,
    Map<String, CharacterClass> characters,
    List<String> flags,
    List<PlayerSave> players,
    Map<String, JsonNode> prefabs,
    List<WorldItemSave> worldItems,
    JsonNode levelState) {

  /** Current file format version. */
  public static final int VERSION = 1;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameSave.class);
  private static final JsonMapper JSON =
      JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

  /**
   * Validates the save and replaces missing lists with empty ones.
   *
   * @param version file format version
   * @param runId identity of the playthrough
   * @param host name of the player that hosted the game, or null
   * @param activeMs active play time
   * @param level level the players were in
   * @param characters character class of every named player
   * @param flags existing game flags
   * @param players state of the players
   * @param prefabs state of the prefab instances of the level
   * @param worldItems items lying in the level
   * @param levelState state of the level handler, or null
   */
  public GameSave {
    if (version < 1 || version > VERSION) {
      throw new IllegalArgumentException("Unsupported save version " + version);
    }
    if (runId == null) throw new IllegalArgumentException("A save needs a run id");
    if (activeMs < 0) throw new IllegalArgumentException("Negative play time");
    if (level == null) throw new IllegalArgumentException("A save needs a level");
    characters = characters == null ? Map.of() : Map.copyOf(characters);
    flags = flags == null ? List.of() : List.copyOf(flags);
    players = players == null ? List.of() : List.copyOf(players);
    prefabs =
        prefabs == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(prefabs));
    worldItems = worldItems == null ? List.of() : List.copyOf(worldItems);
  }

  /**
   * Reads a save file.
   *
   * @param path save file
   * @return the save, or empty if the file is missing or invalid
   */
  static Optional<GameSave> read(Path path) {
    if (!Files.isRegularFile(path)) return Optional.empty();
    try {
      return Optional.of(JSON.readValue(Files.readString(path), GameSave.class));
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not read savegame " + path + ": " + exception.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Replaces a save file, atomically where the file system supports it.
   *
   * @param path save file
   * @throws IOException if the file could not be written
   */
  void write(Path path) throws IOException {
    Path absolute = path.toAbsolutePath();
    Path temporary = absolute.resolveSibling(absolute.getFileName() + ".tmp");
    Files.writeString(temporary, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    try {
      Files.move(
          temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException ignored) {
      Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Level the players were in.
   *
   * @param name registered level name
   * @param variant index of the level file variant, or null if it is unknown
   */
  public record LevelSave(String name, Integer variant) {

    /**
     * Validates the level.
     *
     * @param name registered level name
     * @param variant index of the level file variant, or null
     */
    public LevelSave {
      if (name == null || name.isBlank()) throw new IllegalArgumentException("Missing level name");
    }
  }

  /**
   * State of one player.
   *
   * @param name player name
   * @param x horizontal position, or null if the player was last seen in another level
   * @param y vertical position, or null if the player was last seen in another level
   * @param items inventory content
   */
  public record PlayerSave(String name, Float x, Float y, List<SlotSave> items) {

    /**
     * Validates the player.
     *
     * @param name player name
     * @param x horizontal position, or null
     * @param y vertical position, or null
     * @param items inventory content
     */
    public PlayerSave {
      if (name == null || name.isBlank()) throw new IllegalArgumentException("Missing player name");
      items = items == null ? List.of() : List.copyOf(items);
    }
  }

  /**
   * Item in an inventory slot.
   *
   * @param slot slot index
   * @param item the item
   */
  public record SlotSave(int slot, ItemState item) {

    /**
     * Validates the slot.
     *
     * @param slot slot index
     * @param item the item
     */
    public SlotSave {
      if (slot < 0 || item == null) throw new IllegalArgumentException("Invalid saved item slot");
    }
  }

  /**
   * Item lying in the level.
   *
   * @param item the item
   * @param x horizontal position
   * @param y vertical position
   */
  public record WorldItemSave(ItemState item, float x, float y) {

    /**
     * Validates the item.
     *
     * @param item the item
     * @param x horizontal position
     * @param y vertical position
     */
    public WorldItemSave {
      if (item == null) throw new IllegalArgumentException("Missing saved world item");
    }
  }
}
