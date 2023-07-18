package core.level.utils;

import com.badlogic.gdx.ai.pfa.GraphPath;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.position.Position;

import java.util.*;

public class LevelUtils {

    private static final Random random = new Random();

    /**
     * Finds the path from the given point to another given point.
     *
     * <p>Throws an IllegalArgumentException if the tile at 'from' or 'to' is non-accessible.
     *
     * @param from The start point.
     * @param to The end point.
     * @return Path from the start point to the end point.
     */
    public static GraphPath<Tile> calculatePath(final Position from, final Position to) {
        return calculatePath(from.coordinate().get(), to.coordinate().get());
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
     * position.
     *
     * <p>If there is no accessible tile in the range, the path will be calculated from the given
     * start position to the given start position.
     *
     * <p>Throws an IllegalArgumentException if the tile at the start position is non-accessible.
     *
     * @param position The start position.
     * @param radius Radius in which the tiles are to be considered.
     * @return Path from the center position to the randomly selected tile.
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(
            final Position position, final float radius) {
        Coordinate newPosition =
                randomAccessibleTileCoordinateInRange(position, radius)
                        .orElse(position.coordinate().get());
        return calculatePath(position.coordinate().get(), newPosition);
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
        Position position =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class))
                        .position();
        return calculatePathToRandomTileInRange(position, radius);
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

        Position entity1Position =
                entity1.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity1, PositionComponent.class))
                        .position();
        Position entity2Position =
                entity2.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity2, PositionComponent.class))
                        .position();
        return Position.inRange(entity1Position, entity2Position, range);
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
}
