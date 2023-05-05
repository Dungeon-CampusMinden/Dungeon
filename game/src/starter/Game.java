package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import configuration.Configuration;
import configuration.KeyboardConfig;
import controller.AbstractController;
import controller.SystemController;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.mp.MultiplayerComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.HeroDummy;
import ecs.systems.*;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.menus.*;
import graphic.hud.menus.startmenu.IStartMenuObserver;
import graphic.hud.menus.startmenu.StartMenu;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.*;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelSize;
import mp.IMultiplayer;
import mp.MultiplayerAPI;
import tools.Constants;
import tools.Point;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader, IStartMenuObserver, IMultiplayer {

    private final LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contains all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doSetup = true;
    private static boolean paused = false;

    /** All entities that are currently active in the dungeon */
    private static final Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    private static final Set<Entity> entitiesToRemove = new HashSet<>();
    /** All entities to be added from the dungeon in the next frame */
    private static final Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu<Actor> pauseMenu;
    private static StartMenu<Actor> startMenu;
    private static Entity hero;
    private Logger gameLogger;
    private static MultiplayerAPI multiplayerAPI;

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DesktopLauncher.run(new Game());
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
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        systems = new SystemController();
        controller.add(systems);
        hero = new Hero();
        multiplayerAPI = new MultiplayerAPI(this);
        setupMenus();
        setupRandomLevel();
        createSystems();
        showMenu(startMenu);
    }

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        setCameraFocus();
        manageEntitiesSets();
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !startMenu.isVisible()) togglePause();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (paused) togglePause();
            if (!startMenu.isVisible()) {
                startMenu.resetView();
                showMenu(startMenu);
            }
        }
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clear();
        getHero().ifPresent(this::placeOnLevelStart);
    }

    private void manageEntitiesSets() {
        synchronizePositionsFromMultiplayerSession();
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        for (Entity entity : entitiesToAdd) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was added.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
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

    /**
     * Given entity will be added to the game in the next frame
     *
     * @param entity will be added to the game next frame
     */
    public static void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Given entity will be removed from the game in the next frame
     *
     * @param entity will be removed from the game next frame
     */
    public static void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    /**
     * @return Set with all entities currently in game
     */
    public static Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return Set with all entities that will be added to the game next frame
     */
    public static Set<Entity> getEntitiesToAdd() {
        return entitiesToAdd;
    }

    /**
     * @return Set with all entities that will be removed from the game next frame
     */
    public static Set<Entity> getEntitiesToRemove() {
        return entitiesToRemove;
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

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
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
    }

    private void setupRandomLevel() {
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel();
    }

    private void setupMenus() {
        pauseMenu = new PauseMenu<>();
        startMenu = new StartMenu<>();
        if (!startMenu.addObserver(this)) {
            throw new RuntimeException("Failed to register observer to start menu");
        }

        if (controller != null) {
            controller.add(pauseMenu);
            controller.add(startMenu);
        }
    }

    private void showMenu(Menu<Actor> menuToBeShown) {
        if (menuToBeShown != null) {
            stopSystems();
            menuToBeShown.showMenu();
        }
    }

    private void hideMenu(Menu<? extends Actor> menu) {
        menu.hideMenu();
        resumeSystems();
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            showMenu(pauseMenu);
        }
        else {
            hideMenu(pauseMenu);
        }
    }

    private void resumeSystems() {
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
    }

    private void stopSystems() {
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
    }

    public static void sendPosition(){
        if (getHero().isPresent()) {
            PositionComponent positionComponent =
                (PositionComponent)
                    getHero()
                        .get()
                        .getComponent(PositionComponent.class)
                        .orElseThrow(
                            () ->
                                new MissingComponentException(
                                    "PositionComponent"));
            multiplayerAPI.updateOwnPosition(positionComponent.getPosition());
        } else {
            System.out.println("Hero position not sent. Hero not present/set.");
        }
    }

    private void synchronizePositionsFromMultiplayerSession() {

        if (getHero().isPresent() && multiplayerAPI.isConnectedToSession()) {
            final HashMap<Integer, Point> heroPositionByPlayerIdExceptOwn =
                multiplayerAPI.getHeroPositionByPlayerIdExceptOwn();

            if (heroPositionByPlayerIdExceptOwn != null) {
                PositionComponent positionComponentOwnHero =
                    (PositionComponent)
                        getHero()
                            .get()
                            .getComponent(PositionComponent.class)
                            .orElseThrow(
                                () ->
                                    new MissingComponentException(
                                        "PositionComponent"));

                //Add new hero, if new player joined
                heroPositionByPlayerIdExceptOwn.forEach((Integer playerId, Point position) -> {
                    if(entities.stream().flatMap(e -> e.getComponent(MultiplayerComponent.class).stream())
                        .map(component -> (MultiplayerComponent)component)
                        .noneMatch(component -> component.getPlayerId() == playerId)) {
                        new HeroDummy(positionComponentOwnHero.getPosition(), playerId);
                    }
                });

                // Remove entities not connected to multiplayer session anymore
                entities.stream().flatMap(e -> e.getComponent(MultiplayerComponent.class).stream())
                    .map(e -> (MultiplayerComponent) e)
                    .forEach(mc -> {
                        if(!heroPositionByPlayerIdExceptOwn.containsKey(mc.getPlayerId())){
                            entitiesToRemove.add(mc.getEntity());
                        }
                    });

                // Update all positions of all entities with a multiplayerComponent
                for (Entity entity: entities) {
                    if (entity.getComponent(MultiplayerComponent.class).isPresent()) {
                        MultiplayerComponent multiplayerComponent =
                            (MultiplayerComponent)entity.getComponent(MultiplayerComponent.class).orElseThrow();
                        PositionComponent positionComponent =
                            (PositionComponent) entity.getComponent(PositionComponent.class).orElseThrow();
                        Point currentPositionAtMultiplayerSession =
                            multiplayerAPI.getHeroPositionByPlayerId().get(multiplayerComponent.getPlayerId());
                        positionComponent.setPosition(currentPositionAtMultiplayerSession);
                    }
                }
            }
        }
    }

    @Override
    public void onSinglePlayerModeChosen() {
        // Nothing to do for now. Everything ready for single player but for now just refresh level
        setupRandomLevel();
        hideMenu(startMenu);
    }

    @Override
    public void onMultiPlayerHostModeChosen() {
        setupRandomLevel();
        try {
            multiplayerAPI.startSession(currentLevel);
        } catch (Exception e) {
            // TODO: Nicer error handling
            System.out.println("Multiplayer session failed to start.");
            e.printStackTrace();
        }
    }

    @Override
    public void onMultiPlayerClientModeChosen(final String hostAddress, final Integer port) {
        try {
            multiplayerAPI.joinSession(hostAddress, port);
        } catch (Exception e) {
            // TODO: Nicer error handling
            System.out.println("Multiplayer session failed to join.");
            e.printStackTrace();
        }
    }

    @Override
    public void onMultiplayerSessionStarted(final boolean isSucceed) {
        if (isSucceed) {
            hideMenu(startMenu);
            sendPosition();
        } else {
            // TODO: error handling like popup menu with error message
            System.out.println("Server responded unsuccessful start");
        }
    }

    @Override
    public void onMultiplayerSessionJoined(final ILevel level) {
        if (level != null) {
            levelAPI.setLevel(level);
            hideMenu(startMenu);
            sendPosition();
        } else {
            // TODO: error handling like popup menu with error message
            System.out.println("Cannot join multiplayer session");
        }
    }
}
