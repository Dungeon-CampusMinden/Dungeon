package core.systems;

import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.loader.DungeonLoader;
import core.sound.SoundSpec;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Optional;

/**
 * Manages the dungeon game world.
 *
 * <p>The system will store the currently active level.
 *
 * <p>The system will check if one of the entities managed by this system is positioned on the end
 * tile of the level. If so, the next level will be loaded.
 *
 * <p>The system uses the {@link DungeonLoader} to load levels. Use {@link
 * DungeonLoader#addLevel(Tuple[])} to add a level to the DungeonLoader.
 *
 * <p>If a new level is loaded, the system will trigger the onLevelLoad callback given in the
 * constructor of this system.
 *
 * <p>An entity needs a {@link PositionComponent} and a {@link PlayerComponent} to be managed by
 * this system.
 *
 * <p>Use {@link #level()} to get the currently active level. Use {@link #loadLevel(ILevel)}, to
 * trigger a level load manually. These methods will also trigger the onLevelLoad callback.
 */
public final class LevelSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LevelSystem.class);

  private static final String SOUND_EFFECT = "enterDoor";

  private static ILevel currentLevel;
  private IVoidFunction onLevelLoad = () -> {};
  private IVoidFunction onEndTile;

  /**
   * Create a new {@link LevelSystem}.
   *
   * <p>The system will not load a new level at creation. Use {@link #loadLevel(ILevel)} if you want
   * to trigger the load of a level manually; otherwise, the first level will be loaded if this
   * system's {@link #execute()} is executed.
   */
  public LevelSystem() {
    super(AuthoritativeSide.BOTH, PlayerComponent.class, PositionComponent.class);
    this.onEndTile = DungeonLoader::loadNextLevel;
  }

  /**
   * Get the currently loaded level.
   *
   * @return The currently loaded level or an empty Optional if there is no level.
   */
  public static Optional<ILevel> level() {
    return Optional.ofNullable(currentLevel);
  }

  /**
   * Set the current level to the given level.
   *
   * <p>Will trigger the onLevelLoad callback.
   *
   * @param level The level to be set.
   */
  public void loadLevel(final ILevel level) {
    currentLevel = level;
    onLevelLoad.execute();
    LOGGER.info("A new level was loaded.");
  }

  /**
   * Set the function to be executed when a new level is loaded.
   *
   * @param onLevelLoad The function to be executed when a new level is loaded.
   */
  public void onLevelLoad(final IVoidFunction onLevelLoad) {
    this.onLevelLoad = onLevelLoad;
  }

  /**
   * Check if the given entity is on the end tile.
   *
   * @param entity The entity for which the position is checked.
   * @return True if the entity is on the end tile, else false.
   */
  private boolean isOnOpenEndTile(final Entity entity) {
    Tile currentTile = Game.tileAt(EntityUtils.getPosition(entity)).orElse(null);
    if (currentTile == null) {
      return false;
    }

    return currentTile instanceof ExitTile endTile && endTile.isOpen();
  }

  private Optional<ILevel> isOnDoor(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Tile currentTile = Game.tileAt(pc.position()).orElse(null);

    if (!(currentTile instanceof DoorTile doorTile)) {
      return Optional.empty();
    }
    if (!doorTile.isOpen() || doorTile.otherDoor() == null || !doorTile.otherDoor().isOpen()) {
      return Optional.empty();
    }

    List<Tile> startTiles = doorTile.otherDoor().level().startTiles();
    Tile doorstep = doorTile.otherDoor().doorstep();
    if (startTiles.isEmpty()) startTiles.add(doorstep);
    startTiles.set(0, doorstep);

    return Optional.ofNullable(doorTile.otherDoor().level());
  }

  private void playSound() {
    Game.audio().playGlobal(SoundSpec.builder(SOUND_EFFECT).volume(0.3f));
  }

  /**
   * Execute the system logic.
   *
   * <p>Will load a new level if no level exists or one of the managed entities are on the end tile.
   *
   * <p>Will draw the level.
   */
  @Override
  public void execute() {
    if (currentLevel == null) {
      try {
        DungeonLoader.loadLevel(0);
        execute();
      } catch (IndexOutOfBoundsException e) {
        LOGGER.warn("Can´t load level 0, because no level is added to the DungeonLoader.");
      }
    } else {
      if (Game.allPlayers().findAny().isEmpty()) return;

      // Load next level if all heroes are on the end tile
      if (Game.allPlayers().allMatch(this::isOnOpenEndTile)) {
        onEndTile.execute();
        return;
      }

      // Check if all heroes are on the same open door and load that level
      List<ILevel> doorLevels =
          Game.allPlayers().map(this::isOnDoor).flatMap(Optional::stream).distinct().toList();

      if (doorLevels.size() == 1) {
        loadLevel(doorLevels.get(0));
        playSound();
      }
    }

    openPits();
  }

  private void openPits() {
    level()
        .ifPresent(
            level -> {
              Tile[][] layout = level.layout();
              for (int y = layout.length - 1; y >= 0; y--) {
                for (int x = 0; x < layout[0].length; x++) {
                  if (layout[y][x] instanceof PitTile pit) {
                    if (pit.timeToOpen() <= 0) pit.open();
                  }
                }
              }
            });
  }

  /** LevelSystem can't be paused. If it is paused, the level will not be shown anymore. */
  @Override
  public void stop() {
    run = true;
  }

  /**
   * Sets the function to be executed when an entity reaches the end tile.
   *
   * @param onEndTile The function to be executed when an entity reaches the end tile.
   */
  public void onEndTile(IVoidFunction onEndTile) {
    this.onEndTile = onEndTile;
  }

  /**
   * Gets the function that is executed when an entity reaches the end tile.
   *
   * @return The function that is executed when an entity reaches the end tile.
   */
  public IVoidFunction onEndTile() {
    return onEndTile;
  }
}
