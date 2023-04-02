package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import configuration.Configuration;
import configuration.KeyboardConfig;
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
import graphic.hud.PauseMenu;
import interpreter.DSLInterpreter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelSize;
import tools.Constants;
import tools.Point;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader {

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
    private static Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    private static Set<Entity> entitiesToRemove = new HashSet<>();
    /** All entities to be added from the dungeon in the next frame */
    private static Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu pauseMenu;
    private PositionComponent heroPositionComponent;
    private static Entity hero;
    private Logger gameLogger;

    /** Called once at the beginning of the game. */
    protected void setup() {
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        controller.clear();
        systems = new SystemController();
        controller.add(systems);
        pauseMenu = new PauseMenu();
        controller.add(pauseMenu);
        hero = new Hero(new Point(0, 0));
        heroPositionComponent =
                (PositionComponent)
                        hero.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);

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

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        camera.setFocusPoint(heroPositionComponent.getPosition());
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
        if (isOnEndTile()) levelAPI.loadLevel(LEVELSIZE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
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

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void firstFrame() {
        doFirstFrame = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        setup();
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
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

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();

        entities.clear();
        entities.add(hero);
        heroPositionComponent.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());

        // TODO: when calling this before currentLevel is set, the default ctor of PositionComponent
        // triggers NullPointerException
        setupDSLInput();
    }

    /** Toggle between pause and run */
    public static void togglePause() {
        paused = !paused;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (pauseMenu != null) {
            if (paused) pauseMenu.showMenu();
            else pauseMenu.hideMenu();
        }
    }

    private boolean isOnEndTile() {
        Tile currentTile =
                currentLevel.getTileAt(heroPositionComponent.getPosition().toCoordinate());
        if (currentTile.equals(currentLevel.getEndTile())) return true;

        return false;
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
    public static Optional <Entity> getHero(){
        return Optional.ofNullable(hero);
    }
}
