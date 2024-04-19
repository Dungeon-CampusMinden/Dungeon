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
import core.level.elements.tile.PitTile;
import core.level.generator.IGenerator;
import core.level.utils.*;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.path.IPath;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the dungeon game world.
 *
 * <p>The system will store the currently active level.
 *
 * <p>Each frame, this system will draw the level on the screen. The system will also check if one
 * of the entities managed by this system is positioned on the end tile of the level. If so, the
 * next level will be loaded.
 *
 * <p>The system uses the configured {@link IGenerator} to generate levels in the configured {@link
 * LevelSize}. Use {@link #generator(IGenerator)} to change the used level generator. Use {@link
 * #levelSize(LevelSize)} to set the size of the next levels that get loaded.
 *
 * <p>If a new level is loaded, the system will trigger the onLevelLoad callback given in the
 * constructor of this system.
 *
 * <p>An entity needs a {@link PositionComponent} and a {@link PlayerComponent} to be managed by
 * this system.
 *
 * <p>Use {@link #level()} to get the currently active level. Use {@link #loadLevel(ILevel)}, {@link
 * #loadLevel(LevelSize, DesignLabel)}, {@link #loadLevel(LevelSize)}, or {@link
 * #loadLevel(DesignLabel)} to trigger a level load manually. These methods will also trigger the
 * onLevelLoad callback.
 */
public final class LevelSystem extends System {
  /** offset the coordinate by half a tile, it makes every Entity not walk on the sidewalls. */
  private static final float X_OFFSET = 0.5f;

  /**
   * offset the coordinate by a quarter tile,it looks a bit more like every Entity is not walking
   * over walls.
   */
  private static final float Y_OFFSET = 0.25f;

  private static final String SOUND_EFFECT = "sounds/enterDoor.wav";

  /** Currently used level-size configuration for generating new level. */
  private static LevelSize levelSize = LevelSize.MEDIUM;

  private static ILevel currentLevel;
  private final List<ILevelObserver> observers = new ArrayList<>();
  private final IVoidFunction onLevelLoad;
  private final Painter painter;
  private final Logger levelAPI_logger = Logger.getLogger(this.getClass().getSimpleName());
  private IVoidFunction onEndTile;
  private IGenerator generator;
  private Coordinate lastHeroPosition;

  /**
   * Create a new {@link LevelSystem}.
   *
   * <p>The system will not load a new level at creation. Use {@link #loadLevel(LevelSize,
   * DesignLabel)} if you want to trigger the load of a level manually; otherwise, the first level
   * will be loaded if this system's {@link #execute()} is executed.
   *
   * @param painter The {@link Painter} to use to draw the level.
   * @param generator Level generator to use to generate the level.
   * @param onLevelLoad Callback function that is called if a new level was loaded.
   */
  public LevelSystem(Painter painter, IGenerator generator, IVoidFunction onLevelLoad) {
    super(PlayerComponent.class, PositionComponent.class);
    this.generator = generator;
    this.onLevelLoad = onLevelLoad;
    this.painter = painter;
    this.onEndTile = () -> loadLevel(levelSize);
  }

  /**
   * Get the currently loaded level.
   *
   * @return The currently loaded level.
   */
  public static ILevel level() {
    return currentLevel;
  }

  /**
   * Get the configuration size that is set to generate the next level.
   *
   * @return Size of the next levels that are generated.
   */
  public static LevelSize levelSize() {
    return levelSize;
  }

  /**
   * Set the configuration size that is used to generate the next level.
   *
   * @param levelSize The new configuration size for level generation.
   */
  public static void levelSize(final LevelSize levelSize) {
    LevelSystem.levelSize = levelSize;
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
   * Load a new level.
   *
   * <p>Will trigger the onLevelLoad callback.
   *
   * @param size The wanted size of the new level.
   * @param label The wanted design of the new level.
   */
  public void loadLevel(final LevelSize size, final DesignLabel label) {
    currentLevel = generator.level(label, size);
    onLevelLoad.execute();
    levelAPI_logger.info("A new level was loaded.");
  }

  /**
   * Load a new level with the configured size and the given design.
   *
   * <p>Will trigger the onLevelLoad callback.
   *
   * @param designLabel Wanted level design.
   */
  public void loadLevel(final DesignLabel designLabel) {
    loadLevel(levelSize, designLabel);
  }

  /**
   * Load a new level with the given size and a random design. *
   *
   * <p>Will trigger the onLevelLoad callback.
   *
   * @param size Wanted size of the level.
   */
  public void loadLevel(final LevelSize size) {
    loadLevel(size, DesignLabel.randomDesign());
  }

  /**
   * Load a new level with the configured size and random design. *
   *
   * <p>Will trigger the onLevelLoad callback.
   */
  public void loadLevel() {
    loadLevel(levelSize(), DesignLabel.randomDesign());
  }

  private void drawLevel() {
    Map<IPath, PainterConfig> mapping = new HashMap<>();

    Tile[][] layout = currentLevel.layout();
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        Tile t = tiles[x];
        if (t.levelElement() != LevelElement.SKIP && !isTilePitAndOpen(t) && t.visible()) {
          IPath texturePath = t.texturePath();
          if (!mapping.containsKey(texturePath)
              || (mapping.get(texturePath).tintColor() != t.tintColor())) {
            mapping.put(
                texturePath, new PainterConfig(texturePath, X_OFFSET, Y_OFFSET, t.tintColor()));
          }
          painter.draw(t.position(), texturePath, mapping.get(texturePath));
        }
      }
    }
  }

  /**
   * Checks if the provided tile is an instance of PitTile and if it's open.
   *
   * @param tile The tile to check.
   * @return true if the tile is an instance of PitTile, and it's open, false otherwise.
   */
  private boolean isTilePitAndOpen(final Tile tile) {
    if (tile instanceof PitTile) {
      return ((PitTile) tile).isOpen();
    } else {
      return false;
    }
  }

  /**
   * Get the currently used level generator.
   *
   * @return The currently used level generator.
   */
  public IGenerator generator() {
    return generator;
  }

  /**
   * Set the level generator.
   *
   * @param generator The new level generator.
   */
  public void generator(final IGenerator generator) {
    this.generator = generator;
  }

  /**
   * Check if the given entity is on the end tile.
   *
   * @param entity The entity for which the position is checked.
   * @return True if the entity is on the end tile, else false.
   */
  private boolean isOnEndTile(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Tile currentTile = Game.tileAT(pc.position());
    if (currentTile == null) {
      return false;
    }
    return currentTile.equals(Game.endTile());
  }

  private Optional<ILevel> isOnDoor(final Entity entity) {
    ILevel nextLevel = null;
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    for (DoorTile door : currentLevel.doorTiles()) {
      if (door.isOpen()
          && door.otherDoor() != null
          && door.otherDoor().isOpen()
          && door.equals(Game.tileAT(pc.position()))) {
        door.otherDoor().level().startTile(door.otherDoor().doorstep());
        nextLevel = door.otherDoor().level();
      }
    }
    return Optional.ofNullable(nextLevel);
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
      loadLevel(levelSize);
    } else if (entityStream()
        .anyMatch(
            entity ->
                isOnEndTile(entity)
                    && Game.currentLevel().endTile() instanceof ExitTile
                    && ((ExitTile) Game.currentLevel().endTile()).isOpen())) onEndTile.execute();
    else
      entityStream()
          .forEach(
              e -> {
                if (this.lastHeroPosition == null) {
                  this.lastHeroPosition =
                      new Coordinate(
                          e.fetch(PositionComponent.class).get().position().toCoordinate());
                } else {
                  Coordinate currentHeroPosition =
                      new Coordinate(
                          e.fetch(PositionComponent.class).get().position().toCoordinate());
                  if (!this.lastHeroPosition.equals(currentHeroPosition)) {
                    this.lastHeroPosition = currentHeroPosition;
                    this.observers.forEach(
                        o ->
                            o.onLevelEvent(
                                Game.tileAT(currentHeroPosition),
                                ILevelObserver.LevelEvent.HERO_TILE_CHANGED));
                  }
                }
                isOnDoor(e)
                    .ifPresent(
                        iLevel -> {
                          loadLevel(iLevel);
                          playSound();
                        });
              });
    drawLevel();
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

  /**
   * Registers an observer to the LevelSystem.
   *
   * <p>This method adds an observer to the list of observers that are notified of level events. The
   * observer must implement the ILevelObserver interface.
   *
   * @param observer The observer to be registered.
   * @see ILevelObserver
   */
  public void registerObserver(ILevelObserver observer) {
    this.observers.add(observer);
  }

  /**
   * Removes an observer from the LevelSystem.
   *
   * <p>This method removes an observer from the list of observers that are notified of level
   * events. If the observer is not in the list, the method has no effect.
   *
   * @param observer The observer to be removed.
   * @see ILevelObserver
   */
  public void removeObserver(ILevelObserver observer) {
    this.observers.remove(observer);
  }

  /** LevelSystem can't be paused. If it is paused, the level will not be shown anymore. */
  @Override
  public void stop() {
    run = true;
  }
}
