package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import core.components.PositionComponent;
import core.configuration.Configuration;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.generator.postGeneration.WallGenerator;
import core.level.generator.randomwalk.RandomWalkGenerator;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.systems.*;
import core.utils.Constants;
import core.utils.EntitySystemMapper;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.LoggerConfig;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** The heart of the framework. From here all strings are pulled. */
public final class Game extends ScreenAdapter {

    /**
     * A Map with each {@link System} in the game.
     *
     * <p>The Key-Value is the Class of the system
     */
    private static final Map<Class<? extends System>, System> systems = new LinkedHashMap<>();
    /** Maps the level with the different {@link EntitySystemMapper} for that level. */
    private static final Map<ILevel, Set<EntitySystemMapper>> levelStorageMap = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger("Game");
    /**
     * Collection of {@link EntitySystemMapper} that maps the exisiting entities to the systems. The
     * {@link EntitySystemMapper} with no filter-rules will contain each entity in the game
     */
    private static Set<EntitySystemMapper> activeEntityStorage = new HashSet<>();
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static int WINDOW_WIDTH = 1280;
    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static int WINDOW_HEIGHT = 720;
    /**
     * Part of the pre-run configuration. The fps of the game (frames per second)
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static int FRAME_RATE = 30;

    /**
     * Part of the pre-run configuration. If this value is true, the game will be started in full
     * screen mode.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static boolean FULL_SCREEN = false;

    /**
     * Part of the pre-run configuration. The title of the Game-Window.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static String WINDOW_TITLE = "PM-Dungeon";
    /**
     * Part of the pre-run configuration. The path (as String) to the logo of the Game-Window.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static String LOGO_PATH = "logo/cat_logo_35x35.png";
    /**
     * Part of the pre-run configuration.
     * This function will be called at each frame.
     * <p> Use this, if you want to execute some logic outside of a system.</p>
     * <p> Will not replace {@link #onFrame )</p>
     */
    private static IVoidFunction userOnFrame = () -> {};

    /**
     * Part of the pre-run configuration. This function will be called after the libgdx-setup once.
     *
     * <p>Will not replace {@link #onSetup()} )
     */
    private static IVoidFunction userOnSetup = () -> {};
    /**
     * Part of the pre-run configuration. This function will be called after a level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * <p>The Consumer takes a boolean that is true if the level was never loaded before, and false
     * if it was loaded before. * This can be useful, for example, if you only want to spawn
     * monsters the first time.
     *
     * <p>Will not replace {@link #onLevelLoad}
     */
    private static Consumer<Boolean> userOnLevelLoad = (b) -> {};
    /**
     * Part of the pre-run configuration. If this value is true, the audio for the game will be
     * disabled.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static boolean DISABLE_AUDIO = false;

    private static Entity hero;

    private static Stage stage;
    private boolean doSetup = true;
    private boolean uiDebugFlag = false;
    private boolean newLevelWasLoadedInThisLoop = false;
    /**
     * Sets {@link #currentLevel} to the new level and changes the currently active entity storage.
     *
     * <p>Will remove all Systems using {@link Game#removeAllSystems()} from the Game. This will
     * trigger {@link System#onEntityRemove} for the old level. Then, it will readd all Systems
     * using {@link Game#add(System)}, triggering {@link System#onEntityAdd} for the new level.
     *
     * <p>Will re-add the hero if they exist.
     */
    private final IVoidFunction onLevelLoad =
            () -> {
                newLevelWasLoadedInThisLoop = true;
                boolean firstLoad = !levelStorageMap.containsKey(currentLevel());
                hero().ifPresent(Game::remove);
                // Remove the systems so that each triggerOnRemove(entity) will be called (basically
                // cleanup).
                Map<Class<? extends System>, System> s = Game.systems();
                removeAllSystems();
                activeEntityStorage =
                        levelStorageMap.computeIfAbsent(currentLevel(), k -> new HashSet<>());
                // Readd the systems so that each triggerOnAdd(entity) will be called (basically
                // setup). This will also create new EntitySystemMapper if needed.
                s.values().forEach(Game::add);

                try {
                    hero().ifPresent(this::placeOnLevelStart);
                } catch (MissingComponentException e) {
                    LOGGER.warning(e.getMessage());
                }
                hero().ifPresent(Game::add);
                currentLevel().onLoad();
                userOnLevelLoad.accept(firstLoad);
            };

