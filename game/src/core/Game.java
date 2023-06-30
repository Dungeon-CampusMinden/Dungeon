package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import static core.utils.logging.LoggerConfig.initBaseLogger;

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

import contrib.entities.EntityFactory;

import core.components.PositionComponent;
import core.components.UIComponent;
import core.configuration.Configuration;
import core.hud.UITools;
import core.level.IOnLevelLoader;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.generator.postGeneration.WallGenerator;
import core.level.generator.randomwalk.RandomWalkGenerator;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.systems.*;
import core.utils.Constants;
import core.utils.DelayedSet;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** The heart of the framework. From here all strings are pulled. */
public final class Game extends ScreenAdapter implements IOnLevelLoader {

    /**
     * A Map with each {@link System} in the game.
     *
     * <p>The Key-Value is the Class of the system
     */
    private static final Map<Class<? extends System>, System> systems = new HashMap<>();
    /** All entities that are currently active in the dungeon */
    private static final DelayedSet<Entity> entities = new DelayedSet<>();

    private static final Logger LOGGER = Logger.getLogger("Game");
    /**
     * The currently loaded level of the game.
     *
     * @see ILevel
     * @see LevelManager
     */
    private static ILevel currentLevel;
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static int WINDOW_WIDTH = 640;
    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static int WINDOW_HEIGHT = 480;
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
    private static String LOGO_PATH = "logo/CatLogo_35x35.png";
    /** Currently used level-size configuration for generating new level */
    private static LevelSize LEVELSIZE = LevelSize.SMALL;
    /**
     * Part of the pre-run configuration.
     * This function will be called at each frame.
     * <p> Use this, if you want to execute some logic outside of a system.</p>
     * <p> Will not replace {@link #onFrame )</p>
     */
    private static IVoidFunction userOnFrame = () -> {};
    /**
     * Part of the pre-run configuration. This function will be called after a level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * <p>Will not replace {@link #onLevelLoad}
     */
    private static IVoidFunction userOnLevelLoad = () -> {};
    /**
     * Part of the pre-run configuration. If this value is true, the audio for the game will be
     * disabled.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    private static boolean DISABLE_AUDIO = false;

    private static Entity hero;

    private static Stage stage;

    private static LevelManager levelManager;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    private SpriteBatch batch;
    /** Draws objects */
    private Painter painter;

    private boolean doSetup = true;
    private boolean uiDebugFlag = false;

    // for singleton
    private Game() {}

    /**
     * @return the currently loaded level
     */
    public static ILevel currentLevel() {
        return currentLevel;
    }

    /**
     * @return a copy of the map that stores all registered {@link System} in the game.
     */
    public static Map<Class<? extends System>, System> systems() {
        return new HashMap<>(systems);
    }

