package contrib.utils.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AIUtils;

import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;

import java.util.function.Consumer;

public class RadiusWalk implements Consumer<Entity> {
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
    public RadiusWalk(final float radius, final int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Game.frameRate();
    }

    @Override
    public void accept(final Entity entity) {
        if (path == null || AIUtils.pathFinishedOrLeft(entity, path)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = LevelUtils.calculatePathToRandomTileInRange(entity, radius);
                accept(entity);
            }

            currentBreak++;

        } else AIUtils.move(entity, path);
    }
}
