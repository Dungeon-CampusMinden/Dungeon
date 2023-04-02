package ecs.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import java.util.*;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import starter.Game;
import tools.Point;

public class AITools {
    private static final Random random = new Random();

    /**
     * Sets the velocity of the passed entity so that it takes the next necessary step to get to the
     * end of the path.
     *
     * @param entity Entity moving on the path
     * @param path Path on which the entity moves
     */
    public static void move(Entity entity, GraphPath<Tile> path) {
        // entity is already at the end
        if (pathFinishedOrLeft(entity, path)) {
            return;
        }
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        VelocityComponent vc =
                (VelocityComponent)
                        entity.getComponent(VelocityComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("VelocityComponent"));
        ILevel level = Game.currentLevel;
        Tile currentTile = level.getTileAt(pc.getPosition().toCoordinate());
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
            case N -> vc.setCurrentYVelocity(vc.getYVelocity());
            case S -> vc.setCurrentYVelocity(-vc.getYVelocity());
            case E -> vc.setCurrentXVelocity(vc.getXVelocity());
            case W -> vc.setCurrentXVelocity(-vc.getXVelocity());
        }
        if (currentTile.directionTo(nextTile).length > 1)
            switch (currentTile.directionTo(nextTile)[1]) {
                case N -> vc.setCurrentYVelocity(vc.getYVelocity());
                case S -> vc.setCurrentYVelocity(-vc.getYVelocity());
                case E -> vc.setCurrentXVelocity(vc.getXVelocity());
                case W -> vc.setCurrentXVelocity(-vc.getXVelocity());
            }
    }

    /**
     * @param center center point
     * @param radius Search radius
     * @return List of tiles in the given radius arround the center point
     */
    public static List<Tile> getTilesInRange(Point center, float radius) {
        List<Tile> tiles = new ArrayList<>();
        ILevel level = Game.currentLevel;
        for (float x = center.x - radius; x <= center.x + radius; x++) {
            for (float y = center.y - radius; y <= center.y + radius; y++) {
                tiles.add(level.getTileAt(new Point(x, y).toCoordinate()));
            }
        }
        tiles.removeIf(Objects::isNull);
        return tiles;
    }

    /**
     * @param center center point
     * @param radius Search radius
     * @return List of accessible tiles in the given radius arround the center point
     */
    public static List<Tile> getAccessibleTilesInRange(Point center, float radius) {
        List<Tile> tiles = getTilesInRange(center, radius);
        tiles.removeIf(tile -> !tile.isAccessible());
        return tiles;
    }

    /**
     * @param center center point
     * @param radius search radius
     * @return random tile in given range
     */
    public static Coordinate getRandomAccessibleTileCoordinateInRange(Point center, float radius) {
        List<Tile> tiles = getAccessibleTilesInRange(center, radius);
        Coordinate newPosition = tiles.get(random.nextInt(tiles.size())).getCoordinate();
        return newPosition;
    }

    /**
     * @param from start point
     * @param to end point
     * @return Path from the start point to the end point
     */
    public static GraphPath<Tile> calculatePath(Point from, Point to) {
        return calculatePath(from.toCoordinate(), to.toCoordinate());
    }

    /**
     * @param from start coordinate
     * @param to end coordinate
     * @return Path from the start coordinate to the end coordinate
     */
    public static GraphPath<Tile> calculatePath(Coordinate from, Coordinate to) {
        ILevel level = Game.currentLevel;
        return level.findPath(level.getTileAt(from), level.getTileAt(to));
    }

    /**
     * Finds the path to a random (accessible) tile in the given radius, starting from the given
     * center point
     *
     * @param point Center point
     * @param radius Search radius
     * @return Path from the center point to the randomly selected tile
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(Point point, float radius) {
        Coordinate newPosition = getRandomAccessibleTileCoordinateInRange(point, radius);
        return calculatePath(point.toCoordinate(), newPosition);
    }

    /**
     * Finds the path to a random (accessible) tile in the given radius, starting from the position
     * of the given entity.
     *
     * @param entity Entity whose position is the center point
     * @param radius Search radius
     * @return Path from the position of the entity to the randomly selected tile
     */
    public static GraphPath<Tile> calculatePathToRandomTileInRange(Entity entity, float radius) {
        Point point =
                ((PositionComponent)
                                entity.getComponent(PositionComponent.class)
                                        .orElseThrow(
                                                () ->
                                                        new MissingComponentException(
                                                                "PositionComponent")))
                        .getPosition();
        return calculatePathToRandomTileInRange(point, radius);
    }

    /**
     * Finds the path from the position of one entity to the position of another entity.
     *
     * @param from Entity whose position is the start point
     * @param to Entity whose position is the goal point
     * @return Path
     */
    public static GraphPath<Tile> calculatePath(Entity from, Entity to) {
        PositionComponent fromPositionComponent =
                (PositionComponent)
                        from.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        PositionComponent positionComponent =
                (PositionComponent)
                        to.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        return calculatePath(fromPositionComponent.getPosition(), positionComponent.getPosition());
    }

    /**
     * @param entity
     * @return Path from the entity to the hero, if there is no hero, path from the entity to itself
     */
    public static GraphPath<Tile> calculatePathToHero(Entity entity) {
        Optional<Entity> hero = Game.getHero();
        if (hero.isPresent()) return calculatePath(entity, hero.get());
        else return calculatePath(entity, entity);
    }

    /**
     * @param p1 Point A
     * @param p2 Point B
     * @param range Radius
     * @return if the distance between the two points is within the radius
     */
    public static boolean inRange(Point p1, Point p2, float range) {
        return Point.calculateDistance(p1, p2) <= range;
    }

    /**
     * @param entity1
     * @param entity2
     * @param range search radius
     * @return if the position of the two entities is within the given radius
     */
    public static boolean entityInRange(Entity entity1, Entity entity2, float range) {

        Point entity1Position =
                ((PositionComponent)
                                entity1.getComponent(PositionComponent.class)
                                        .orElseThrow(
                                                () ->
                                                        new MissingComponentException(
                                                                "PositionComponent")))
                        .getPosition();
        Point entity2Position =
                ((PositionComponent)
                                entity2.getComponent(PositionComponent.class)
                                        .orElseThrow(
                                                () ->
                                                        new MissingComponentException(
                                                                "PositionComponent")))
                        .getPosition();
        return inRange(entity1Position, entity2Position, range);
    }

    /**
     * @param entity Entity whose position specifies the center point
     * @param range search radius
     * @return if the position of the player is within the given radius of the position of the given
     *     entity. If there is no hero, return false.
     */
    public static boolean playerInRange(Entity entity, float range) {

        Optional<Entity> hero = Game.getHero();
        if (hero.isPresent()) return entityInRange(entity, hero.get(), range);
        else return false;
    }

    /**
     * Check if the entity is on the end of the path or has left the path.
     *
     * @param entity Entity
     * @param path Path
     * @return true, if the entity is on the end of the path or has left the path
     */
    public static boolean pathFinishedOrLeft(Entity entity, GraphPath<Tile> path) {
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        ILevel level = Game.currentLevel;
        boolean finished =
                path.get(path.getCount() - 1)
                        .equals(level.getTileAt(pc.getPosition().toCoordinate()));

        boolean onPath = false;
        Tile currentTile = level.getTileAt(pc.getPosition().toCoordinate());
        for (Tile tile : path) {
            if (currentTile == tile) onPath = true;
        }

        return !onPath || finished;
    }

    /**
     * Check if the entity is on the end of the path
     *
     * @param entity Entity
     * @param path Path
     * @return true, if the entity is on the end of the path.
     */
    public static boolean pathFinished(Entity entity, GraphPath<Tile> path) {
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        ILevel level = Game.currentLevel;
        return path.get(path.getCount() - 1)
                .equals(level.getTileAt(pc.getPosition().toCoordinate()));
    }

    /**
     * Check if the entity has left the path
     *
     * @param entity Entity
     * @param path Path
     * @return true, if the entity has left the path.
     */
    public static boolean pathLeft(Entity entity, GraphPath<Tile> path) {
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        ILevel level = Game.currentLevel;
        boolean onPath = false;
        Tile currentTile = level.getTileAt(pc.getPosition().toCoordinate());
        for (Tile tile : path) {
            if (currentTile == tile) onPath = true;
        }
        return !onPath;
    }
}
