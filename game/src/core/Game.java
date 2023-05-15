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
import core.systems.*;
import core.systems.PlayerSystem;
import core.utils.*;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;
import core.utils.components.draw.TextureHandler;
import core.utils.controller.AbstractController;
import core.utils.controller.SystemController;
import core.utils.logging.CustomLogLevel;

import quizquestion.DummyQuizQuestionList;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader {

    /** Currently used level-size configuration for generating new level */
    public static LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contains all Controller of the Dungeon */
    public static List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelManager levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doSetup = true;

    /** A handler for managing asset paths */
    private static TextureHandler handler;

    /** All entities that are currently active in the dungeon */
    private static final DelayedEntitySet entities = new DelayedEntitySet();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static Entity hero;
    private static Logger LOGGER = Logger.getLogger("Game");
    ;

    private DebuggerSystem debugger;
    private static Game game;

    /**
     * Create a new Game instance if no instance currently exist.
     *
     * @return the (new) Game instance
     */
    public static Game newGame() {
        if (game == null) game = new Game();
        return game;
    }

    // for singleton
    private Game() {}

    public static void updateEntity(Entity entity) {
        LOGGER.log(CustomLogLevel.INFO, entity + "was updated in Game.");
        if (systems != null && getEntities().contains(entity))
            systems.forEach(system -> system.accept(entity));
    }

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
        if (doSetup) setup();
        batch.setProjectionMatrix(camera.combined);
        frame();
        clearScreen();
        levelAPI.update();
        controller.forEach(AbstractController::update);
        camera.update();
    }

    /** Called once at the beginning of the game. */
    protected void setup() {
        doSetup = false;
        /*
         * THIS EXCEPTION HANDLING IS A TEMPORARY WORKAROUND !
         *
         * <p>The TextureHandler can throw an exception when it is first created. This exception
         * (IOException) must be handled somewhere. Normally we want to pass exceptions to the method
         * caller. This approach is (atm) not possible in the libgdx render method because Java does
         * not allow extending method signatures derived from a class. We should try to make clean
         * code out of this workaround later.
         *
         * <p>Please see also discussions at:<br>
         * - https://github.com/Programmiermethoden/Dungeon/pull/560<br>
         * - https://github.com/Programmiermethoden/Dungeon/issues/587<br>
         */
        try {
            handler = TextureHandler.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller = new ArrayList<>();
        batch = new SpriteBatch();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelManager(batch, painter, generator, this);
        initBaseLogger();
        systems = new SystemController();
        controller.add(systems);
        hero = EntityFactory.getHero();
        levelAPI =
                new LevelManager(
                        batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);
        createSystems();
    }

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        entities.update();
        setCameraFocus();
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Text Dialogue (output of information texts)
            UITools.showInfoText(Constants.DEFAULT_MESSAGE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            // Dialogue for quiz questions (display of quiz questions and the answer area in test
            // mode)
            DummyQuizQuestionList.getRandomQuestion().askQuizQuestionWithUI();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) UITools.showInfoText();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_KEY.get())) {
            debugger.toggleRun();
            LOGGER.info("Debugger ist now " + debugger.isRunning());
        }
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clearExcept(hero);
        getHero().ifPresent(this::placeOnLevelStart);
        EntityFactory.getChest();
    }

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

    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero)) levelAPI.loadLevel(LEVELSIZE);
    }

    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
        return currentTile.equals(currentLevel.getEndTile());
    }

    private void placeOnLevelStart(Entity hero) {
        entities.add(hero);
        PositionComponent pc =
                (PositionComponent)
                        hero.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
    }

    public static TextureHandler getHandler() {
        return handler;
    }

    /**
     * Given entity will be added to the game in the next frame
     *
     * @param entity will be added to the game next frame
     */
    public static void addEntity(Entity entity) {

        LOGGER.log(CustomLogLevel.INFO, "Entity: " + entity + " will be added from the Game.");
        entities.add(entity);
    }

    /**
     * Given entity will be removed from the game in the next frame
     *
     * @param entity will be removed from the game next frame
     */
    public static void removeEntity(Entity entity) {

        LOGGER.log(CustomLogLevel.INFO, "Entity: " + entity + " will be removed from the Game.");
        entities.remove(entity);
    }

    /**
     * @return Copy of the Set with all entities currently in game
     */
    public static Set<Entity> getEntities() {
        return entities.getSet();
    }

    /**
     * @return The {@link DelayedSet} to manage all the entities in the ecs
     */
    public static DelayedSet getDelayedEntitySet() {
        return entities;
    }

    /**
     * @return the player character, can be null if not initialized
     */
    public static Optional<Entity> getHero() {
        return Optional.ofNullable(hero);
    }

    /**
     * set the reference of the playable character careful: old hero will not be removed from the
     * game
     *
     * @param hero new reference of hero
     */
    public static void setHero(Entity hero) {
        Game.hero = hero;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    private void createSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new PlayerSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();
        debugger = new DebuggerSystem();
    }

    /**
     * Load the configuration from the given path. If the configuration has already been loaded, the
     * cached version will be used.
     *
     * @param pathAsString Path to the config-file as String
     * @param klass Class where the ConfigKey field are located.
     * @throws IOException If the file could not be read
     */
    public static void loadConfig(String pathAsString, Class klass) throws IOException {
        Configuration.loadAndGetConfiguration(pathAsString, klass);
    }

    /** Starts the dungeon and needs a {@link Game}. */
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
}
