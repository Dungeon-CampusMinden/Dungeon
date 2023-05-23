package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Point;

public class BossWalk implements IIdleAI {
    private GraphPath<Tile> path;
    private static final float MAX_RADIUS = 10f;
    private static final float MIN_RADIUS = 5f;

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            // is not on path
            int random = (int) (Math.random() * 100);
            if (random > 97) {// 3% chance
                Point entityPoint = entityPosition(entity);
                Point newPosition = entityPoint;
                while (AITools.inRange(entityPoint, newPosition, MIN_RADIUS)) {
                    // must be at least 5 tiles distance
                    newPosition = AITools.getRandomAccessibleTileCoordinateInRange(entityPoint, MAX_RADIUS).toPoint();
                    // looks for a point within 10 tiles radius
                }
                path = AITools.calculatePath(entityPoint, newPosition);
            }
            return;
        }
        // is on path
        AITools.move(entity, path);
    }

    public Point entityPosition(Entity entity) {
        return ((PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException(
                                "PositionComponent")))
                .getPosition();
    }
}