    // for singleton
    private Game() {}

    /**
     * @return the currently loaded level
     */
    public static ILevel currentLevel() {
        return LevelSystem.level();
    }

    /**
     * @return a copy of the map that stores all registered {@link System} in the game.
     */
    public static Map<Class<? extends System>, System> systems() {
        return new LinkedHashMap<>(systems);
    }

    /**
     * Remove all registered systems from the game.
     *
     * <p>Will trigger {@link System#onEntityRemove} for each entity in each system.
     */
    public static void removeAllSystems() {
        new HashSet<>(systems.keySet()).forEach(Game::remove);
    }

    /**
     * Width of the game-window in pixel
     *
     * @return the width of the game-window im pixel
     */
    public static int windowWidth() {
        return WINDOW_WIDTH;
    }

    /**
     * Height of the game-window in pixel
     *
     * @return the height of the game-window im pixel
     */
    public static int windowHeight() {
        return WINDOW_HEIGHT;
    }

    /**
     * Get the current frame rate of the game
     *
     * @return current frame rate of the game
     */
    public static int frameRate() {
        return FRAME_RATE;
    }

    /**
     * Get if the game is currently in full screen mode
     *
     * @return true if the game is currently in full screen mode
     */
    public static boolean fullScreen() {
        return FULL_SCREEN;
    }

    /**
     * The currently set level-Size.
     *
     * <p>This value is used for the generation of the next level.
     *
     * <p>The currently active level can have a different size.
     *
     * @return currently set level-Size.
     */
    public static LevelSize levelSize() {
        return LevelSystem.levelSize();
    }

    /**
     * Set the width of the game window in pixels.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param windowWidth: the new width of the game window in pixels.
     */
    public static void windowWidth(int windowWidth) {
        WINDOW_WIDTH = windowWidth;
    }

    /**
     * Set the height of the game window in pixels.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param windowHeight: the new height of the game window in pixels.
     */
    public static void windowHeight(int windowHeight) {
        WINDOW_HEIGHT = windowHeight;
    }

    /**
     * The frames per second of the game. The FPS determine in which interval the update cycle of
     * the systems is triggered. Each system is updated once per frame. With an FPS of 30, each
     * system is updated 30 times per second.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param frameRate: the new fps of the game
     */
    public static void frameRate(int frameRate) {
        FRAME_RATE = frameRate;
    }

    /**
     * Set the window to fullscreen mode or windowed mode.
     *
     * @param fullscreen true for fullscreen, false for windowed
     */
    public static void fullScreen(boolean fullscreen) {
        FULL_SCREEN = fullscreen;
    }

    /**
     * Set the title of the game window.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param windowTitle: new title
     */
    public static void windowTitle(String windowTitle) {
        WINDOW_TITLE = windowTitle;
    }

    /**
     * Set the path to the logo of the game window.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param logoPath: path to the nwe logo as String
     */
    public static void logoPath(String logoPath) {
        LOGO_PATH = logoPath;
    }

    /**
     * Set the {@link LevelSize} of the next level.
     *
     * @param levelSize Size of the next level.
     */
    public static void levelSize(LevelSize levelSize) {
        LevelSystem.levelSize(levelSize);
    }

    /**
     * Set the function that will be executed at each frame.
     * <p> Use this, if you want to execute some logic outside of a system.</p>
     * <p> Will not replace {@link #onFrame )</p>
     *
     * @param userFrame function that will be called at each frame.
     * @see IVoidFunction
     */
    public static void userOnFrame(IVoidFunction userFrame) {
        Game.userOnFrame = userFrame;
    }

    /**
     * Set the function that will be executed once after the libgdx-setup.
     *
     * <p>Will not replace {@link #onSetup()} )
     *
     * @param userOnSetup function that will be once after the libgdx-setup.
     * @see IVoidFunction
     */
    public static void userOnSetup(IVoidFunction userOnSetup) {
        Game.userOnSetup = userOnSetup;
    }

