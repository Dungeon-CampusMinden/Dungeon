package starter;

import com.badlogic.gdx.Gdx;
import configuration.KeyboardConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.SelfDefendTransition;
import ecs.components.skill.SkillTools;
import ecs.entities.Entity;
import ecs.systems.ECS_System;
import graphic.Animation;
import java.util.logging.Logger;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import level.tools.LevelSize;
import logging.CustomLogLevel;
import tools.Point;

/** Collection of functions for easy debugging */
public class Debugger extends ECS_System {

    private static final Logger debugger_logger = Logger.getLogger(Debugger.class.getName());

    public Debugger() {
        super();
        toggleRun();
        debugger_logger.info("Create new Debugger");
    }

    @Override
    public void update() {
        // DEBUGGER
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.get()))
            Debugger.ZOOM_CAMERA(-0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.get()))
            Debugger.ZOOM_CAMERA(0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.get()))
            Debugger.TELEPORT_TO_CURSOR();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.get()))
            Debugger.TELEPORT_TO_END();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.get()))
            Debugger.TELEPORT_TO_START();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.get()))
            Debugger.LOAD_NEXT_LEVEL();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_LEVELSIZE.get()))
            Debugger.TOGGLE_LEVEL_SIZE(); // Z is Y on QWERTZ because libGDX
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.get()))
            Debugger.SPAWN_MONSTER_ON_CURSOR();
    }

    /**
     * Zoom in or out
     *
     * @param amount Zoom length
     */
    public static void ZOOM_CAMERA(float amount) {
        debugger_logger.log(CustomLogLevel.DEBUG, "Change Camera Zoom " + amount);
        Game.camera.zoom = Math.max(0.1f, Game.camera.zoom + amount);
        debugger_logger.log(CustomLogLevel.DEBUG, "Camera Zoom is now " + Game.camera.zoom);
    }

    /** Teleport the Hero to the current cursor Position */
    public static void TELEPORT_TO_CURSOR() {
        debugger_logger.log(CustomLogLevel.DEBUG, "TELEPORT TO CURSOR");
        try {
            TELEPORT(SkillTools.getCursorPositionAsPoint());
        } catch (NullPointerException exception) {
            debugger_logger.info(exception.getMessage());
        }
    }

    /** Teleport the Hero to the end of the level. */
    public static void TELEPORT_TO_END() {
        debugger_logger.info("TELEPORT TO END");
        Coordinate endTile = Game.currentLevel.getEndTile().getCoordinate();
        Coordinate[] neighborTiles = {
            new Coordinate(endTile.x + 1, endTile.y),
            new Coordinate(endTile.x - 1, endTile.y),
            new Coordinate(endTile.x, endTile.y + 1),
            new Coordinate(endTile.x, endTile.y - 1)
        };
        for (Coordinate neighborTile : neighborTiles) {
            Tile neighbor = Game.currentLevel.getTileAt(neighborTile);
            if (neighbor.isAccessible()) {
                TELEPORT(neighborTile.toPoint());
                return;
            }
        }
    }

    /** Will teleport the Hero on the EndTile so the next level gets loaded */
    public static void LOAD_NEXT_LEVEL() {
        debugger_logger.info("TELEPORT ON END");
        TELEPORT(Game.currentLevel.getEndTile().getCoordinate().toPoint());
    }

    /** Teleport the Hero to the start of the level */
    public static void TELEPORT_TO_START() {
        debugger_logger.info("TELEPORT TO START");
        TELEPORT(Game.currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /**
     * Teleport the Hero to the given location
     *
     * @param targetLocation locations to telport to
     */
    public static void TELEPORT(Point targetLocation) {
        debugger_logger.log(
                CustomLogLevel.DEBUG,
                "Try to teleport to " + targetLocation.x + ":" + targetLocation.y);
        PositionComponent pc =
                (PositionComponent)
                        Game.hero
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "Hero is missing PositionComponent"));
        if (Game.currentLevel.getTileAt(targetLocation.toCoordinate()).isAccessible()) {
            pc.setPosition(targetLocation);
            debugger_logger.info("teleport successful");
        } else debugger_logger.info("Can not teleport to unaccessbile tile");
    }

    /** Switch between Small, Medium and Large level. Changes will affect on next level load */
    public static void TOGGLE_LEVEL_SIZE() {
        switch (Game.LEVELSIZE) {
            case SMALL -> Game.LEVELSIZE = LevelSize.MEDIUM;
            case MEDIUM -> Game.LEVELSIZE = LevelSize.LARGE;
            case LARGE -> Game.LEVELSIZE = LevelSize.SMALL;
        }
        debugger_logger.info("Levelsize toggled to: " + Game.LEVELSIZE);
    }

    /** Spawn a Monster on the Cursor-Position */
    public static void SPAWN_MONSTER_ON_CURSOR() {
        debugger_logger.info("Spawn Monster on Cursor");
        try {
            SPAWN_MONSTER(SkillTools.getCursorPositionAsPoint());
        } catch (NullPointerException exception) {
            debugger_logger.info(exception.getMessage());
        }
    }

    /**
     * Spawn a monster at the given Position
     *
     * @param position location to spawn monster on
     */
    public static void SPAWN_MONSTER(Point position) {

        // check if the point is in the level and accessible
        if (Game.currentLevel.getTileAt(position.toCoordinate()).isAccessible()) {

            Entity monster = new Entity();
            monster.addComponent(new PositionComponent(monster, position));
            Animation idleLeft =
                    AnimationBuilder.buildAnimation("character/monster/chort/idleLeft/");
            Animation idleRight =
                    AnimationBuilder.buildAnimation("character/monster/chort/idleRight/");
            new AnimationComponent(monster, idleLeft, idleRight);
            Animation runRight =
                    AnimationBuilder.buildAnimation("character/monster/chort/runRight/");
            Animation runLeft = AnimationBuilder.buildAnimation("character/monster/chort/runLeft/");
            new VelocityComponent(monster, 0.1f, 0.1f, runLeft, runRight);
            new HealthComponent(monster);
            new HitboxComponent(monster);
            new AIComponent(
                    monster, new CollideAI(1), new RadiusWalk(5, 1), new SelfDefendTransition());
            debugger_logger.info("Monster spawned");
        } else debugger_logger.info("Cant spawn Monster on unaccessbile tile");
    }
}
