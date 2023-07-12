package core.level.utils;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AITools;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.Optional;

public class LevelUtils {
    /**
     * Finds the path from the given point to another given point.
     *
     * <p>Throws an IllegalArgumentException if the tile at 'from' or 'to' is non-accessible.
     *
     * @param from The start point.
     * @param to The end point.
     * @return Path from the start point to the end point.
     */
    public static GraphPath<Tile> calculatePath(final Point from, final Point to) {
        return calculatePath(from.toCoordinate(), to.toCoordinate());
    }

    /**
     * Finds the path from the given coordinate to another given coordinate.
     *
     * <p>Throws an IllegalArgumentException if the tile at the start or end is non-accessible.
     *
     * @param from The start coordinate.
     * @param to The end coordinate.
     * @return Path from the start coordinate to the end coordinate.
     */
    public static GraphPath<Tile> calculatePath(final Coordinate from, final Coordinate to) {
        return Game.findPath(Game.tileAT(from), Game.tileAT(to));
    }

    /**
     * Finds the path to a random (accessible) tile in the given radius, starting from the given
     * point.
     *
     * <p>If there is no accessible tile in the range, the path will be calculated from the given
     * start point to the given start point.
     *
     * <p>Throws an IllegalArgumentException if the tile at the start point is non-accessible.
     *
     * @param point The start point.
     * @param radius Radius in which the tiles are to be considered.
     * @return Path from the center point to the randomly selected tile.
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(
            final Point point, final float radius) {
        Coordinate newPosition =
                AITools.randomAccessibleTileCoordinateInRange(point, radius)
                        .orElse(point.toCoordinate());
        return calculatePath(point.toCoordinate(), newPosition);
    }

    /**
     * Finds the path to a random (accessible) tile in the given radius, starting from the position
     * of the given entity.
     *
     * <p>If there is no accessible tile in the range, the path will be calculated from the given
     * start point to the given start point.
     *
     * <p>Throws an IllegalArgumentException if the entities position is on a non-accessible tile.
     *
     * @param entity Entity whose position is the center point.
     * @param radius Radius in which the tiles are to be considered.
     * @return Path from the position of the entity to the randomly selected tile.
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(
            final Entity entity, final float radius) {
        Point point =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class))
                        .position();
        return calculatePathToRandomTileInRange(point, radius);
    }

    /**
     * Finds the path from the position of one entity to the position of another entity.
     *
     * <p>Throws an IllegalArgumentException if one of the entities position is non-accessible.
     *
     * @param from Entity whose position is the start point.
     * @param to Entity whose position is the goal point.
     * @return Path from one entity to the other entity.
     */
    public static GraphPath<Tile> calculatePath(final Entity from, final Entity to) {
        PositionComponent fromPositionComponent =
                from.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                from, PositionComponent.class));
        PositionComponent positionComponent =
                to.fetch(PositionComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(to, PositionComponent.class));
        return calculatePath(fromPositionComponent.position(), positionComponent.position());
    }

    /**
     * Finds the path from the position of one entity to the position of the hero.
     *
     * <p>If no hero exist in the game, the path will be calculated from the given entity to the
     * given entity.
     *
     * <p>Throws an IllegalArgumentException if one of the entities position is non-accessible.
     *
     * @param entity Entity from which the path to the hero is calculated.
     * @return Path from the entity to the hero, if there is no hero, path from the entity to
     *     itself.
     */
    public static GraphPath<Tile> calculatePathToHero(final Entity entity) {
        Optional<Entity> hero = Game.hero();
        if (hero.isPresent()) return calculatePath(entity, hero.get());
        else return calculatePath(entity, entity);
    }

    /**
     * Get the last Tile in the given GraphPath.
     *
     * @param path Considered GraphPath.
     * @return Last Tile in the given path.
     * @see GraphPath
     */
    public static Tile lastTile(final GraphPath<Tile> path) {
        return path.get(path.getCount() - 1);
    }
}