    /**
     * Set the function that will be executed after a new level was loaded.
     *
     * <p>The Consumer takes a boolean that is true if the level was never loaded before, and false
     * if it was loaded before. This can be useful, for example, if you only want to spawn monsters
     * the first time.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * @param userOnLevelLoad the function that will be executed after a new level was loaded
     *     <p>Will not replace {@link #onLevelLoad}
     */
    public static void userOnLevelLoad(Consumer<Boolean> userOnLevelLoad) {
        Game.userOnLevelLoad = userOnLevelLoad;
    }

    /**
     * Set if you want to disable or enable the audi of the game.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Game#run} was executed.
     *
     * @param disableAudio true if you want to disable the audio, false (default) if not.
     */
    public static void disableAudio(boolean disableAudio) {
        DISABLE_AUDIO = disableAudio;
    }

    /**
     * Initialize the base logger.
     *
     * <p>Will remove the console handler and put all log messages in the log files.
     */
    public static void initBaseLogger() {
        LoggerConfig.initBaseLogger();
    }

    /**
     * Inform each {@link System} that the given Entity has changes on component bases.
     *
     * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} or
     * {@link System#triggerOnRemove(Entity)}.
     *
     * @param entity the entity that has changes in its Component Collection.
     */
    public static void informAboutChanges(Entity entity) {
        if (entityStream().anyMatch(entity1 -> entity1.equals(entity))) {
            activeEntityStorage.forEach(f -> f.update(entity));
            LOGGER.info("Entity: " + entity + " informed the Game about component changes.");
        }
    }

    /**
     * The given entity will be added to the game.
     *
     * <p>For each {@link System}, it will be checked if the {@link System} will process this
     * entity.
     *
     * <p>If necessary, the {@link System} will trigger {@link System#triggerOnAdd(Entity)} .
     *
     * @param entity the entity to add.
     */
    public static void add(Entity entity) {
        activeEntityStorage.forEach(f -> f.add(entity));
        LOGGER.info("Entity: " + entity + " will be added to the Game.");
    }

    /**
     * The given entity will be removed from the game.
     *
     * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} .
     *
     * @param entity the entity to remove
     */
    public static void remove(Entity entity) {
        activeEntityStorage.forEach(f -> f.remove(entity));
        LOGGER.info("Entity: " + entity + " will be removed from the Game.");
    }

    /**
     * Use this stream if you want to iterate over all currently active entities.
     *
     * @return a stream of all entities currently in the game
     */
    public static Stream<Entity> entityStream() {
        return entityStream(new HashSet<>());
    }

    /**
     * Use this stream if you want to iterate over all entities that contain the necessary
     * Components to be processed by the given system.
     *
     * @return a stream of all entities currently in the game that should be processed by the given
     *     system.
     */
    public static Stream<Entity> entityStream(System system) {
        return entityStream(system.filterRules());
    }

    /**
     * Use this stream if you want to iterate over all entities that contain the given components.
     *
     * @return a stream of all entities currently in the game that contains the given components.
     */
    public static Stream<Entity> entityStream(Set<Class<? extends Component>> filter) {
        Stream<Entity> returnStream;
        Optional<EntitySystemMapper> rf =
                activeEntityStorage.stream().filter(f -> f.equals(filter)).findFirst();

        if (rf.isEmpty()) {
            EntitySystemMapper newMapper = createNewEntitySystemMapper(filter);
            returnStream = newMapper.stream();
        } else returnStream = rf.get().stream();
        return returnStream;
    }

    /**
     * Create a new {@link EntitySystemMapper} with the given filter rules.
     *
     * <p>The {@link EntitySystemMapper} will be added to {@link #activeEntityStorage}.
     *
     * <p>All entities in the empty filter (basically every entity in the game) will be tried to add
     * with {@link EntitySystemMapper#add(Entity)}.
     *
     * <p>This function will not check if an {@link EntitySystemMapper} with the same rules already
     * exists. If an {@link EntitySystemMapper} exists, it will not be replaced, and the {@link
     * EntitySystemMapper} created in this function will be lost.
     *
     * @param filter Set of Component classes that define the filter rules.
     * @return the created {@link EntitySystemMapper}.
     */
    private static EntitySystemMapper createNewEntitySystemMapper(
            Set<Class<? extends Component>> filter) {
        EntitySystemMapper mapper = new EntitySystemMapper(filter);
        activeEntityStorage.add(mapper);
        entityStream().forEach(mapper::add);
        return mapper;
    }

