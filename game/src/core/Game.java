package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import static core.utils.logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;





import contrib.configuration.KeyboardConfig;
import contrib.systems.DebuggerSystem;

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
import core.level.utils.LevelSize;
import core.systems.CameraSystem;
import core.systems.DrawSystem;

import core.systems.PlayerSystem;
import core.systems.VelocitySystem;
import core.utils.*;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;


import quizquestion.DummyQuizQuestionList;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** The heart of the framework. From here all strings are pulled. */
public final class Game extends ScreenAdapter implements IOnLevelLoader {

    /**
     * A Map with each {@link System} in the game.
     *
     * <p>The Key-Value is the Class of the system
     */
    public static final Map<Class<? extends System>, System> systems = new HashMap<>();
    /** All entities that are currently active in the dungeon */
    private static final DelayedSet<Entity> entities = new DelayedSet<>();

    private static final Logger LOGGER = Logger.getLogger("Game");
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static int WINDOW_WIDTH = 640;
    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static int WINDOW_HEIGHT = 480;
    /**
     * Part of the pre-run configuration. The fps of the game (frames per second)
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static int FRAME_RATE = 30;
    /**
     * Part of the pre-run configuration. The tilte of the Game-Window.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static String WINDOW_TITLE = "PM-Dungeon";
    /**
     * Part of the pre-run configuration. The path (as String) to the logo of the Game-Window.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static String LOGO_PATH = "logo/CatLogo_35x35.png";
    /** Currently used level-size configuration for generating new level */
    public static LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The currently loaded level of the game.
     *
     * @see ILevel
     * @see LevelManager
     */
    public static ILevel currentLevel;
    /**
     * Part of the pre-run configuration.
     * This function will be called at each frame.
     * <p> Use this, if you want to execute some logic outside of a sytem.</p>
     * <p> Will not replace {@link #onFrame )</p>
     */
    public static IVoidFunction userFrame = () -> {};
    /**
     * Part of the pre-run configuration. This function will be called after a level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * <p>Will not replace {@link #onLevelLoad} )
     */
    public static IVoidFunction userOnLevelLoad = () -> {};
    /**
     * Part of the pre-run configuration. If this value is true, the audio for the game will be
     * disabled.
     *
     * <p>Manipulating this value will only result in changes before {@link Game#run} was executed.
     */
    public static boolean DISABLE_AUDIO = false;

    private static Entity hero;
    private static Stage stage;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    private SpriteBatch batch;
    /** Draws objects */
    private Painter painter;

    private LevelManager levelManager;
    private boolean doSetup = true;
    private DebuggerSystem debugger;

    // for singleton
    private Game() {}

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
    public static void userFrame(IVoidFunction userFrame) {
        Game.userFrame = userFrame;
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
    public static void loadConfig(String pathAsString, Class<?> klass) throws IOException {
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
        levelManager = new LevelManager(batch, painter, generator, this);
        initBaseLogger();
        levelManager =
                new LevelManager(
                        batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelManager.loadLevel(LEVELSIZE);
        createSystems();

        setupStage();
    }

    private static void setupStage() {
        stage =
                new Stage(
                        new ScalingViewport(
                                Scaling.stretch, WINDOW_WIDTH, WINDOW_HEIGHT),
                        new SpriteBatch());
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Called at the beginning of each frame, before the entities are updated and the systems are
     * executed.
     *
     * <p>This is the place to add basic logic that isn't part of any system.
     */
    private void onFrame() {
        hero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        debugKeys();
        userFrame.execute();
    }




   private boolean uiDebugFlag = false;

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Text Dialogue (output of information texts)

            newPauseMenu();

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            // Dialogue for quiz questions (display of quiz questions and the answer area in test
            // mode)
            UITools.showQuizDialog(DummyQuizQuestionList.getRandomQuestion());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            // toggle UI "debug rendering"
            stage().ifPresent(x -> x.setDebugAll(uiDebugFlag = !uiDebugFlag));
        }
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_KEY.get())) {
            debugger.toggleRun();
            LOGGER.info("Debugger ist now " + debugger.isRunning());
        }
    }

    private Entity newPauseMenu() {
        Entity entity = UITools.generateNewTextDialog("Pause", "Continue", "Pausemenu");
        entity.getComponent(UIComponent.class)
                .map(UIComponent.class::cast)
                .ifPresent(y -> y.getDialog().setVisible(true));

        return entity;
    }



    /**
     * Will update the entity sets of each system and {@link Game#entities}.
     */
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
        currentLevel = levelManager.getCurrentLevel();
        removeAllEntities();
        hero().ifPresent(this::placeOnLevelStart);
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
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Tile currentTile = tileAT(pc.getPosition());
        return currentTile.equals(currentLevel.getEndTile());
    }

    /**
     * Set the position of the given entity to the position of the level-start.
     *
     * <p>A {@link PositionComponent} is needed.
     *
     * @param hero entity to set on the start of the level, normally this is the hero.
     */
    private void placeOnLevelStart(Entity hero) {
        entities.add(hero);
        PositionComponent pc =
                (PositionComponent)
                        hero.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
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
        // Debugger should not be a system, see #651
        debugger = new DebuggerSystem();
        addSystem(debugger);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage().ifPresent(x -> x.getViewport().update(width, height, true));
    }
}
