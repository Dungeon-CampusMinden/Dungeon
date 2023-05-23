package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import static core.utils.logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import contrib.configuration.KeyboardConfig;
import contrib.entities.EntityFactory;
import contrib.systems.*;

import core.components.PositionComponent;
import core.configuration.Configuration;
import core.hud.UITools;
import core.level.IOnLevelLoader;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.generator.postGeneration.WallGenerator;
import core.level.generator.randomwalk.RandomWalkGenerator;
import core.level.utils.LevelSize;
import core.systems.DrawSystem;
import core.systems.PlayerSystem;
import core.systems.VelocitySystem;
import core.utils.Constants;
import core.utils.DelayedSet;
import core.utils.DungeonCamera;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;
import core.utils.controller.AbstractController;

import quizquestion.DummyQuizQuestionList;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** The heart of the framework. From here all strings are pulled. */
public final class Game extends ScreenAdapter implements IOnLevelLoader {

    /** Contains all Controller of the Dungeon */
    public static final List<AbstractController<?>> controller = new ArrayList<>();
    /** Set of all Systems in the ECS */
    public static final Map<Class<? extends System>, System> systems = new HashMap<>();
    /** All entities that are currently active in the dungeon */
    private static final DelayedSet<Entity> entities = new DelayedSet<>();

    private static final Logger LOGGER = Logger.getLogger("Game");
    /** Currently used level-size configuration for generating new level */
    public static LevelSize LEVELSIZE = LevelSize.SMALL;

    public static DungeonCamera camera;
    public static ILevel currentLevel;
    private static Entity hero;
    private static Game game;
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
     * Create a new Game instance if no instance currently exists.
     *
     * @return the (new) Game instance
     */
    public static Game newGame() {
        if (game == null) game = new Game();
        return game;
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
    public static Stream<Entity> getEntitiesStream() {
        return entities.stream();
    }

    /**
     * @return the player character, can be null if not initialized
     * @see Optional
     */
    public static Optional<Entity> getHero() {
        return Optional.ofNullable(hero);
    }

    /**
     * Set the reference of the playable character.
     *
     * <p>Be careful: the old hero will not be removed from the game.
     *
     * @param hero the new reference of the hero
     */
    public static void setHero(Entity hero) {
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
        config.setWindowSizeLimits(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 9999, 9999);
        // The third and fourth parameters ("maxWidth" and "maxHeight") affect the resizing
        // behavior
        // of the window. If the window is enlarged or maximized, then it can assume these
        // dimensions at maximum. If you have a larger screen resolution than 9999x9999 pixels,
        // increase these parameters.
        config.setForegroundFPS(Constants.FRAME_RATE);
        config.setTitle(Constants.WINDOW_TITLE);
        config.setWindowIcon(Constants.LOGO_PATH);
        // config.disableAudio(true);
        // uncomment this if you wish no audio
        new Lwjgl3Application(
                new com.badlogic.gdx.Game() {
                    @Override
                    public void create() {
                        setScreen(Game.newGame());
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

    /**
     * Main game loop.
     *
     * <p>Redraws the dungeon, updates the entity sets, and triggers the execution of the systems.
     * Will call {@link #frame}.
     *
     * @param delta the time since the last loop
     */
    @Override
    public void render(float delta) {
        if (doSetup) setup();
        batch.setProjectionMatrix(camera.combined);
        frame();
        clearScreen();
        levelManager.update();
        updateSystems();
        systems.values().stream().filter(System::isRunning).forEach(System::execute);
        // screen controller
        controller.forEach(AbstractController::update);
        setCameraFocus();
        camera.update();
    }

    /**
     * Called once at the beginning of the game.
     *
     * <p>Will perform some setup.
     */
    private void setup() {
        doSetup = false;
        batch = new SpriteBatch();
        setupCameras();
        painter = new Painter(batch, camera);
        IGenerator generator = new RandomWalkGenerator();
        levelManager = new LevelManager(batch, painter, generator, this);
        initBaseLogger();
        hero = EntityFactory.getHero();
        levelManager =
                new LevelManager(
                        batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelManager.loadLevel(LEVELSIZE);
        createSystems();
    }

    /**
     * Called at the beginning of each frame, before the entities are updated and the systems are
     * executed.
     *
     * <p>This is the place to add basic logic that isn't part of any system.
     */
    private void frame() {
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        debugKeys();
    }

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Text Dialogue (output of information texts)
            UITools.showInfoText(Constants.DEFAULT_MESSAGE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            // Dialogue for quiz questions (display of quiz questions and the answer area in test
            // mode)
            DummyQuizQuestionList.getRandomQuestion().askQuizQuestionWithUI();
        }
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_KEY.get())) {
            debugger.toggleRun();
            LOGGER.info("Debugger ist now " + debugger.isRunning());
        }
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
        currentLevel = levelManager.getCurrentLevel();
        removeAllEntities();
        getHero().ifPresent(this::placeOnLevelStart);
        getHero().ifPresent(Game::addEntity);
        EntityFactory.getChest();
    }

    /** Set the focus of the camera on the hero, if he exists otherwise focus on Pont (0,0) */
    private void setCameraFocus() {
        if (getHero().isPresent()) {
            PositionComponent pc =
                    (PositionComponent)
                            getHero()
                                    .get()
                                    .getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            camera.setFocusPoint(pc.getPosition());

        } else camera.setFocusPoint(new Point(0, 0));
    }

    /**
     * If the given entity is on the end-tile, load the new level
     *
     * @param hero entity to check for, normally this is the hero
     */
    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero)) levelManager.loadLevel(LEVELSIZE);
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
        Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
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

    /** Create a new Camera and set the default values. */
    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    /** Create the systems. */
    private void createSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new PlayerSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new ProjectileSystem();
        debugger = new DebuggerSystem();
    }
}
