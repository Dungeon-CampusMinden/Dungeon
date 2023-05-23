package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Point;

public class BossWalk implements IIdleAI{
    private GraphPath<Tile> path;

    @Override
    public void idle(Entity entity) {
        if(path == null || AITools.pathFinishedOrLeft(entity, path)){
            int random = (int) (Math.random() * 100);
            if(random > 97) {
                Point entityPoint = entityPosition(entity);
                Point newPosition = AITools.getRandomAccessibleTileCoordinateInRange(entityPoint, 10).toPoint();
                while (AITools.inRange(entityPoint, newPosition, 5)) {
                    newPosition = AITools.getRandomAccessibleTileCoordinateInRange(entityPoint, 10).toPoint();
                }
                path = AITools.calculatePath(entityPoint, newPosition);
            }
        }
        if(path != null) {
            AITools.move(entity, path);
        }
    }

    public Point entityPosition(Entity entity){
        return ((PositionComponent)
            entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () ->
                        new MissingComponentException(
                            "PositionComponent")))
            .getPosition();
    }
}
