package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.loader.DungeonLoader;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import java.util.*;
import java.util.logging.Logger;

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
  private static final String SOUND_EFFECT = "sounds/enterDoor.wav";

  private static ILevel currentLevel;
  private final IVoidFunction onLevelLoad;
  private final Logger levelAPI_logger = Logger.getLogger(this.getClass().getSimpleName());
  private IVoidFunction onEndTile;

  /**
   * Create a new {@link LevelSystem}.
   *
   * <p>The system will not load a new level at creation. Use {@link #loadLevel(ILevel)} if you want
   * to trigger the load of a level manually; otherwise, the first level will be loaded if this
   * system's {@link #execute()} is executed.
   *
   * @param onLevelLoad Callback function that is called if a new level was loaded.
   */
  public LevelSystem(IVoidFunction onLevelLoad) {
    super(PlayerComponent.class, PositionComponent.class);
    this.onLevelLoad = onLevelLoad;
    this.onEndTile = () -> DungeonLoader.loadNextLevel();
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
    levelAPI_logger.info("A new level was loaded.");
  }

  /**
   * Check if the given entity is on the end tile.
   *
   * @param entity The entity for which the position is checked.
   * @return True if the entity is on the end tile, else false.
   */
  private boolean isOnOpenEndTile(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Tile currentTile = Game.tileAt(pc.position()).orElse(null);
    if (currentTile == null) {
      return false;
    }

    if (currentTile instanceof ExitTile endTile && endTile.isOpen()) return true;

    return false;
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

    doorTile.otherDoor().level().startTile(doorTile.otherDoor().doorstep());
    return Optional.ofNullable(doorTile.otherDoor().level());
  }

  private void playSound() {
    Sound doorSound = Gdx.audio.newSound(Gdx.files.internal(SOUND_EFFECT));
    long soundId = doorSound.play();
    doorSound.setLooping(soundId, false);
    doorSound.setVolume(soundId, 0.3f);
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
        levelAPI_logger.warning(
            "Can´t load level 0, because no level is added to the DungeonLoader.");
      }
    } else {
      if (filteredEntityStream(PlayerComponent.class, PositionComponent.class)
          .anyMatch(this::isOnOpenEndTile)) onEndTile.execute();
      else
        filteredEntityStream()
            .forEach(
                e -> {
                  isOnDoor(e)
                      .ifPresent(
                          iLevel -> {
                            loadLevel(iLevel);
                            playSound();
                          });
                });
    }
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