    /**
     * @return the player character, can be null if not initialized
     * @see Optional
     */
    public static Optional<Entity> hero() {
        return Optional.ofNullable(hero);
    }

    /**
     * Set the reference of the playable character.
     *
     * <p>Be careful: the old hero will not be removed from the game.
     *
     * @param hero the new reference of the hero
     */
    public static void hero(Entity hero) {
        Game.hero = hero;
    }

    /**
     * Load the configuration from the given path. If the configuration has already been loaded, the
     * cached version will be used.
     *
     * @param pathAsString the path to the config file as a string
     * @param klass the class where the ConfigKey fields are located
     * @throws IOException if the file could not be read
     */
    public static void loadConfig(String pathAsString, Class<?>... klass) throws IOException {
        Configuration.loadAndGetConfiguration(pathAsString, klass);
    }

    /** Starts the dungeon and requires a {@link Game}. */
    public static void run() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowSizeLimits(WINDOW_WIDTH, WINDOW_HEIGHT, 9999, 9999);
        // The third and fourth parameters ("maxWidth" and "maxHeight") affect the resizing
        // behavior
        // of the window. If the window is enlarged or maximized, then it can assume these
        // dimensions at maximum. If you have a larger screen resolution than 9999x9999 pixels,
        // increase these parameters.
        config.setForegroundFPS(FRAME_RATE);
        config.setTitle(WINDOW_TITLE);
        config.setWindowIcon(LOGO_PATH);
        config.disableAudio(DISABLE_AUDIO);

        if (FULL_SCREEN) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        // uncomment this if you wish no audio
        new Lwjgl3Application(
                new com.badlogic.gdx.Game() {
                    @Override
                    public void create() {
                        setScreen(new Game());
                    }
                },
                config);
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
     * @see Optional
     */
    public static Optional<System> add(System system) {
        System currentSystem = systems.get(system.getClass());
        systems.put(system.getClass(), system);
        // add to existing filter or create new filter if no matching exists
        Optional<EntitySystemMapper> filter =
                activeEntityStorage.stream()
                        .filter(f -> f.equals(system.filterRules()))
                        .findFirst();
        filter.ifPresentOrElse(
                f -> f.add(system),
                () -> createNewEntitySystemMapper(system.filterRules()).add(system));
        LOGGER.info("A new " + system.getClass().getName() + " was added to the game");
        return Optional.ofNullable(currentSystem);
    }

    /**
     * Remove the stored system of the given class from the game. If the System is successfully
     * removed, the {@link System#triggerOnRemove(Entity)} method of the System will be called for
     * each existing Entity that was associated with the removed System.
     *
     * @param system the class of the system to remove
     */
    public static void remove(Class<? extends System> system) {
        System systemInstance = systems.remove(system);
        if (systemInstance != null) activeEntityStorage.forEach(f -> f.remove(systemInstance));
    }

    /**
     * Remove all entities from the game.
     *
     * <p>This will also remove all entities from each system.
     */
    public static void removeAllEntities() {
        Game.entityStream().forEach(Game::remove);
        LOGGER.info("All entities will be removed from the game.");
    }

    public static Optional<Stage> stage() {
        return Optional.ofNullable(stage);
    }

    private static void updateStage(Stage x) {
        x.act(Gdx.graphics.getDeltaTime());
        x.draw();
    }

    /**
     * Get the tile at the given point in the level
     *
     * <p>{@link Point#toCoordinate} will be used, to convert the point into a coordinate.
     *
     * @param p Point from where to get the tile
     * @return the tile at the given point.
     */
    public static Tile tileAT(Point p) {
        return currentLevel().tileAt(p);
    }

    /**
     * Get the tile at the given coordinate in the level
     *
     * @param c Coordinate from where to get the tile
     * @return the tile at the given coordinate.
     */
    public static Tile tileAT(Coordinate c) {
        return currentLevel().tileAt(c);
    }

