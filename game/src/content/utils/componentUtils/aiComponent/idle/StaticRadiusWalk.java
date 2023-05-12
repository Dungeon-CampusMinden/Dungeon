package content.utils.componentUtils.aiComponent.idle;

import api.Entity;
import api.components.PositionComponent;
import api.level.Tile;
import api.utils.Constants;
import api.utils.Point;
import api.utils.componentUtils.aiComponent.IIdleAI;
import com.badlogic.gdx.ai.pfa.GraphPath;
import content.utils.componentUtils.aiComponent.AITools;

public class StaticRadiusWalk implements IIdleAI {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;
    private Point center;
    private Point currentPosition;
    private Point newEndTile;

    /**
     * Finds a point in the radius and then moves there. When the point has been reached, a new
     * point in the radius is searched for from the center.
     *
     * @param radius Radius in which a target point is to be searched for
     * @param breakTimeInSeconds how long to wait (in seconds) before searching a new goal
     */
    public StaticRadiusWalk(float radius, int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (center == null) {
                PositionComponent pc =
                        (PositionComponent)
                                entity.getComponent(PositionComponent.class).orElseThrow();
                center = pc.getPosition();
            }

            if (currentBreak >= breakTime) {
                currentBreak = 0;
                PositionComponent pc2 =
                        (PositionComponent)
                                entity.getComponent(PositionComponent.class).orElseThrow();
                currentPosition = pc2.getPosition();
                newEndTile =
                        AITools.getRandomAccessibleTileCoordinateInRange(center, radius).toPoint();
                path = AITools.calculatePath(currentPosition, newEndTile);
                idle(entity);
            }
            currentBreak++;

        } else AITools.move(entity, path);
    }
}
