package contrib.utils.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
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
     * Get all tile coordinates within a specified range around a given center point. The range is
     * determined by the provided radius.
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
     * Get all accessible tile coordinates within a specified range around a given center point. The
     * range is determined by the provided radius.
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
     * The range is determined by the provided radius.
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
     * Finds the path from the given point to another given point.
     *
     * <p>Throws an IllegalArgumentException if 'from' or 'to' is non-accessible.
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
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
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
     * center point
     *
     * <p>If there is no accessible tile in the range, the path will be calculated from the given
     * center point to the given center point. This is known misbehavior, see <a
     * href="https://github.com/Programmiermethoden/Dungeon/issues/786">...</a>
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
     *
     * @param point The center point.
     * @param radius Radius in which the tiles are to be considered.
     * @return Path from the center point to the randomly selected tile.
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(
            final Point point, final float radius) {
        Coordinate newPosition =
                randomAccessibleTileCoordinateInRange(point, radius).orElse(point.toCoordinate());
        return calculatePath(point.toCoordinate(), newPosition);
    }

    /**
     * Finds the path to a random (accessible) tile in the given radius, starting from the position
     * of the given entity.
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
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
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
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
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
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
     * Check if two points are positioned in a specified range from each other.
     *
     * @param p1 The first point which is considered.
     * @param p2 The second point which is considered.
     * @param range The range in which the two points are positioned from each other.
     * @return True if the distance between the two points is within the radius, else false.
     */
    public static boolean inRange(final Point p1, final Point p2, final float range) {
        return Point.calculateDistance(p1, p2) <= range;
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
        return inRange(entity1Position, entity2Position, range);
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
        boolean finished = lastTile(path).equals(Game.tileAT(pc.position()));

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
        return lastTile(path).equals(Game.tileAT(pc.position()));
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
