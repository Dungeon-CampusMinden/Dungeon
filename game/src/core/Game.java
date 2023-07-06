package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import static core.utils.logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import contrib.entities.EntityFactory;
import contrib.systems.MultiplayerSynchronizationSystem;
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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import contrib.utils.multiplayer.IMultiplayer;
import contrib.utils.multiplayer.MultiplayerManager;

public class Game extends ScreenAdapter implements IOnLevelLoader, IMultiplayer {

    /* Used for singleton. */
    private static Game INSTANCE;

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
     * <p>Manipulating this value will only result in changes before {@link Dungeon#run} was executed.
     */
    private static int WINDOW_WIDTH = 640;
    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     */
    private static int WINDOW_HEIGHT = 480;

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

    private static Entity hero;

    private static Stage stage;

    private static LevelManager levelManager;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Draws objects */
    protected Painter painter;

    private boolean doSetup = true;
    private boolean uiDebugFlag = false;

    private static MultiplayerManager multiplayerManager = new MultiplayerManager(Game.getInstance());

    // for singleton
    private Game() {
    }

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
     * In the next frame, each system will be informed that the given entity has changes in its
     * Component Collection.
     *
     * @param entity the entity that has changes in its Component Collection
     */
    public static void informAboutChanges(Entity entity) {
        // Todo - is the next line necessary?
        //entities.add(entity);
//        LOGGER.info("Entity: " + entity + " informed the Game about component changes.");
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

    public void resumeSystems() {
        if (systems != null) {
            systems.forEach((klass, system) -> {
                system.run();
            });
        }
    }

    public void stopSystems() {
        if (systems != null) {
            systems.forEach((klass, system) -> {
                system.stop();
            });
        }
    }

    public static void sendPositionUpdate(final Entity entity){
        PositionComponent positionComponent =
            (PositionComponent)
                entity
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () ->
                            new MissingComponentException(
                                "PositionComponent"));
        multiplayerManager.sendPositionUpdate(entity.globalID(), positionComponent.position());
    }

    public void openToLan() {
        if (doSetup) onSetup();
        updateSystems();
        try {
            if (hero == null) {
                hero(EntityFactory.newHero());
            }
            PositionComponent positionComponent =
                hero()
                    .get()
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () ->
                            new MissingComponentException(
                                "PositionComponent"));
            multiplayerManager.startSession(currentLevel, positionComponent.position());
        } catch (Exception ex) {
            final String message = "Multiplayer session failed to start.";
            LOGGER.warning(String.format("%s\n%s", message, ex.getMessage()));
            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Error on session start.");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        }
    }

    public void joinMultiplayerSession(final String hostAddress, final Integer port) {
        if (doSetup) onSetup();
        try {
            if (hero == null) {
                hero(EntityFactory.newHero());
            }
            multiplayerManager.joinSession(hostAddress, port);
        } catch (Exception ex) {
            final String message = "Multiplayer session failed to join.";
            LOGGER.warning(String.format("%s\n%s", message, ex.getMessage()));
            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Error on join.");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        }
    }

    @Override
    public void onMultiplayerServerInitialized(final boolean isSucceed) {
        if (isSucceed) {
            changeLevel();
        } else {
            final String message = "Server respond unsuccessful start.";
            LOGGER.warning(message);
            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Error session start.");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        }
    }

    @Override
    public void onMultiplayerSessionJoined(final boolean isSucceed, final ILevel level) {
        if (isSucceed) {
            try {
                levelManager.level(level);
            }
            catch (Exception ex) {
                final String message = "Failed to set received level from server.";
                LOGGER.warning(String.format("%s\n%s", message, ex.getMessage()));
                Entity entity = UITools.generateNewTextDialog(message, "Ok", "Level error.");
                entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
            }
        } else {
            final String message = "Cannot join multiplayer session";
            LOGGER.warning(message);
            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Connection failed.");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        }
    }

    @Override
    public void onMapLoad(ILevel level) {
        levelManager.level(level);
    }

    @Override
    public void onChangeMapRequest() {
        if(multiplayerManager.isHost()) {
            levelManager.loadLevel(LEVELSIZE);
            changeLevel();
        }
    }

    @Override
    public void onMultiplayerSessionLost() {
        final String message = "Disconnected from multiplayer session.";
        LOGGER.info(message);
        Entity entity = UITools.generateNewTextDialog(message, "Ok", "Connection lost.");
        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
    }

    @Override
    public void dispose() {
        multiplayerManager.stopSession();
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
                        batch, painter, new WallGenerator(generator), this);
        levelManager.loadLevel(LEVELSIZE);
        multiplayerManager = new MultiplayerManager(this);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) ){ //&& !startMenu.isVisible()) {
            // Text Dialogue (output of information texts)
            newPauseMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
//            if (!startMenu.isVisible()) {
//                startMenu.resetView();
//                showMenu(startMenu);
//            }
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
        } catch (MissingComponentException e) {
            LOGGER.warning(e.getMessage());
        }
        hero().ifPresent(Game::addEntity);
        userOnLevelLoad.execute();
    }

    public static Game getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Game();
        }

        return INSTANCE;
    }

    /**
     * If the given entity is on the end-tile, load the new level
     *
     * @param hero entity to check for, normally this is the hero
     */
    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (!isOnEndTile(hero)) return;

        if (!multiplayerManager.isConnectedToSession()){
            levelManager.loadLevel(LEVELSIZE);
            changeLevel();
        } else {
            if(multiplayerManager.isHost()){
                levelManager.loadLevel(LEVELSIZE);
                changeLevel();
            } else {
                //ask host to generate new map
                multiplayerManager.requestNewLevel();
            }
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
        addSystem(new MultiplayerSynchronizationSystem(multiplayerManager));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage().ifPresent(x -> x.getViewport().update(width, height, true));
    }

    public static MultiplayerManager multiplayerManager() {
        return multiplayerManager;
    }

    private void changeLevel() {
        updateSystems();
        multiplayerManager.changeLevel(currentLevel, entities.stream().collect(Collectors.toSet()), hero);
    }
}
