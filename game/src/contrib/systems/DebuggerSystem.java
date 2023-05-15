package contrib.systems;

import com.badlogic.gdx.Gdx;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.configuration.KeyboardConfig;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.skill.SkillTools;

import core.Entity;
import core.Game;
import core.System;
import core.components.*;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelSize;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.CustomLogLevel;

import dslToGame.AnimationBuilder;

import java.util.logging.Logger;

/**
 * The Debugger is an auxiliary system designed to accelerate the creation and testing of specific
 * game scenarios.
 *
 * <p>While not strictly an ECS_System in the traditional sense, it provides useful functionalities
 * that can aid in verifying the correct behavior of a game implementation. The Debugger is
 * integrated into the GameLoop as an ECS_System.
 *
 * <p>On default the debugger is deactivated and must first be activated, by pressing the
 * corresponding key.
 *
 * @see System
 * @see KeyboardConfig
 */
public class DebuggerSystem extends System {

    private static final Logger DEBUGGER_LOGGER = Logger.getLogger(DebuggerSystem.class.getName());

    /**
     * Constructs a new Debugger instance, initially in an inactive state. To activate it, use the
     * togglePause method.
     */
    public DebuggerSystem() {
        super();
        toggleRun();
        DEBUGGER_LOGGER.info("Create new Debugger");
    }

    /**
     * Checks for key input corresponding to Debugger functionalities, and executes the relevant
     * function if detected.
     */
    @Override
    public void update() {
        // DEBUGGER
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.get()))
            DebuggerSystem.ZOOM_CAMERA(-0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.get()))
            DebuggerSystem.ZOOM_CAMERA(0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.get()))
            DebuggerSystem.TELEPORT_TO_CURSOR();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.get()))
            DebuggerSystem.TELEPORT_TO_END();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.get()))
            DebuggerSystem.TELEPORT_TO_START();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.get()))
            DebuggerSystem.LOAD_NEXT_LEVEL();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_LEVELSIZE.get()))
            DebuggerSystem.TOGGLE_LEVEL_SIZE();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.get()))
            DebuggerSystem.SPAWN_MONSTER_ON_CURSOR();
    }

    /**
     * Zooms the camera in or out by the given amount.
     *
     * @param amount the length of the zoom change
     */
    public static void ZOOM_CAMERA(float amount) {
        DEBUGGER_LOGGER.log(CustomLogLevel.DEBUG, "Change Camera Zoom " + amount);
        Game.camera.zoom = Math.max(0.1f, Game.camera.zoom + amount);
        DEBUGGER_LOGGER.log(CustomLogLevel.DEBUG, "Camera Zoom is now " + Game.camera.zoom);
    }

    /** Teleports the Hero to the current position of the cursor. */
    public static void TELEPORT_TO_CURSOR() {
        DEBUGGER_LOGGER.log(CustomLogLevel.DEBUG, "TELEPORT TO CURSOR");
        TELEPORT(SkillTools.getCursorPositionAsPoint());
    }

    /** Teleports the Hero to the end of the level, on a neighboring accessible tile if possible. */
    public static void TELEPORT_TO_END() {
        DEBUGGER_LOGGER.info("TELEPORT TO END");
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
        DEBUGGER_LOGGER.info("TELEPORT ON END");
        TELEPORT(Game.currentLevel.getEndTile().getCoordinate().toPoint());
    }

    /** Teleports the hero to the start of the level. */
    public static void TELEPORT_TO_START() {
        DEBUGGER_LOGGER.info("TELEPORT TO START");
        TELEPORT(Game.currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /**
     * Teleports the hero to the given location.
     *
     * @param targetLocation the location to teleport to
     */
    public static void TELEPORT(Point targetLocation) {
        if (Game.getHero().isPresent()) {
            PositionComponent pc =
                    (PositionComponent)
                            Game.getHero()
                                    .get()
                                    .getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "Hero is missing PositionComponent"));

            // Attempt to teleport to targetLocation
            DEBUGGER_LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Trying to teleport to " + targetLocation.x + ":" + targetLocation.y);
            Tile t = Game.currentLevel.getTileAt(targetLocation.toCoordinate());
            if (t == null || !t.isAccessible()) {
                DEBUGGER_LOGGER.info("Cannot teleport to non-existing or non-accessible tile");
                return;
            }

            pc.setPosition(targetLocation);
            DEBUGGER_LOGGER.info("Teleport successful");
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
        DEBUGGER_LOGGER.info("LevelSize toggled to: " + Game.LEVELSIZE);
    }

    /** Spawns a monster at the cursor's position. */
    public static void SPAWN_MONSTER_ON_CURSOR() {
        DEBUGGER_LOGGER.info("Spawn Monster on Cursor");
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
            DEBUGGER_LOGGER.info(ex.getMessage());
        }

        // If the tile is accessible, spawn a monster at the position
        if (tile != null && tile.isAccessible()) {
            Entity monster = new Entity();

            // Add components to the monster entity
            monster.addComponent(new PositionComponent(monster, position));
            monster.addComponent(
                    new DrawComponent(
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
            monster.addComponent(new CollideComponent(monster));
            monster.addComponent(
                    new AIComponent(
                            monster,
                            new CollideAI(1),
                            new RadiusWalk(5, 1),
                            new SelfDefendTransition()));

            // Log that the monster was spawned
            DEBUGGER_LOGGER.info("Spawned monster at position " + position);
        } else {
            // Log that the monster couldn't be spawned
            DEBUGGER_LOGGER.info("Cannot spawn monster at non-existent or non-accessible tile");
        }
    }
}
