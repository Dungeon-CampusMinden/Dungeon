package tools;

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
import java.util.logging.Logger;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import level.tools.LevelSize;
import logging.CustomLogLevel;
import starter.Game;

/**
 * The Debugger is an auxiliary system designed to accelerate the creation and testing of specific
 * game scenarios. While not strictly an ECS_System in the traditional sense, it provides useful
 * functionalities that can aid in verifying the correct behavior of a game implementation. The
 * Debugger is integrated into the GameLoop as an ECS_System.
 */
public class Debugger extends ECS_System {

    private static final Logger debugger_logger = Logger.getLogger(Debugger.class.getName());

    /**
     * Constructs a new Debugger instance, initially in an inactive state. To activate it, use the
     * togglePause method.
     */
    public Debugger() {
        super();
        toggleRun();
        debugger_logger.info("Create new Debugger");
    }

    /**
     * Checks for key input corresponding to Debugger functionalities, and executes the relevant
     * function if detected.
     */
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
            Debugger.TOGGLE_LEVEL_SIZE();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.get()))
            Debugger.SPAWN_MONSTER_ON_CURSOR();
    }

    /**
     * Zooms the camera in or out by the given amount.
     *
     * @param amount the length of the zoom change
     */
    public static void ZOOM_CAMERA(float amount) {
        debugger_logger.log(CustomLogLevel.DEBUG, "Change Camera Zoom " + amount);
        Game.camera.zoom = Math.max(0.1f, Game.camera.zoom + amount);
        debugger_logger.log(CustomLogLevel.DEBUG, "Camera Zoom is now " + Game.camera.zoom);
    }

    /** Teleports the Hero to the current position of the cursor. */
    public static void TELEPORT_TO_CURSOR() {
        debugger_logger.log(CustomLogLevel.DEBUG, "TELEPORT TO CURSOR");
        TELEPORT(SkillTools.getCursorPositionAsPoint());
    }

    /** Teleports the Hero to the end of the level, on a neighboring accessible tile if possible. */
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

    /** Teleports the hero to the start of the level. */
    public static void TELEPORT_TO_START() {
        debugger_logger.info("TELEPORT TO START");
        TELEPORT(Game.currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /**
     * Teleports the hero to the given location.
     *
     * @param targetLocation the location to teleport to
     */
    public static void TELEPORT(Point targetLocation) {
        if (Game.getHero().isPresent()) {
            debugger_logger.log(
                    CustomLogLevel.DEBUG,
                    "Try to teleport to " + targetLocation.x + ":" + targetLocation.y);

            PositionComponent pc =
                    (PositionComponent)
                            Game.getHero()
                                    .get()
                                    .getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "Hero is missing PositionComponent"));

            Tile t = null;
            try {
                t = Game.currentLevel.getTileAt(targetLocation.toCoordinate());
            } catch (NullPointerException ex) {
                debugger_logger.info(ex.getMessage());
            }
            // check if the point is in the level and accessible
            if (t != null && t.isAccessible()) {
                pc.setPosition(targetLocation);
                debugger_logger.info("teleport successful");
            } else debugger_logger.info("Can not teleport to non existing or non accessible tile");
        }
    }

    /**
     * Toggles the level size between small, medium, and large. Changes will affect the next level
     * load.
     */
    public static void TOGGLE_LEVEL_SIZE() {
        switch (Game.LEVELSIZE) {
            case SMALL -> Game.LEVELSIZE = LevelSize.MEDIUM;
            case MEDIUM -> Game.LEVELSIZE = LevelSize.LARGE;
            case LARGE -> Game.LEVELSIZE = LevelSize.SMALL;
        }
        debugger_logger.info("LevelSize toggled to: " + Game.LEVELSIZE);
    }

    /** Spawns a monster at the cursor's position. */
    public static void SPAWN_MONSTER_ON_CURSOR() {
        debugger_logger.info("Spawn Monster on Cursor");
        SPAWN_MONSTER(SkillTools.getCursorPositionAsPoint());
    }

    /**
     * Spawn a monster at the given position if it is in the level and accessible.
     *
     * @param position The location to spawn the monster on.
     */
    public static void SPAWN_MONSTER(Point position) {
        // Get the tile at the given position
        Tile tile = null;
        try {
            tile = Game.currentLevel.getTileAt(position.toCoordinate());
        } catch (NullPointerException ex) {
            debugger_logger.info(ex.getMessage());
        }

        // If the tile is accessible, spawn a monster at the position
        if (tile != null && tile.isAccessible()) {
            Entity monster = new Entity();

            // Add components to the monster entity
            monster.addComponent(new PositionComponent(monster, position));
            monster.addComponent(
                    new AnimationComponent(
                            monster,
                            AnimationBuilder.buildAnimation("character/monster/chort/idleLeft/"),
                            AnimationBuilder.buildAnimation("character/monster/chort/idleRight/")));
            monster.addComponent(
                    new VelocityComponent(
                            monster,
                            0.1f,
                            0.1f,
                            AnimationBuilder.buildAnimation("character/monster/chort/runLeft/"),
                            AnimationBuilder.buildAnimation("character/monster/chort/runRight/")));
            monster.addComponent(new HealthComponent(monster));
            monster.addComponent(new HitboxComponent(monster));
            monster.addComponent(
                    new AIComponent(
                            monster,
                            new CollideAI(1),
                            new RadiusWalk(5, 1),
                            new SelfDefendTransition()));

            // Log that the monster was spawned
            debugger_logger.info("Spawned monster at position " + position);
        } else {
            // Log that the monster couldn't be spawned
            debugger_logger.info("Cannot spawn monster at non-existent or non-accessible tile");
        }
    }
}
