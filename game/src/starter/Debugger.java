package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.skill.SkillTools;
import ecs.systems.ECS_System;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import level.tools.LevelSize;
import tools.Point;

/** Collection of functions for easy debugging */
public class Debugger extends ECS_System {

    @Override
    public void update() {
        // DEBUGGER
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) Debugger.ZOOM_CAMERA(-0.2f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) Debugger.ZOOM_CAMERA(0.2f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) Debugger.TELEPORT_TO_CURSOR();
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) Debugger.TELEPORT_TO_END();
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) Debugger.TELEPORT_TO_START();
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) Debugger.LOAD_NEXT_LEVEL();
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) Debugger.TOGGLE_LEVEL_SIZE(); //Z is Y on QWERTZ because libGDX
    }

    /**
     * Zoom in or out
     *
     * @param amount Zoom length
     */
    public static void ZOOM_CAMERA(float amount) {
        Game.camera.zoom = Math.max(0.1f, Game.camera.zoom + amount);
    }

    /** Teleport the Hero to the current cursor Position */
    public static void TELEPORT_TO_CURSOR() {
        TELEPORT(SkillTools.getCursorPositionAsPoint());
    }

    /** Teleport the Hero to the end of the level. */
    public static void TELEPORT_TO_END() {
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

    /**
     * Will teleport the Hero on the EndTile so the next level gets loaded
     */
    public static void LOAD_NEXT_LEVEL(){
        TELEPORT(Game.currentLevel.getEndTile().getCoordinate().toPoint());
    }

    /** Teleport the Hero to the start of the level */
    public static void TELEPORT_TO_START() {
        TELEPORT(Game.currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /**
     * Teleport the Hero to the given location
     *
     * @param targetLocation
     */
    public static void TELEPORT(Point targetLocation) {
        PositionComponent pc =
                (PositionComponent)
                        Game.hero
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "Hero is missing PositionComponent"));
        if (Game.currentLevel.getTileAt(targetLocation.toCoordinate()).isAccessible())
            pc.setPosition(targetLocation);
    }

    /**
     * Switch between Small, Medium and Large level.
     * Changes will affect on next level load
     */
    public static void TOGGLE_LEVEL_SIZE(){
        switch (Game.LEVELSIZE){
            case SMALL -> Game.LEVELSIZE= LevelSize.MEDIUM;
            case MEDIUM -> Game.LEVELSIZE=LevelSize.LARGE;
            case LARGE -> Game.LEVELSIZE=LevelSize.SMALL;
        }
    }
}
