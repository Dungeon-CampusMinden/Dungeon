package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import configuration.Configuration;
import configuration.KeyboardConfig;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import controller.AbstractController;
import controller.SystemController;
import dslToGame.AnimationBuilder;
import dslToGame.QuestConfig;
import ecs.components.AnimationComponent;
import ecs.components.Component;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.mp.MultiplayerComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.HeroDummy;
import ecs.systems.*;
import graphic.Animation;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.menus.*;
import graphic.hud.menus.startmenu.IStartMenuObserver;
import graphic.hud.menus.startmenu.StartMenu;
import interpreter.DSLInterpreter;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.*;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelSize;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import mp.client.IMultiplayerClientObserver;
import mp.client.MultiplayerClient;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.server.MultiplayerServer;
import tools.Constants;
import tools.Point;

import javax.swing.text.Position;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader, IStartMenuObserver, IMultiplayerClientObserver {

    private final LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contais all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doFirstFrame = true;
    static boolean paused = false;

    /** All entities that are currently active in the dungeon */
    public static Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    public static Set<Entity> entitiesToRemove = new HashSet<>();

    public static Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu pauseMenu;
    private static StartMenu startMenu;
    private PositionComponent heroPositionComponent;
    private Logger gameLogger;
    private static MultiplayerClient multiplayerClient;
    private static MultiplayerServer multiplayerServer;
    private static int playerId;
    public static Hero hero;

    private static HashMap<Integer, Point> playerPositions;
    private HashMap<Integer, Entity> playerEntities = new HashMap<Integer, Entity>();

    /** Called once at the beginning of the game. */
    protected void setup() {
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        controller.clear();
        systems = new SystemController();
        controller.add(systems);

        setupClient();
        setupMenus();
        setupHero();
        setupRandomLevel();
        setupSystems();

        showMenu(startMenu);
    }

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
            if (doFirstFrame) {
                firstFrame();
            }
            batch.setProjectionMatrix(camera.combined);
            if (runLoop()) {
                frame();
                if (runLoop()) {
                    clearScreen();
                    levelAPI.update();
                    if (runLoop()) {
                        controller.forEach(AbstractController::update);
                        if (runLoop()) {
                            camera.update();
                        }
                    }
                }
            }
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();

        entities.clear();
        if (hero != null) {
            entities.add(hero);
            heroPositionComponent.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
        }

        // TODO: when calling this before currentLevel is set, the default ctor of PositionComponent
        // triggers NullPointerException
//        setupDSLInput();
    }

    @Override
    public void onSinglePlayerModeChosen() {
        // Nothing to do for now. Everything ready for single player but for now just refresh level
        setupRandomLevel();
        hideMenu(startMenu);
    }

    @Override
    public void onMultiPlayerHostModeChosen() {
        setupServer();
        // Check whether which random port is not already in use and listen to this on serverside
        boolean isRandomPortAlreadyInUse = false;
        int serverPort;
        do {
            // Create random 5 digit port
            serverPort = ThreadLocalRandom.current().nextInt(10000, 65535 + 1);
            try {
                multiplayerServer.startListening(serverPort);
            } catch (Exception e) {
                isRandomPortAlreadyInUse = true;
            }
        } while(isRandomPortAlreadyInUse);
        if (!multiplayerClient.connectToHost("127.0.0.1", serverPort)) {
            // TODO: error handling
            System.out.println(String.format("Could not connect to host on this device at port %d.", serverPort));
        } else {
            // refresh level because was loaded on setup as background for start menu
            setupRandomLevel();
            multiplayerClient.send(new InitializeServerRequest(currentLevel));
        }
    }

    @Override
    public void onMultiPlayerClientModeChosen(String hostAddress, Integer port) {
        // TODO: configure client as slave
        if (!multiplayerClient.connectToHost(hostAddress, port)) {
            // TODO: error handling like popup menu with error message
            System.out.println(String.format("Could not connect to host %s:%d", hostAddress, port));
        } else {
            multiplayerClient.send(new JoinSessionRequest());
        }
    }

    @Override
    public void onServerInitializedReceived(boolean isSucceed, int id) {
        // TODO: do some stuff
        if (isSucceed) {
            playerId = id;
            playerEntities.put(id, hero);
            hideMenu(startMenu);
            sendPosition();
        } else {
            // TODO: error handling like popup menu with error message
            System.out.println("Multiplayer host session failed to initialize");
        }
    }

    @Override
    public void onSessionJoined(ILevel level, int id) {
        // TODO: do some stuff
        if (level != null) {
            levelAPI.setLevel(level);
            playerId = id;
            hideMenu(startMenu);
            sendPosition();

            // Creating HeroDummy
            //playerEntities.put(id,new HeroDummy(new Point(0.0f,0.0f)));

        } else {
            // TODO: error handling like popup menu with error message
            System.out.println("Multiplayer host session failed to initialize");
        }
    }

    private void sendPosition(){
        PositionComponent pos = (PositionComponent) hero.getComponent(PositionComponent.class).orElseThrow();
        UpdateOwnPositionRequest posReq = new UpdateOwnPositionRequest(playerId, pos.getPosition());

        multiplayerClient.send(posReq);
    }

    @Override
    public void onPositionUpdate(HashMap playerPositions){
        this.playerPositions = playerPositions;

        playerPositions.forEach((id, position) -> {
            if(playerId == (int) id){
                return;
            } else if(!playerEntities.containsKey(id)){
                playerEntities.put((int) id, new HeroDummy((Point)position));
            }

            Entity herotestdummy = playerEntities.get(id);
            PositionComponent pc = (PositionComponent) herotestdummy.getComponent(PositionComponent.class).orElseThrow();
            pc.setPosition((Point)position);
        });
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        if (hero != null && heroPositionComponent != null) {
            camera.setFocusPoint(heroPositionComponent.getPosition());
        }
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
        if (isOnEndTile()) levelAPI.loadLevel(LEVELSIZE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !startMenu.isVisible()) togglePause();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (paused) togglePause();
            if (!startMenu.isVisible()) {
                startMenu.resetView();
                showMenu(startMenu);
            }
        }
    }

    protected boolean runLoop() {
        return true;
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    private void setupRandomLevel() {
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel();
    }

    private void setupSystems() {
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

    private void setupDSLInput() {
        String program =
            """
    game_object monster {
        position_component {
        },
        velocity_component {
        x_velocity: 0.1,
        y_velocity: 0.1,
        move_right_animation:"monster/imp/runRight",
        move_left_animation: "monster/imp/runLeft"
        },
        animation_component{
            idle_left: "monster/imp/idleLeft",
            idle_right: "monster/imp/idleRight",
            current_animation: "monster/imp/idleLeft"
        },
        hitbox_component {
        }
    }

    quest_config config {
        entity: monster
    }
    """;
        DSLInterpreter interpreter = new DSLInterpreter();
        QuestConfig config = (QuestConfig) interpreter.getQuestConfig(program);
        entities.add(config.entity());
    }

    private void setupHero() {
        hero = new Hero(new Point(0, 0));
        heroPositionComponent =
            (PositionComponent)
                hero.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
    }

    private void setupServer() {
        multiplayerServer = new MultiplayerServer();
    }

    private void setupClient() {
        multiplayerClient = new MultiplayerClient();
        multiplayerClient.addObserver(this);
    }

    private void setupMenus() {
        pauseMenu = new PauseMenu();
        startMenu = new StartMenu();
        if (!startMenu.addObserver(this)) {
            throw new RuntimeException("Failed to register observer to start menu");
        };

        if (controller != null) {
            controller.add(pauseMenu);
            controller.add(startMenu);
        }
    }

    private void showMenu(Menu menuToBeShown) {
        if (menuToBeShown != null) {
            stopSystems();
            menuToBeShown.showMenu();
        }
    }

    private void hideMenu(Menu menu) {
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

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void firstFrame() {
        doFirstFrame = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
//        generator = new RandomWalkGenerator();
//        levelAPI = new LevelAPI(batch, painter, generator, this);
        setup();
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

    private boolean isOnEndTile() {
        if (hero != null && heroPositionComponent != null) {
            Tile currentTile =
                    currentLevel.getTileAt(heroPositionComponent.getPosition().toCoordinate());
            return currentTile.equals(currentLevel.getEndTile());
        }

        return false;
    }

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DesktopLauncher.run(new Game());
    }
}