    /**
     * @return a random Tile in the Level
     */
    public static Tile randomTile() {
        return currentLevel().randomTile();
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    public static Tile endTile() {
        return currentLevel().endTile();
    }

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    public static Tile startTile() {
        return currentLevel().startTile();
    }

    /**
     * Returns the tile the given entity is standing on.
     *
     * @param entity entity to check for.
     * @return tile at the coordinate of the entity
     */
    public static Tile tileAtEntity(Entity entity) {
        return currentLevel().tileAtEntity(entity);
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    public static Tile randomTile(LevelElement elementType) {
        return currentLevel().randomTile(elementType);
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint() {
        return currentLevel().randomTilePoint();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint(LevelElement elementTyp) {
        return currentLevel().randomTilePoint(elementTyp);
    }

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    public static GraphPath<Tile> findPath(Tile start, Tile end) {
        return currentLevel().findPath(start, end);
    }

    /**
     * Get the Position of the given entity in the level.
     *
     * @param entity Entity to get the current position from (needs a {@link PositionComponent}
     * @return Position of the given entity.
     */
    public static Point positionOf(Entity entity) {
        return currentLevel().positionOf(entity);
    }

    /**
     * Set the current level.
     *
     * <p>This method is for testing and debugging purposes.
     *
     * @param level New level
     */
    public static void currentLevel(ILevel level) {
        LevelSystem levelSystem = (LevelSystem) systems.get(LevelSystem.class);
        if (levelSystem != null) levelSystem.loadLevel(level);
        else LOGGER.warning("Can not set Level because levelSystem is null.");
    }

    private static void setupStage() {
        stage =
                new Stage(
                        new ScalingViewport(Scaling.stretch, WINDOW_WIDTH, WINDOW_HEIGHT),
                        new SpriteBatch());
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Main game loop.
     *
     * <p>Redraws the dungeon, updates the entity sets, and triggers the execution of the systems.
     * Will call {@link #onFrame}.
     *
     * @param delta the time since the last loop
     */
    @Override
    public void render(float delta) {
        if (doSetup) onSetup();
        DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined);
        onFrame();
        clearScreen();

        for (System system : systems().values()) {
            // if a new level was loaded, stop this loop-run
            if (newLevelWasLoadedInThisLoop) break;
            if (system.isRunning()) system.execute();
        }
        newLevelWasLoadedInThisLoop = false;
        CameraSystem.camera().update();
        // stage logic
        Game.stage().ifPresent(Game::updateStage);
    }

    /**
     * Called once at the beginning of the game.
     *
     * <p>Will perform some setup.
     */
    private void onSetup() {
        doSetup = false;
        CameraSystem.camera().zoom = Constants.DEFAULT_ZOOM_FACTOR;
        createSystems();
        setupStage();
        userOnSetup.execute();
    }

    /**
     * Called at the beginning of each frame, before the entities are updated and the systems are
     * executed.
     *
     * <p>This is the place to add basic logic that isn't part of any system.
     */
    private void onFrame() {
        debugKeys();
        fullscreenKey();
        userOnFrame.execute();
    }

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            // toggle UI "debug rendering"
            stage().ifPresent(x -> x.setDebugAll(uiDebugFlag = !uiDebugFlag));
        }
    }

    private void fullscreenKey() {
        if (Gdx.input.isKeyJustPressed(
                core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
            }
        }
    }

    /**
     * Set the position of the given entity to the position of the level-start.
     *
     * <p>A {@link PositionComponent} is needed.
     *
     * @param entity entity to set on the start of the level, normally this is the hero.
     */
    private void placeOnLevelStart(Entity entity) {
        add(entity);
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        pc.position(startTile());
    }

    /**
     * Clear the screen. Removes all.
     *
     * <p>Needs to be called before redraw something.
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    /** Create the systems. */
    private void createSystems() {
        add(new PositionSystem());
        add(new CameraSystem());
        add(
                new LevelSystem(
                        DrawSystem.painter(),
                        new WallGenerator(new RandomWalkGenerator()),
                        onLevelLoad));
        add(new DrawSystem());
        add(new VelocitySystem());
        add(new PlayerSystem());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage().ifPresent(
                        x -> {
                            x.getViewport().setWorldSize(width, height);
                            x.getViewport().update(width, height, true);
                        });
    }
}
