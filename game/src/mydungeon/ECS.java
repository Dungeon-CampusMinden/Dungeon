package mydungeon;

import controller.Game;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import ecs.systems.ECS_System;
import ecs.systems.MovementSystem;
import java.util.*;
import level.LevelAPI;
import level.elements.ILevel;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import starter.DesktopLauncher;

public class ECS extends Game {
    /** Map with all PositionComponents in the ECS. TODO: HOW TO DELETE? */
    public static Map<Entity, PositionComponent> positionComponentMap;
    /** Map with all VelocityComponent in the ECS. TODO: HOW TO DELETE? */
    public static Map<Entity, VelocityComponent> velocityComponentMap;

    /** List of all Systems in the ECS */
    public static List<ECS_System> systems;

    public static ILevel currentLevel;

    @Override
    protected void setup() {
        // controller.clear();
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel();
        systems = new ArrayList<>();
        positionComponentMap = new HashMap<>();
        velocityComponentMap = new HashMap<>();
        new MovementSystem();
    }

    @Override
    protected void frame() {
        systems.forEach(s -> s.update());
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        camera.setFocusPoint(levelAPI.getCurrentLevel().getStartTile().getCoordinate().toPoint());
    }

    public static void main(String[] args) {
        // start the game
        System.out.println("START");
        DesktopLauncher.run(new ECS());
    }
}
