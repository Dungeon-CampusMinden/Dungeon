package contrib.utils.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AIUtils;

import core.Dungeon;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.function.Consumer;

public class StaticRadiusWalk implements Consumer<Entity> {
    private final float radius;
    private final int breakTime;
    private GraphPath<Tile> path;
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
    public StaticRadiusWalk(final float radius, final int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Dungeon.frameRate();
    }

    @Override
    public void accept(final Entity entity) {
        if (path == null || AIUtils.pathFinishedOrLeft(entity, path)) {
            if (center == null) {
                PositionComponent pc =
                        entity.fetch(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                MissingComponentException.build(
                                                        entity, PositionComponent.class));
                center = pc.position();
            }

            if (currentBreak >= breakTime) {
                currentBreak = 0;
                PositionComponent pc2 =
                        entity.fetch(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                MissingComponentException.build(
                                                        entity, PositionComponent.class));
                currentPosition = pc2.position();
                newEndTile =
                        LevelUtils.randomAccessibleTileCoordinateInRange(center, radius)
                                .map(Coordinate::toPoint)
                                // center is the start position of the entity, so it must be
                                // accessible
                                .orElse(center);
                path = LevelUtils.calculatePath(currentPosition, newEndTile);
                accept(entity);
            }
            currentBreak++;

        } else AIUtils.move(entity, path);
    }

    /**
     * @return  radius.
     */
    public float radius() {
        return radius;
    }

    /**
     * @return break time.
     */
    public int breakTime() {
        return breakTime;
    }
}
