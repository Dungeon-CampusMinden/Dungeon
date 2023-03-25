package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import controller.AbstractController;
import controller.SystemController;
import dslToGame.QuestConfig;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.systems.*;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.menus.*;
import graphic.hud.menus.startmenu.GameMode;
import graphic.hud.menus.startmenu.IStartMenuObserver;
import graphic.hud.menus.startmenu.StartMenu;
import interpreter.DSLInterpreter;

import java.util.*;

import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.*;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import mp.client.IMultiplayerClientObserver;
import mp.client.MultiplayerClient;
import mp.packages.request.InitializeServerRequest;
import tools.Constants;
import tools.Point;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader, IStartMenuObserver, IMultiplayerClientObserver {
    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contais all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    protected DungeonCamera camera;
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

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static Menu activeMenu;
    private static PauseMenu pauseMenu;
    private static StartMenu startMenu;
    private PositionComponent heroPositionComponent;
    private static MultiplayerClient client;
    public static Hero hero;

    /** Called once at the beginning of the game. */
    protected void setup() {
        client = new MultiplayerClient();
        controller.clear();
        systems = new SystemController();
        controller.add(systems);

        setupMenus();
        setupHero();
        setupLevel();
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
        setupDSLInput();
    }

    @Override
    public void onGameModeChosen(GameMode gameMode) {
        // refresh level because was loaded on setup as background for start menu
        setupLevel();

        switch (gameMode) {
            case SinglePlayer -> {
                // Nothing to do for now. Everything ready for single player
            }
            case MultiplayerClient -> {
                // TODO: configure client as slave
            }
            case MultiplayerHost -> {
                // TODO: configure client as host
            }
        }

        hideMenu(startMenu);
    }

    @Override
    public void onServerInitializedReceived(boolean isSucceed) {
        // TODO: do some stuff
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
        entitiesToRemove.clear();
        if (isOnEndTile()) levelAPI.loadLevel();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !startMenu.isVisible()) togglePause();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (paused) togglePause();
            startMenu.resetView();
            showMenu(startMenu);
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

    private void setupLevel() {
        levelAPI = new LevelAPI(batch, painter, this, new WallGenerator(new RandomWalkGenerator()));
        levelAPI.loadLevel();
    }

    private void setupSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new KeyboardSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
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
        ai_component {
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
//        levelAPI = new LevelAPI(batch, painter, this, generator);
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
        DesktopLauncher.run(new Game());
    }
}
