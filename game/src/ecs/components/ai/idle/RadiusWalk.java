package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.ILevel;
import level.elements.tile.Tile;
import mydungeon.ECS;
import tools.Constants;

public class RadiusWalk implements IIdleAI {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime = Constants.FRAME_RATE * 5;
    private int currentBreak = 0;

    /**
     * Finds a point in the radius and then moves there. When the point has been reached, a new
     * point in the radius is searched for from there.
     *
     * @param radius Radius in which a target point is to be searched for
     */
    public RadiusWalk(float radius) {
        this.radius = radius;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || pathFinished(entity)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = AITools.calculatePathToRandomTileInRange(entity, radius);
                idle(entity);
            }

            currentBreak++;

        } else AITools.move(entity, path);
    }

    private boolean pathFinished(Entity entity) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        ILevel level = ECS.currentLevel;
        return path.get(path.getCount() - 1)
                .equals(level.getTileAt(pc.getPosition().toCoordinate()));
    }
}
