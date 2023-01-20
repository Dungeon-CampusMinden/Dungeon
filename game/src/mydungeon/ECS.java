package mydungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import controller.Game;
import dslToGame.QuestConfig;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.systems.*;
import interpreter.DSLInterpreter;
import java.util.*;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import starter.DesktopLauncher;
import tools.Point;

public class ECS extends Game {

    public static Set<Entity> entities = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;

    private Hero hero;
    private PositionComponent heroPositionComponent;

    @Override
    protected void setup() {
        controller.clear();
        systems = new SystemController();
        controller.add(systems);
        hero = new Hero(new Point(0, 0));
        heroPositionComponent = (PositionComponent) hero.getComponent(PositionComponent.name);
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel();

        new VelocitySystem();
        new DrawSystem(painter);
        new KeyboardSystem();
    }

    @Override
    protected void frame() {
        camera.setFocusPoint(heroPositionComponent.getPosition());

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

    /** Toggle between pause and run */
    public static void togglePause() {
        if (systems != null) {
            systems.forEach(s -> s.toggleRun());
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
                x_speed: 0.5,
                y_speed: 0.5,
                move_right_animation:"monster/imp/runRight",
                move_left_animation: "monster/imp/runLeft"
                },
                animation_component{
                    idle_left: "monster/imp/idleLeft",
                    idle_right: "monster/imp/idleRight",
                    current_animation: "monster/imp/idleLeft"
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
