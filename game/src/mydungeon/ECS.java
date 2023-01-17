package mydungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import controller.Game;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.systems.*;
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

    @Override
    protected void setup() {
        controller.clear();
        systems = new SystemController();
        controller.add(systems);
        hero = new Hero(new Point(0, 0));
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel();

        new VelocitySystem();
        new DrawSystem(painter);
        new KeyboardSystem();
    }

    @Override
    protected void frame() {
        camera.setFocusPoint(hero.getPositionComponent().getPosition());

        if (isOnEndTile(hero)) levelAPI.loadLevel();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clear();
        entities.add(hero);
        hero.getPositionComponent()
                .setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /** Toggle between pause and run */
    public static void togglePause() {
        if (systems != null) {
            systems.forEach(s -> s.toggleRun());
        }
    }

    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        if (pc != null) {
            Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
            if (currentTile.equals(currentLevel.getEndTile())) return true;
        }
        return false;
    }
   
    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new ECS());
    }
}
