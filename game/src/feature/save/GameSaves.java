package feature.save;

import com.badlogic.gdx.utils.TimeUtils;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.game.PreRunConfiguration;
import engine.game.ServerProcess;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.level.loader.DungeonLoader;
import engine.network.ConnectionListener;
import engine.network.messages.s2c.ItemState;
import engine.network.server.CharacterSelection;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import feature.entities.WorldItemBuilder;
import feature.inventory.Item;
import feature.prefabs.GameFlags;
import feature.prefabs.PrefabSaveStates;
import feature.save.GameSave.LevelSave;
import feature.save.GameSave.PlayerSave;
import feature.save.GameSave.SlotSave;
import feature.save.GameSave.WorldItemSave;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

/**
 * General save system: keeps the state of a running game in a save file and continues from it.
 *
 * <p>A game enables it with {@code GameStarter.Builder#saveGame}, which offers "Continue" in the
 * main menu while a save exists. The authoritative server saves:
 *
 * <ul>
 *   <li>the level, play time and the character class of every player,
 *   <li>the {@link GameFlags game flags},
 *   <li>position and inventory of every player, restored when a player with the same name joins,
 *   <li>the state players gave to prefabs, see {@link feature.prefabs.Prefab#captureState},
 *   <li>items lying in the level,
 *   <li>the state of the level handler, see {@link DungeonLevel#saveState}.
 * </ul>
 *
 * <p>Like the hand-crafted rooms, the game saves itself when it progresses: after a game flag
 * changed, after the players reached another level, and when a level handler asks for it with
 * {@link #requestSave()}.
 *
 * <p>When a save is continued, the level is restored in this order: prefabs spawn in their saved
 * state, the saved items are placed, the level handler restores its state after {@link
 * DungeonLevel#onFirstTick()}, and finally the game flags are restored, so flag triggers that fire
 * on restore can rebuild what links did during play, e.g. open a door.
 */
public final class GameSaves {

  /** Server argument that starts a new game and deletes the old save. */
  public static final String NEW_GAME_ARGUMENT = "--new-game";