    /** Remove all registered systems from the game. */
    public static void removeAllSystems() {
        systems.clear();
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
        return LEVELSIZE;
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
        Game.LEVELSIZE = levelSize;
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
     * Set the function that will be executed after a new level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * @param userOnLevelLoad the function that will be executed after a new level was loaded
     * @see IVoidFunction
     *     <p>Will not replace {@link #onLevelLoad}
     */
    public static void userOnLevelLoad(IVoidFunction userOnLevelLoad) {
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
     * In the next frame, each system will be informed that the given entity has changes in its
     * Component Collection.
     *
     * @param entity the entity that has changes in its Component Collection
     */
    public static void informAboutChanges(Entity entity) {
        entities.add(entity);
        LOGGER.info("Entity: " + entity + " informed the Game about component changes.");
    }

    /**
     * The given entity will be added to the game on the next frame.
     *
     * @param entity the entity to add
     * @see DelayedSet
     */
    public static void addEntity(Entity entity) {
        entities.add(entity);
        LOGGER.info("Entity: " + entity + " will be added to the Game.");
    }

    /**
     * The given entity will be removed from the game on the next frame.
     *
     * @param entity the entity to remove
     * @see DelayedSet
     */
    public static void removeEntity(Entity entity) {
        entities.remove(entity);
        LOGGER.info("Entity: " + entity + " will be removed from the Game.");
    }

    /**
     * Use this stream if you want to iterate over all currently active entities.
     *
     * @return a stream of all entities currently in the game
     */
    public static Stream<Entity> entityStream() {
        return entities.stream();
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
     * <p>Additionally, the system will be informed about all new, changed, and removed entities via
     * {@link System#showEntity} or {@link System#removeEntity}.
     *
     * <p>The game can only store one system of each system type.
     *
     * @param system the System to add
     * @return an optional that contains the previous existing system of the given system class, if
     *     one exists
     * @see System
     * @see Optional
     */
    public static Optional<System> addSystem(System system) {
        System currentSystem = systems.get(system.getClass());
        systems.put(system.getClass(), system);
        LOGGER.info("A new " + system.getClass().getName() + " was added to the game");
        return Optional.ofNullable(currentSystem);
    }

    /**
     * Remove the stored system of the given class from the game.
     *
     * @param system the class of the system to remove
     */
    public static void removeSystem(Class<? extends System> system) {
        systems.remove(system);
    }

    /**
     * Remove all entities from the game immediately.
     *
     * <p>This will also remove all entities from each system.
     */
    public static void removeAllEntities() {
        systems.values().forEach(System::clearEntities);
        entities.clear();
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
        return currentLevel.tileAt(p);
    }

    /**
     * Get the tile at the given coordinate in the level
     *
     * @param c Coordinate from where to get the tile
     * @return the tile at the given coordinate.
     */
    public static Tile tileAT(Coordinate c) {
        return currentLevel.tileAt(c);
    }

    /**
     * @return a random Tile in the Level
     */
    public static Tile randomTile() {
        return currentLevel.randomTile();
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    public static Tile endTile() {
        return currentLevel.endTile();
    }

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    public static Tile startTile() {
        return currentLevel.startTile();
    }

    /**
     * Returns the tile the given entity is standing on.
     *
     * @param entity entity to check for.
     * @return tile at the coordinate of the entity
     */
    public static Tile tileAtEntity(Entity entity) {
        return currentLevel.tileAtEntity(entity);
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    public static Tile randomTile(LevelElement elementType) {
        return currentLevel.randomTile(elementType);
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint() {
        return currentLevel.randomTilePoint();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint(LevelElement elementTyp) {
        return currentLevel.randomTilePoint(elementTyp);
    }

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    public static GraphPath<Tile> findPath(Tile start, Tile end) {
        return currentLevel.findPath(start, end);
    }

    /**
     * Get the Position of the given entity in the level.
     *
     * @param entity Entity to get the current position from (needs a {@link PositionComponent}
     * @return Position of the given entity.
     */
    public static Point positionOf(Entity entity) {
        return currentLevel.positionOf(entity);
    }

    /**
     * Set the current level.
     *
     * <p>This method is for testing and debugging purposes.
     *
     * <p>Will trigger {@link #onLevelLoad() if a {@link LevelManager} is active.}
     *
     * @param level New level
     */
    public static void currentLevel(ILevel level) {
        if (levelManager != null) levelManager.level(level);
        currentLevel = level;
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
        batch.setProjectionMatrix(CameraSystem.camera().combined);
        onFrame();
        clearScreen();
        levelManager.update();
        updateSystems();
        systems.values().stream().filter(System::isRunning).forEach(System::execute);
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
        batch = new SpriteBatch();
        painter = new Painter(batch);
        IGenerator generator = new RandomWalkGenerator();
        initBaseLogger();
        levelManager =
                new LevelManager(
                        batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelManager.loadLevel(LEVELSIZE);
        createSystems();

        setupStage();
    }

    /**
     * Called at the beginning of each frame, before the entities are updated and the systems are
     * executed.
     *
     * <p>This is the place to add basic logic that isn't part of any system.
     */
    private void onFrame() {
        try {
            hero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        } catch (MissingComponentException e) {
            LOGGER.warning(e.getMessage());
        }
        debugKeys();
        fullscreenKey();
        userOnFrame.execute();
    }

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Text Dialogue (output of information texts)

            newPauseMenu();

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
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

    private Entity newPauseMenu() {
        Entity entity = UITools.generateNewTextDialog("Pause", "Continue", "Pausemenu");
        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        return entity;
    }

    /** Will update the entity sets of each system and {@link Game#entities}. */
    private void updateSystems() {
        for (System system : systems.values()) {
            entities.foreachEntityInAddSet(system::showEntity);
            entities.foreachEntityInRemoveSet(system::removeEntity);
        }
        entities.update();
    }

    /**
     * Sets {@link #currentLevel} to the new level and removes all entities.
     *
     * <p>Will re-add the hero if he exists.
     */
    @Override
    public void onLevelLoad() {
        currentLevel = levelManager.currentLevel();
        removeAllEntities();
        try {
            hero().ifPresent(this::placeOnLevelStart);
            EntityFactory.newChest();
            for (int i = 0; i < 5; i++) {
                EntityFactory.getRandomizedMonster();
            }
        } catch (MissingComponentException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            // will be moved to MAIN in https://github.com/Programmiermethoden/Dungeon/pull/688
            LOGGER.warning("Could not create new Chest: " + e.getMessage());
            throw new RuntimeException();
        }
        hero().ifPresent(Game::addEntity);

        userOnLevelLoad.execute();
    }

    /**
     * If the given entity is on the end-tile, load the new level
     *
     * @param hero entity to check for, normally this is the hero
     */
    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero)) {
            levelManager.loadLevel(LEVELSIZE);
        }
    }

    /**
     * Check if the given en entity is on the end-tile
     *
     * @param entity entity to check for
     * @return true if the entity is on the end-tile, false if not
     */
    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        Tile currentTile = tileAT(pc.position());
        return currentTile.equals(endTile());
    }

    /**
     * Set the position of the given entity to the position of the level-start.
     *
     * <p>A {@link PositionComponent} is needed.
     *
     * @param entity entity to set on the start of the level, normally this is the hero.
     */
    private void placeOnLevelStart(Entity entity) {
        entities.add(entity);
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
        addSystem(new CameraSystem());
        addSystem(new VelocitySystem());
        addSystem(new DrawSystem(painter));
        addSystem(new PlayerSystem());
        addSystem(new HudSystem());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage().ifPresent(x -> x.getViewport().update(width, height, true));
    }
}
