package contrib.utils.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AIUtils;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.function.Consumer;

/** Implements an idle AI that lets the entity walk in a specific radius from a point. */
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
        this.breakTime = breakTimeInSeconds * Game.frameRate();
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

                if (pc.position().equals(PositionComponent.ILLEGAL_POSITION)) return;
                else center = pc.position();
            }

            if (currentBreak >= breakTime) {
                currentBreak = 0;
                PositionComponent pc2 =
                        entity.fetch(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                MissingComponentException.build(
                                                        entity, PositionComponent.class));
                if (pc2.position().equals(PositionComponent.ILLEGAL_POSITION)) return;
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
}
