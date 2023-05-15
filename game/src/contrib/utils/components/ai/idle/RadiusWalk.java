package contrib.utils.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AITools;
import contrib.utils.components.ai.IIdleAI;

import core.Entity;
import core.level.Tile;
import core.utils.Constants;

public class RadiusWalk implements IIdleAI {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;

    /**
     * Finds a point in the radius and then moves there. When the point has been reached, a new
     * point in the radius is searched for from there.
     *
     * @param radius Radius in which a target point is to be searched for
     * @param breakTimeInSeconds how long to wait (in seconds) before searching a new goal
     */
    public RadiusWalk(float radius, int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = AITools.calculatePathToRandomTileInRange(entity, radius);
                idle(entity);
            }

            currentBreak++;

        } else AITools.move(entity, path);
    }
}
