package core;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.components.PositionComponent;
import core.game.ECSManagment;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.network.DefaultSnapshotTranslator;
import core.network.NetworkException;
import core.network.handler.INetworkHandler;
import core.network.handler.LocalNetworkHandler;
import core.network.handler.NettyNetworkHandler;
import core.systems.LevelSystem;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Center-Point of the framework.
 *
 * <p>This class is basically an API-Class, it will forward the request to the responsible classes.
 *
 * <p>Use {@link #run()} to start the game.
 *
 * <p>For Entity management use: {@link #add(Entity)}, {@link #remove(Entity)} or {@link
 * #removeAllEntities()}
 *
 * <p>Use {@link #userOnFrame(IVoidFunction)}, {@link #userOnSetup(IVoidFunction)}, and {@link
 * #userOnLevelLoad(Consumer)} to configure event callbacks. This is the best way to include your
 * own program logic outside a {@link java.lang.System}.
 *
 * <p>For System management use: {@link #add(System)}, {@link #remove(Class)} or {@link
 * #removeAllSystems()}
 *
 * <p>Get access via: {@link #levelEntities()}, {@link #systems()}
 *
 * @see PreRunConfiguration
 * @see ECSManagment
 * @see GameLoop
 */
public final class Game {

  private static final Logger LOGGER = Logger.getLogger(Game.class.getSimpleName());
  private static INetworkHandler networkHandler;

  /** Starts the dungeon. Initializes the network handler if networking is enabled. */
  public static void run() {
    if (PreRunConfiguration.multiplayerEnabled()) {
      networkHandler = new NettyNetworkHandler();
    } else {
      networkHandler = new LocalNetworkHandler();
    }

    try {
      // Always route raw messages into the game dispatcher on the game thread
      networkHandler._setRawMessageConsumer(networkHandler.messageDispatcher()::dispatch);
      // Explicitly inject a SnapshotTranslator before initialization
      networkHandler.setSnapshotTranslator(new DefaultSnapshotTranslator());
      networkHandler.initialize(
          PreRunConfiguration.isNetworkServer(),
          PreRunConfiguration.networkServerAddress(),
          PreRunConfiguration.networkPort(),
          PreRunConfiguration.username());
      networkHandler.start();
      LOGGER.info("Network handler initialized and started.");
    } catch (NetworkException e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize network handler.", e);
    }

    // Start the main game loop
    if (!PreRunConfiguration.isNetworkServer()) {
      GameLoop.run();
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOGGER.info("Stopping server...");
                  networkHandler.shutdown();
                }));
  }

  /**
   * Retrieves the window width from Gdx.
   *
   * @return The window width.
   */
  public static int windowWidth() {
    return Gdx.graphics.getWidth();
  }

  /**
   * Sets the window width in the pre-run configuration.
   *
   * @param windowWidth The new window width.
   */
  public static void windowWidth(int windowWidth) {
    PreRunConfiguration.windowWidth(windowWidth);
  }

  /**
   * Retrieves the window height from Gdx.
   *
   * @return The window height.
   */
  public static int windowHeight() {
    return Gdx.graphics.getHeight();
  }

  /**
   * Sets the window height in the pre-run configuration.
   *
   * @param windowHeight The new window height.
   */
  public static void windowHeight(int windowHeight) {
    PreRunConfiguration.windowHeight(windowHeight);
  }

  /**
   * Retrieves the frame rate from the pre-run configuration.
   *
   * @return The frame rate.
   */
  public static int frameRate() {
    return PreRunConfiguration.frameRate();
  }

  /**
   * Sets the frame rate in the pre-run configuration.
   *
   * @param frameRate The new frame rate.
   */
  public static void frameRate(int frameRate) {
    PreRunConfiguration.frameRate(frameRate);
  }

  /**
   * Checks if the game-window can be resized.
   *
   * @return True if the game-window can be resized. , false otherwise.
   */
  public static boolean resizeable() {
    return PreRunConfiguration.resizeable();
  }

  /**
   * Sets whether the game-window can be resized..
   *
   * @param resizeable True to enable resizing, false otherwise.
   */
  public static void resizeable(boolean resizeable) {
    PreRunConfiguration.resizeable(resizeable);
  }

  /**
   * Sets the window title in the pre-run configuration.
   *
   * @param windowTitle The new window title.
   */
  public static void windowTitle(final String windowTitle) {
    PreRunConfiguration.windowTitle(windowTitle);
  }

  /**
   * Updates the window title.
   *
   * @param newTitle The new window title.
   */
  public static void updateWindowTitle(String newTitle) {
    Gdx.graphics.setTitle(newTitle);
  }

  /**
   * Gets the path to the game logo.
   *
   * @return The path to the game logo.
   */
  public static IPath logoPath() {
    return PreRunConfiguration.logoPath();
  }

  /**
   * Sets the path to the game logo.
   *
   * @param logoPath The path to the game logo.
   */
  public static void logoPath(IPath logoPath) {
    PreRunConfiguration.logoPath(logoPath);
  }

  /**
   * Sets the audio disable setting in the pre-run configuration.
   *
   * @param disableAudio True to disable audio, false otherwise.
   */
  public static void disableAudio(boolean disableAudio) {
    PreRunConfiguration.disableAudio(disableAudio);
  }

  /**
   * Enables or disables the check pattern drawing mode.
   *
   * @param enabled {@code true} to draw the level with a check pattern, {@code false} to draw it
   *     without
   */
  public static void enableCheckPattern(boolean enabled) {
    PreRunConfiguration.enableCheckPattern(enabled);
  }

  /**
   * Checks if the check pattern drawing mode is enabled.
   *
   * @return {@code true} if the level will be drawn with a check pattern, {@code false} otherwise
   */
  public static boolean isCheckPatternEnabled() {
    return PreRunConfiguration.isCheckPatternEnabled();
  }

  /**
   * Sets the user-defined function for frame updates in the pre-run configuration.
   *
   * @param userOnFrame The new user-defined function for frame updates.
   */
  public static void userOnFrame(final IVoidFunction userOnFrame) {
    PreRunConfiguration.userOnFrame(userOnFrame);
  }

  /**
   * Sets the user-defined function for setup in the pre-run configuration.
   *
   * @param userOnSetup The new user-defined function for setup.
   */
  public static void userOnSetup(final IVoidFunction userOnSetup) {
    PreRunConfiguration.userOnSetup(userOnSetup);
  }

  /**
   * Sets the user-defined function for level load in the pre-run configuration.
   *
   * @param userOnLevelLoad The new user-defined function for level load.
   */
  public static void userOnLevelLoad(final Consumer<Boolean> userOnLevelLoad) {
    PreRunConfiguration.userOnLevelLoad(userOnLevelLoad);
  }

  /**
   * Initialize the base logger.
   *
   * <p>Set a logging level, and remove the console handler, and write all log messages into the log
   * files.
   *
   * @param level Set logging level to {@code level}
   */
  public static void initBaseLogger(Level level) {
    PreRunConfiguration.initBaseLogger(level);
  }

  /**
   * Initialize the base logger.
   *
   * <p>Set the logging level to {@code Level.ALL}, and remove the console handler, and write all
   * log messages into the log files. This is a convenience method.
   */
  public static void initBaseLogger() {
    Game.initBaseLogger(Level.ALL);
  }

  /**
   * Loads the configuration from the given path. If the configuration has already been loaded, the
   * cached version will be used.
   *
   * @param path The path to the config file.
   * @param keyboardConfigClass The class where the ConfigKey fields are located.
   * @throws IOException If the file could not be read.
   */
  public static void loadConfig(final IPath path, final Class<?>... keyboardConfigClass)
      throws IOException {
    PreRunConfiguration.loadConfig(path, keyboardConfigClass);
  }

  /**
   * Retrieves the optional stage from the game loop.
   *
   * @return The optional stage.
   */
  public static Optional<Stage> stage() {
    return GameLoop.stage();
  }

  /**
   * The given entity will be added to the game.
   *
   * <p>For each {@link System}, it will be checked if the {@link System} will process this entity.
   *
   * <p>If necessary, the {@link System} will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to add.
   */
  public static void add(final Entity entity) {
    ECSManagment.add(entity);
  }

  /**
   * The given entity will be removed from the game.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to remove
   */
  public static void remove(final Entity entity) {
    ECSManagment.remove(entity);
  }

  /**
   * Add a {@link System} to the game.
   *
   * <p>If a System is added to the game, the {@link System#execute} method will be called every
   * frame.
   *
   * <p>Additionally, the system will be informed about all new, changed, and removed entities.
   *
   * <p>The game can only store one system of each system type.
   *
   * @param system the System to add
   * @return an optional that contains the previous existing system of the given system class, if
   *     one exists
   * @see System
   */
  public static Optional<System> add(final System system) {
    return ECSManagment.add(system);
  }

  /**
   * Get all registered systems.
   *
   * @return a copy of the map that stores all registered {@link System} in the game.
   */
  public static Map<Class<? extends System>, System> systems() {
    return ECSManagment.systems();
  }

  /**
   * If a system instance of the specified type is present, performs the given action on it.
   *
   * @param <T> the type of the system, which must extend {@link System}
   * @param s the class object of the desired system type
   * @param c the {@link Consumer} to execute with the system instance if present
   */
  public static <T extends System> void system(Class<T> s, Consumer<T> c) {
    ECSManagment.system(s, c);
  }

  /**
   * Remove all registered systems from the game.
   *
   * <p>Will trigger {@link System#onEntityRemove} for each entity in each system.
   */
  public static void removeAllSystems() {
    ECSManagment.removeAllSystems();
  }

  /**
   * Use this stream if you want to iterate over all entities in the current level.
   *
   * @return a stream of all entities currently in the level
   */
  public static Stream<Entity> levelEntities() {
    return ECSManagment.levelEntities();
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the necessary Components
   * to be processed by the given system.
   *
   * @param system the system to check.
   * @return a stream of all entities currently in the game that should be processed by the given
   *     system.
   */
  public static Stream<Entity> levelEntities(final System system) {
    return ECSManagment.levelEntities(system);
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the given components.
   *
   * @param filter the components to check.
   * @return a stream of all entities currently in the game that contains the given components.
   */
  public static Stream<Entity> levelEntities(final Set<Class<? extends Component>> filter) {
    return ECSManagment.levelEntities(filter);
  }

  /**
   * Searches the current level for the player character.
   *
   * @return an {@link Optional} containing the player character from the current level, or an empty
   *     {@code Optional} if none is present
   */
  public static Optional<Entity> hero() {
    return ECSManagment.hero();
  }

  /**
   * Remove the stored system of the given class from the game. If the System is successfully
   * removed, the {@link System#triggerOnRemove(Entity)} method of the System will be called for
   * each existing Entity that was associated with the removed System.
   *
   * @param system the class of the system to remove
   */
  public static void remove(final Class<? extends System> system) {
    ECSManagment.remove(system);
  }

  /**
   * Remove all entities from the game.
   *
   * <p>This will also remove all entities from each system.
   */
  public static void removeAllEntities() {
    ECSManagment.removeAllEntities();
  }

  /**
   * Use this stream if you want to iterate over all active entities.
   *
   * <p>Use {@link #levelEntities()} if you want to iterate over all active entities.
   *
   * @return a stream of all entities currently in the game
   */
  public static Stream<Entity> allEntities() {
    return ECSManagment.allEntities();
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities in the game, not just those in the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInAll(final Component component) {
    return ECSManagment.findInAll(component);
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities in the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInLevel(final Component component) {
    return ECSManagment.findInLevel(component);
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInLevel(Entity entity) {
    return ECSManagment.existInLevel(entity);
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInAll(final Entity entity) {
    return ECSManagment.existInAll(entity);
  }

  /**
   * Get the current level.
   *
   * @return the currently loaded level or an empty Optional if there is no level.
   */
  public static Optional<ILevel> currentLevel() {
    return LevelSystem.level();
  }

  /**
   * Get the tile at the given point in the level.
   *
   * <p>{@link Point#toCoordinate} will be used, to convert the point into a coordinate.
   *
   * @param point Point from where to get the tile
   * @return the tile at the given point or an empty Optional.
   */
  public static Optional<Tile> tileAt(final Point point) {
    return currentLevel().flatMap(level -> level.tileAt(point));
  }

  /**
   * Get the tile at the given coordinate in the level.
   *
   * @param coordinate Coordinate from where to get the tile
   * @return The tile at the specified coordinate, or an empty Optional. if there is no tile or the
   *     coordinate is out of bounds.
   */
  public static Optional<Tile> tileAt(final Coordinate coordinate) {
    return currentLevel().flatMap(level -> level.tileAt(coordinate));
  }

  /**
   * Get the next tile in the given direction from the specified coordinate.
   *
   * @param coordinate The starting coordinate
   * @param direction The direction in which to find the next tile
   * @return The tile that is the next tile from the given coordinate in the specified direction or
   *     an empty Optional.
   */
  public static Optional<Tile> tileAt(final Coordinate coordinate, Direction direction) {
    return tileAt(coordinate.translate(direction));
  }

  /**
   * Get the next tile in the given direction from the specified point.
   *
   * @param point The starting point
   * @param direction The direction in which to find the next tile
   * @return The tile that is the next tile from the given point in the specified direction or an
   *     empty Optional.
   */
  public static Optional<Tile> tileAt(final Point point, Direction direction) {
    return tileAt(point.toCoordinate(), direction);
  }

  /**
   * Get a random tile in the level.
   *
   * @return a random Tile in the Level or an empty Optional if there is no tile in the level.
   */
  public static Optional<Tile> randomTile() {
    return currentLevel().flatMap(ILevel::randomTile);
  }

  /**
   * Get the neighbors of the given Tile.
   *
   * <p>Neighbors are the tiles directly above, below, left, and right of the given Tile.
   *
   * @param tile Tile to get the neighbors for
   * @return Set with the neighbor tiles.
   */
  public static Set<Tile> neighbours(final Tile tile) {
    return LevelUtils.neighbours(tile);
  }

  /**
   * Retrieves one end tile of the level, if present.
   *
   * <p>If multiple end tiles exist in the level, only one of them will be returned.
   *
   * @return an {@link Optional} containing one end tile if present, or an empty {@link Optional} if
   *     none exist
   * @deprecated use {@link #endTiles()} to retrieve all end tiles
   */
  @Deprecated
  public static Optional<Tile> endTile() {
    return currentLevel().flatMap(ILevel::endTile);
  }

  /**
   * Retrieves a Set containing the end tiles of the level.
   *
   * @return Set containing the end tiles of the level.
   */
  public static Set<ExitTile> endTiles() {
    return currentLevel().map(ILevel::endTiles).orElseGet(Collections::emptySet);
  }

  /**
   * Get the start tile.
   *
   * @return The start tile or an empty Optional if there is no dedicated start tile.
   */
  public static Optional<Tile> startTile() {
    return currentLevel().flatMap(ILevel::startTile);
  }

  /**
   * Returns the tile the given entity is standing on.
   *
   * @param entity entity to check for.
   * @return tile at the coordinate of the entity or an empty Optional.
   */
  public static Optional<Tile> tileAtEntity(final Entity entity) {
    return currentLevel().flatMap(level -> level.tileAtEntity(entity));
  }

  /**
   * Returns the entities on the given tile. If the tile is null, an empty stream will be returned.
   *
   * @param check Tile to check for.
   * @return Stream of all entities on the given tile
   */
  public static Stream<Entity> entityAtTile(final Tile check) {
    return Game.tileAt(check.position())
        .map(
            target -> {
              Set<Class<? extends Component>> filter = new HashSet<>();
              filter.add(PositionComponent.class);

              return ECSManagment.levelEntities(filter)
                  .filter(
                      e ->
                          e.fetch(PositionComponent.class)
                              .map(PositionComponent::position)
                              .flatMap(Game::tileAt)
                              .map(target::equals)
                              .orElse(false));
            })
        .orElseGet(Stream::empty);
  }

  /**
   * Get a random tile of the given type.
   *
   * @param elementType Type of the tile.
   * @return A random tile of the given type or an empty Optional if there is no tile of that type.
   */
  public static Optional<Tile> randomTile(final LevelElement elementType) {
    return currentLevel().flatMap(level -> level.randomTile(elementType));
  }

  /**
   * Get the position of a random Tile as Point.
   *
   * @return Position of the Tile as Point or an empty Optional if there is no tile in the level.
   */
  public static Optional<Point> randomTilePoint() {
    return currentLevel().flatMap(ILevel::randomTilePoint);
  }

  /**
   * Get the position of a random Tile as Point.
   *
   * @param elementTyp Type of the Tile.
   * @return Position of the Tile as Point or an empty Optional if there is no tile of that type.
   */
  public static Optional<Point> randomTilePoint(final LevelElement elementTyp) {
    return currentLevel().flatMap(level -> randomTilePoint(elementTyp));
  }

  /**
   * Get all tiles from the current level.
   *
   * @return A Set containing all tiles in the current level.
   */
  public static Set<Tile> allTiles() {
    return currentLevel()
        .map(
            level ->
                Arrays.stream(level.layout()).flatMap(Arrays::stream).collect(Collectors.toSet()))
        .orElseGet(Collections::emptySet);
  }

  /**
   * Get all tiles from the current level that satisfy the provided predicate.
   *
   * @param filterRule A predicate that determines which tiles to include.
   * @return A Set containing all tiles in the current level that satisfy the predicate.
   */
  public static Set<Tile> allTiles(Predicate<Tile> filterRule) {
    return currentLevel()
        .map(
            level ->
                Arrays.stream(level.layout())
                    .flatMap(Arrays::stream)
                    .filter(filterRule)
                    .collect(Collectors.toSet()))
        .orElseGet(Collections::emptySet);
  }

  /**
   * Get all tiles of the specified type from the current level.
   *
   * @param elementTyp Type of the tiles to retrieve.
   * @return A Set containing all tiles of the specified type in the current level.
   */
  public static Set<Tile> allTiles(final LevelElement elementTyp) {
    return allTiles(tile -> tile.levelElement() == elementTyp);
  }

  /**
   * Get all free tiles from the current level. A tile is considered free if no entity is present on
   * it, and it is accessible.
   *
   * @return A Set containing all free tiles in the current level.
   */
  public static Set<Tile> allFreeTiles() {
    return allTiles(LevelUtils::isFreeTile);
  }

  /**
   * Get a random free tile from the current level. A free tile is a tile that is of type FLOOR and
   * is not occupied by any entity and is accessible.
   *
   * @return An Optional containing a random free tile if available, otherwise an empty Optional.
   */
  public static Optional<Tile> freeTile() {
    return LevelUtils.freeTile();
  }

  /**
   * Get a position of a random free tile from the current level. A free tile is a tile that is of
   * type FLOOR and is not occupied by any entity and is accessible.
   *
   * @return An Optional containing the postion of a random free tile if available, otherwise an
   *     empty Optional.
   */
  public static Optional<Point> freePosition() {
    return freeTile().map(Tile::position);
  }

  /**
   * Checks if the given Tile is accessible and no entity is placed on that tile.
   *
   * @param tile Tile to check.
   * @return True if the Tile is free, false if not
   */
  public static boolean isFreeTile(Tile tile) {
    return LevelUtils.isFreeTile(tile);
  }

  /**
   * Get all accessible tiles within a specified range around a given center point.
   *
   * <p>The range is determined by the provided radius.
   *
   * <p>The tile at the given point will be part of the list as well, if it is accessible.
   *
   * @param center The center point around which the tiles are considered.
   * @param radius The radius within which the accessible tiles should be located.
   * @return List of accessible tiles in the given radius around the center point.
   */
  public static List<Tile> accessibleTilesInRange(final Point center, float radius) {
    return LevelUtils.accessibleTilesInRange(center, radius);
  }

  /**
   * Starts the indexed A* pathfinding algorithm and returns a path.
   *
   * <p>Returns an empty Optional if there is no current level.
   *
   * @param start Start tile
   * @param end End tile
   * @return Generated path or Optional.empty() if no current level
   */
  public static Optional<GraphPath<Tile>> findPath(final Tile start, final Tile end) {
    return currentLevel()
        .map(level -> level.findPath(start, end)); // map liefert Optional<GraphPath<Tile>>
  }

  /**
   * Returns the position of the given entity, if it has a {@link PositionComponent}.
   *
   * @param entity the entity to retrieve the current position from
   * @return an {@link Optional} containing the entity's position, or empty if no {@link
   *     PositionComponent} is present
   */
  public static Optional<Point> positionOf(final Entity entity) {
    return entity.fetch(PositionComponent.class).map(PositionComponent::position);
  }

  /**
   * Set the current level.
   *
   * <p>This method is for testing and debugging purposes.
   *
   * @param level New level
   */
  public static void currentLevel(final ILevel level) {
    LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
    if (levelSystem != null) levelSystem.loadLevel(level);
    else LOGGER.warning("Can not set Level because levelSystem is null.");
  }

  /** Exits the GDX application and shuts down the network handler. */
  public static void exit(String reason) {
    if (networkHandler != null) {
      try {
        networkHandler.shutdown(reason);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Error shutting down network handler", e);
      }
    }
    Gdx.app.exit();
  }

  /** Exits the GDX application and shuts down the network handler. */
  public static void exit() {
    exit("Exit Game");
  }

  /**
   * Gets the network handler instance. This allows other parts of the game (like HeroFactory) to
   * send messages.
   *
   * @return The NetworkHandler instance.
   * @throws IllegalStateException if the network handler is not initialized.
   */
  public static INetworkHandler network() {
    if (networkHandler == null) {
      throw new IllegalStateException("Network handler is not initialized. Call Game.run() first.");
    }
    return networkHandler;
  }
}
