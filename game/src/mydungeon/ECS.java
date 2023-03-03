package mydungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import controller.Game;
import dslToGame.QuestConfig;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.systems.*;
import hud.PauseMenu;
import interpreter.DSLInterpreter;
import java.util.*;
import java.util.logging.Logger;

import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import starter.DesktopLauncher;
import tools.Point;

import static logging.LoggerConfig.initBaseLogger;

public class ECS extends Game {

    /** All entities that are currently active in the dungeon */
    public static Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    public static Set<Entity> entitiesToRemove = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu pauseMenu;
    private PositionComponent heroPositionComponent;
    public static Hero hero;
    private Logger ecsLogger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    protected void setup() {
        initBaseLogger();

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
        levelAPI.loadLevel();

        new VelocitySystem();
        new DrawSystem(painter);
        new KeyboardSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
    }

    @Override
    protected void frame() {
        camera.setFocusPoint(heroPositionComponent.getPosition());
        entities.removeAll(entitiesToRemove);
        for (Entity entity : entitiesToRemove) {
            ecsLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        entitiesToRemove.clear();
        if (isOnEndTile()) levelAPI.loadLevel();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
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

    static boolean paused = false;
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
        DesktopLauncher.run(new ECS());
    }
}
