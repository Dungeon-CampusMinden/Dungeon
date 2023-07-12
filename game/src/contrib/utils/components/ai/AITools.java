package contrib.utils.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.*;

public class AITools {
    private static final Random random = new Random();

    /**
     * Sets the velocity of the passed entity so that it takes the next necessary step to get to the
     * end of the path.
     *
     * @param entity Entity moving on the path.
     * @param path Path on which the entity moves.
     */
    public static void move(final Entity entity, final GraphPath<Tile> path) {
        // entity is already at the end
        if (pathFinishedOrLeft(entity, path)) {
            return;
        }
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        VelocityComponent vc =
                entity.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, VelocityComponent.class));
        Tile currentTile = Game.tileAT(pc.position());
        int i = 0;
        Tile nextTile = null;
        while (nextTile == null && i < path.getCount()) {
            if (path.get(i).equals(currentTile)) {
                nextTile = path.get(i + 1);
            }
            i++;
        }
        // currentTile not in path
        if (nextTile == null) {
            return;
        }

        switch (currentTile.directionTo(nextTile)[0]) {
            case N -> vc.currentYVelocity(vc.yVelocity());
            case S -> vc.currentYVelocity(-vc.yVelocity());
            case E -> vc.currentXVelocity(vc.xVelocity());
            case W -> vc.currentXVelocity(-vc.xVelocity());
        }
        if (currentTile.directionTo(nextTile).length > 1)
            switch (currentTile.directionTo(nextTile)[1]) {
                case N -> vc.currentYVelocity(vc.yVelocity());
                case S -> vc.currentYVelocity(-vc.yVelocity());
                case E -> vc.currentXVelocity(vc.xVelocity());
                case W -> vc.currentXVelocity(-vc.xVelocity());
            }
    }

    /**
     * Get all tiles within a specified range around a given center point.
     *
     * <p>The range is determined by the provided radius.
     *
     * <p>The tile at the given point will be part of the list as well.
     *
     * @param center The center point around which the tiles are considered.
     * @param radius The radius within which the tiles should be located.
     * @return List of tiles in the given radius around the center point.
     */
    public static List<Tile> tilesInRange(final Point center, final float radius) {
        List<Tile> tiles = new ArrayList<>();
        for (float x = center.x - radius; x <= center.x + radius; x++) {
            for (float y = center.y - radius; y <= center.y + radius; y++) {
                tiles.add(Game.tileAT(new Point(x, y)));
            }
        }
        tiles.removeIf(Objects::isNull);
        return tiles;
    }

    /**
     * Get all accessible tiles within a specified range around a given center point.
     *
     * <p>The range is determined by the provided radius.
     *
     * <p>The tile at the given point will be part of the list as well, if it is accessible.
     *
     * @param center The center point around which the tiles are considered.
     * @param radius The radius within which the accessible tiles should be located.
     * @return List of accessible tiles in the given radius around the center point.
     */
    public static List<Tile> accessibleTilesInRange(final Point center, final float radius) {
        List<Tile> tiles = tilesInRange(center, radius);
        tiles.removeIf(tile -> !tile.isAccessible());
        return tiles;
    }

    /**
     * Get a random accessible tile coordinate within a specified range around a given center point.
     *
     * <p>The range is determined by the provided radius.
     *
     * <p>The tile at the given point can be the return value as well, if it is accessible.
     *
     * @param center The center point around which the tiles are considered.
     * @param radius The radius within which the accessible tiles should be located.
     * @return An Optional containing a random Coordinate object representing an accessible tile
     *     within the range, or an empty Optional if no accessible tiles were found.
     */
    public static Optional<Coordinate> randomAccessibleTileCoordinateInRange(
            final Point center, final float radius) {
        List<Tile> tiles = accessibleTilesInRange(center, radius);
        if (tiles.isEmpty()) return Optional.empty();
        Coordinate newPosition = tiles.get(random.nextInt(tiles.size())).coordinate();
        return Optional.of(newPosition);
    }

    /**
     * Check if two entities are positioned in a specified range from each other.
     *
     * @param entity1 The first entity which is considered.
     * @param entity2 The second entity which is to be searched for in the given range.
     * @param range The range in which the two entities are positioned from each other.
     * @return True if the position of the two entities is within the given range, else false
     */
    public static boolean entityInRange(
            final Entity entity1, final Entity entity2, final float range) {

        Point entity1Position =
                entity1.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity1, PositionComponent.class))
                        .position();
        Point entity2Position =
                entity2.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity2, PositionComponent.class))
                        .position();
        return Point.inRange(entity1Position, entity2Position, range);
    }

    /**
     * Check if the player is in the given range of an entity.
     *
     * @param entity Entity whose position specifies the center point.
     * @param range The range within which the player should be located.
     * @return True if the position of the player is within the given radius of the position of the
     *     given entity. If there is no hero, return false.
     */
    public static boolean playerInRange(final Entity entity, final float range) {

        Optional<Entity> hero = Game.hero();
        return hero.filter(value -> entityInRange(entity, value, range)).isPresent();
    }

    /**
     * Check if the entity is on the end of the path or has left the path.
     *
     * @param entity Entity to be checked.
     * @param path Path which the entity possibly left or has reached the end.
     * @return True if the entity is on the end of the path or has left the path, otherwise false.
     */
    public static boolean pathFinishedOrLeft(final Entity entity, final GraphPath<Tile> path) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        boolean finished = LevelUtils.lastTile(path).equals(Game.tileAT(pc.position()));

        boolean onPath = false;
        Tile currentTile = Game.tileAT(pc.position());
        for (Tile tile : path) {
            if (currentTile == tile) {
                onPath = true;
            }
        }

        return !onPath || finished;
    }

    /**
     * Check if the entity is on the end of the path.
     *
     * @param entity Entity to be checked.
     * @param path Path on which the entity possible reached the end.
     * @return True if the entity is on the end of the path, otherwise false.
     */
    public static boolean pathFinished(final Entity entity, final GraphPath<Tile> path) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        return LevelUtils.lastTile(path).equals(Game.tileAT(pc.position()));
    }

    /**
     * Check if the entity has left the path.
     *
     * @param entity Entity to be checked.
     * @param path Path to be checked.
     * @return True if the entity has left the path, otherwise false.
     */
    public static boolean pathLeft(final Entity entity, final GraphPath<Tile> path) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        boolean onPath = false;
        Tile currentTile = Game.tileAT(pc.position());
        for (Tile tile : path) {
            if (currentTile == tile) {
                onPath = true;
            }
        }
        return !onPath;
    }
}