  /** Server argument that continues from the save. */
  public static final String LOAD_ARGUMENT = "--load-game";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameSaves.class);
  // Shortest time between two saves, so quickly repeated changes do not write on every tick.
  private static final long MIN_SAVE_INTERVAL_MS = 1000;
  // Players of the loaded save that did not join yet, by name.
  private static final Map<String, PlayerSave> PENDING_PLAYERS = new LinkedHashMap<>();

  // Save file of this server, null while the save system is disabled.
  private static Path path;
  // Save to continue from, until its level is restored.
  private static GameSave loaded;
  // Level that is being restored from the loaded save.
  private static ILevel restoring;
  private static UUID runId = UUID.randomUUID();
  // Host of the loaded save, kept if the host of this server is unknown.
  private static String host;
  private static boolean saveRequested;
  private static long savedFlagRevision;
  private static int levelsEntered;
  private static int revision;
  private static long lastSaveAt;
  private static boolean finished;

  private GameSaves() {}

  /**
   * Returns the save file of a game.
   *
   * @param id identifier of the game
   * @return path of the save file
   */
  public static Path path(String id) {
    return Path.of(id + "-save.json");
  }

  /**
   * Returns whether a game has a save to continue from.
   *
   * @param id identifier of the game
   * @return true if a valid save file exists
   */
  public static boolean exists(String id) {
    return GameSave.read(path(id)).isPresent();
  }

  /**
   * Fills in the name of the player that hosted the saved game, so "Continue" restores that player.
   * Called by the main menu.
   *
   * @param id identifier of the game
   */
  public static void prefillPlayerName(String id) {
    GameSave.read(path(id))
        .map(GameSave::host)
        .filter(name -> !name.isBlank())
        .ifPresent(
            name -> {
              try {
                PreRunConfiguration.username(name);
              } catch (IllegalArgumentException invalidName) {
                // The player can still type the name.
              }
            });
  }

  /**
   * Enables the save system on an authoritative server. Must run before the game starts.
   *
   * <p>With {@link #LOAD_ARGUMENT}, the server continues from the save: play time and character
   * classes are restored right away, the rest when the saved level loads. With {@link
   * #NEW_GAME_ARGUMENT}, the old save is deleted.
   *
   * @param id identifier of the game
   * @param args server arguments
   */
  public static void startServer(String id, String[] args) {
    path = path(id);
    if (contains(args, NEW_GAME_ARGUMENT)) deleteFile();
    if (!contains(args, LOAD_ARGUMENT)) return;
    loaded = GameSave.read(path).orElse(null);
    if (loaded == null) {
      LOGGER.warn("No valid savegame to continue from, starting a new game");
      return;
    }
    runId = loaded.runId();
    host = loaded.host();
    loaded.players().forEach(player -> PENDING_PLAYERS.put(player.name(), player));
    try {
      Game.playClock().restore(loaded.activeMs());
      CharacterSelection.restoreAssignments(loaded.characters());
    } catch (RuntimeException exception) {
      LOGGER.warn("Could not restore play time or characters of the savegame", exception);
    }
  }

  /**
   * Returns whether this process saves the game.
   *
   * @return true on an authoritative server with the save system enabled
   */
  public static boolean enabled() {
    return path != null;
  }

  /**
   * Registers the server systems of the save system, or the save confirmation on clients. Called
   * once during game setup.
   */
  public static void onSetup() {
    if (enabled()) ECSManagement.add(new GameSaveSystem());
    if (Game.isMultiplayerClient()) {
      Game.network()
          .addConnectionListener(
              new ConnectionListener() {
                @Override
                public void onConnected() {
                  SaveSync.reset();
                }

                @Override
                public void onDisconnected(String reason) {}
              });
    }
  }

  /**
   * Loads the level of the save to continue from, instead of the first level.
   *
   * @return true if the saved level was loaded
   */
  public static boolean loadSavedLevel() {
    if (!enabled() || loaded == null) return false;
    LevelSave level = loaded.level();
    try {
      if (level.variant() == null) DungeonLoader.loadLevel(level.name());
      else DungeonLoader.loadLevel(level.name(), level.variant());
      return true;
    } catch (RuntimeException exception) {
      LOGGER.warn("Could not load the saved level '" + level.name() + "'", exception);
      abandonLoadedSave();
      return false;
    }
  }

  /**
   * Prepares the saved prefab states before the server-side prefabs of a level spawn.
   *
   * @param level level whose prefabs spawn
   */
  public static void beforePrefabSpawn(ILevel level) {
    if (!enabled() || loaded == null || restoring != null) return;
    String name = currentLevelName().orElse(null);
    if (!loaded.level().name().equals(name)) {
      LOGGER.warn(
          "The savegame belongs to level '" + loaded.level().name() + "', starting a new game");
      abandonLoadedSave();
      return;
    }
    restoring = level;
    PrefabSaveStates.restore(level, loaded.prefabs());
  }

  /**
   * Places the saved items after the server-side prefabs of a level spawned.
   *
   * @param level level whose prefabs spawned
   */
  public static void afterPrefabSpawn(ILevel level) {
    if (level != restoring || loaded == null) return;
    for (WorldItemSave saved : loaded.worldItems()) {
      try {
        Game.add(
            WorldItemBuilder.buildWorldItem(
                saved.item().toItem(), new Point(saved.x(), saved.y())));
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not restore the saved item " + saved.item().itemType(), exception);
      }
    }
  }

  /**
   * Finishes the restore of a level, or saves after the players reached another level. Called right
   * after the first tick of every level.
   *
   * @param level level that ticked for the first time
   */
  public static void afterFirstTick(ILevel level) {
    if (!enabled()) return;
    levelsEntered++;
    if (level != restoring) {
      // The first level of a new game has no progress to save yet.
      if (levelsEntered > 1) {
        // Saved positions of players who did not come back belong to the previous level.
        PENDING_PLAYERS.replaceAll(
            (name, player) -> new PlayerSave(name, null, null, player.items()));
        requestSave();
      }
      return;
    }
    try {
      if (loaded.levelState() != null && level instanceof DungeonLevel dungeonLevel) {
        dungeonLevel.loadState(loaded.levelState());
      }
    } catch (RuntimeException exception) {
      LOGGER.error("Could not restore the level state of the savegame", exception);
    }
    try {
      // Flags last: flag triggers that fire on restore see everything else in place.
      GameFlags.restore(loaded.flags());
    } catch (RuntimeException exception) {
      LOGGER.error("Could not restore the flags of the savegame", exception);
    } finally {
      PrefabSaveStates.finishRestore(level);
      restoring = null;
      loaded = null;
      savedFlagRevision = GameFlags.revision();
    }
  }

  /**
   * Asks for a save at the end of the current tick, e.g. when a hand-crafted room reached a story
   * step. Does nothing if the save system is disabled.
   */
  public static void requestSave() {
    saveRequested = true;
  }

  /**
   * Deletes the save, e.g. when the players finished the game, and stops saving for the rest of the
   * session.
   */
  public static void delete() {
    if (!enabled()) return;
    finished = true;
    deleteFile();
  }

  /**
   * Returns how often the game was saved in this session, so clients can confirm saves.
   *
   * @return number of written saves
   */
  public static int revision() {
    return revision;
  }

  /**
   * Saves the game if it progressed since the last save, at most once per {@link
   * #MIN_SAVE_INTERVAL_MS}. Called once per server tick.
   */
  static void saveIfProgressed() {
    if (!enabled() || finished || loaded != null || restoring != null) return;
    if (!saveRequested && GameFlags.revision() == savedFlagRevision) return;
    long now = TimeUtils.millis();
    if (now - lastSaveAt < MIN_SAVE_INTERVAL_MS) return;
    lastSaveAt = now;
    saveRequested = false;
    savedFlagRevision = GameFlags.revision();
    if (!(Game.currentLevel().orElse(null) instanceof DungeonLevel level)) return;
    try {
      capture(level).write(path);
      revision++;
    } catch (IOException | RuntimeException exception) {
      LOGGER.error("Could not save the game", exception);
    }
  }

  private static GameSave capture(DungeonLevel level) {
    OptionalInt variant = DungeonLoader.currentVariantIndex();
    return new GameSave(
        GameSave.VERSION,
        runId,
        hostName(),
        Game.playClock().activeMs(),
        new LevelSave(
            currentLevelName().orElseThrow(() -> new IllegalStateException("No level to save")),
            variant.isPresent() ? variant.getAsInt() : null),
        CharacterSelection.assignments(),
        GameFlags.snapshot(),
        capturePlayers(),
        PrefabSaveStates.capture(level),
        captureWorldItems(level),
        level.saveState().orElse(null));
  }

  private static List<PlayerSave> capturePlayers() {
    // Saved players who did not come back yet keep their state.
    Map<String, PlayerSave> players = new LinkedHashMap<>(PENDING_PLAYERS);
    Game.allPlayers().forEach(player -> capture(player).ifPresent(p -> players.put(p.name(), p)));
    return List.copyOf(players.values());
  }

  private static Optional<PlayerSave> capture(Entity player) {
    String name = player.fetch(PlayerComponent.class).map(PlayerComponent::playerName).orElse(null);
    PositionComponent position = player.fetch(PositionComponent.class).orElse(null);
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (name == null
        || name.isBlank()
        || position == null
        || PositionComponent.ILLEGAL_POSITION.equals(position.position())
        || inventory == null) {
      return Optional.empty();
    }
    List<SlotSave> items = new ArrayList<>();
    Item[] slots = inventory.items();
    for (int slot = 0; slot < slots.length; slot++) {
      if (slots[slot] == null) continue;
      int index = slot;
      itemState(slots[slot]).ifPresent(item -> items.add(new SlotSave(index, item)));
    }
    Point at = position.position();
    return Optional.of(new PlayerSave(name, at.x(), at.y(), items));
  }

  private static List<WorldItemSave> captureWorldItems(DungeonLevel level) {
    // Items of prefabs, e.g. those an item spawner placed, are saved with their prefab.
    Set<Entity> claimed = PrefabSaveStates.claimedEntities(level);
    List<WorldItemSave> items = new ArrayList<>();
    Game.entities()
        .filter(entity -> entity.isPresent(ItemComponent.class) && !claimed.contains(entity))
        .forEach(
            entity -> {
              PositionComponent position = entity.fetch(PositionComponent.class).orElse(null);
              if (position == null
                  || PositionComponent.ILLEGAL_POSITION.equals(position.position())) {
                return;
              }
              Point at = position.position();
              itemState(entity.fetch(ItemComponent.class).orElseThrow().item())
                  .ifPresent(item -> items.add(new WorldItemSave(item, at.x(), at.y())));
            });
    return items;
  }

  /**
   * Restores position and inventory of a joined player from the loaded save.
   *
   * @param player joined player
   * @return false if the player is not set up yet and has to be restored later
   */
  static boolean restorePlayer(Entity player) {
    String name = player.fetch(PlayerComponent.class).map(PlayerComponent::playerName).orElse(null);
    if (name == null) return true;
    PlayerSave saved = PENDING_PLAYERS.get(name);
    if (saved == null) return true;
    PositionComponent position = player.fetch(PositionComponent.class).orElse(null);
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (position == null || inventory == null) return false;
    if (saved.x() != null && saved.y() != null) {
      position.position(new Point(saved.x(), saved.y()));
    }
    int capacity = inventory.items().length;
    for (int slot = 0; slot < capacity; slot++) inventory.set(slot, null);
    for (SlotSave slot : saved.items()) {
      try {
        Item item = slot.item().toItem();
        if (slot.slot() >= capacity || !inventory.set(slot.slot(), item)) inventory.add(item);
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not restore the saved item " + slot.item().itemType(), exception);
      }
    }
    PENDING_PLAYERS.remove(name);
    return true;
  }

  private static Optional<ItemState> itemState(Item item) {
    try {
      return Optional.of(ItemState.fromItem(item));
    } catch (RuntimeException exception) {
      LOGGER.warn("Could not save the item " + item.displayName(), exception);
      return Optional.empty();
    }
  }

  private static String hostName() {
    String hosting = System.getProperty(ServerProcess.HOST_PLAYER_NAME_PROPERTY);
    if (hosting != null && !hosting.isBlank()) return hosting;
    if (host != null) return host;
    return Game.allPlayers()
        .flatMap(player -> player.fetch(PlayerComponent.class).stream())
        .map(PlayerComponent::playerName)
        .findFirst()
        .orElse(null);
  }

  private static Optional<String> currentLevelName() {
    try {
      return Optional.of(DungeonLoader.currentLevel());
    } catch (IndexOutOfBoundsException noRegisteredLevel) {
      return Optional.empty();
    }
  }

  private static void abandonLoadedSave() {
    loaded = null;
    PENDING_PLAYERS.clear();
    // The game starts over, without the play time and characters of the unusable save.
    runId = UUID.randomUUID();
    CharacterSelection.clear();
    try {
      Game.playClock().restore(0);
    } catch (IllegalStateException alreadyStarted) {
      LOGGER.warn("Could not reset the play time of the unusable savegame");
    }
  }

  private static void deleteFile() {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      LOGGER.warn("Could not delete savegame " + path, exception);
    }
  }

  private static boolean contains(String[] args, String expected) {
    if (args == null) return false;
    for (String arg : args) if (expected.equals(arg)) return true;
    return false;
  }
}
