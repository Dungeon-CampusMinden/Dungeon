package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import static ecs.components.ai.AITools.getRandomAccessibleTileCoordinateInRange;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import level.elements.tile.Tile;
import starter.Game;
import tools.Constants;
import tools.Point;
import ecs.components.PositionComponent;
import starter.Game;

public class WalkToEndTile implements IIdleAI {
    private final int PAUSEFRAMES = 180;
    private final float RADIUS = 4;
    private int frameCounter = -1;
    private boolean atEndTile;
    private boolean initialized = false;
    private GraphPath<Tile> currentPath;

    public WalkToEndTile(float radius) {
        currentPath = null;
        radius = RADIUS;
    }

    private boolean isOnEndTile(Entity entity) {
        // Get the current position of the entity.
        // And the position of the end tile, to compare them.
        Point currentPosition = getCurrentPositionOf(entity);
        Point endTilePoint = Game.currentLevel.getEndTile().getCoordinateAsPoint();

        // If the entity is already at the end tile, set the flag.
        // Otherwise calculate a new path to the end tile.
        return currentPosition.equals(endTilePoint) ? true : false;
    }

    /**
     * Idle behaviour of the entity.
     * @param associatedEntity
     */
    @Override
    public void idle(Entity entity) {
        // Pause for a few frames before calculating a new path.
        if (frameCounter < PAUSEFRAMES) {
            frameCounter++;
            return;
        }
        if (currentPath == null)
        currentPath = atEndTile ? getPathAroundTile(entity) : getPathToEndTile(entity);

        if (currentPath != null) {
            AITools.move(entity, currentPath);
        }

        if (AITools.pathFinished(entity, currentPath)) {
            currentPath = null;
            atEndTile = !atEndTile;
            frameCounter = 0;
        }
    }
 
    /**
     * Walk to the end tile.
     * @param entity
     */
    private GraphPath<Tile> getPathToEndTile(Entity entity) {
        // Get the current position of the entity.
        // And the position of the end tile, to compare them.
        Point currentPosition = getCurrentPositionOf(entity);
        Point endTilePoint = Game.currentLevel.getEndTile().getCoordinateAsPoint();
        GraphPath<Tile> newPath = null;
        // If the entity is already at the end tile, set the flag.
        // Otherwise calculate a new path to the end tile.
        if (currentPosition.equals(endTilePoint)) {
            atEndTile = true;
        } else {
            newPath = AITools.calculatePath(currentPosition, endTilePoint);
        }

        return newPath;
    }

    private GraphPath<Tile> getPathAroundTile(Entity entity) {
        GraphPath<Tile> newPath = null;
        
        Point currentPosition = getCurrentPositionOf(entity);
        Point newDestinationTile = getRandomAccessibleTileCoordinateInRange(currentPosition, RADIUS).toPoint();
        newPath = AITools.calculatePath(currentPosition, newDestinationTile);
        return newPath;
    }

    private Point getCurrentPositionOf(Entity entity) {
        PositionComponent positionComponent =
                (PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(() -> new MissingComponentException("PositionComponent"));
        Point currentPosition = positionComponent.getPosition();
        return currentPosition;
    }

}
