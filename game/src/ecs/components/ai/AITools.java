package ecs.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import mydungeon.ECS;
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
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.name);
        ILevel level = ECS.currentLevel;
        Tile currentTile = level.getTileAt(pc.getPosition().toCoordinate());
        int i = 0;
        Tile nextTile = null;
        do {
            if (i >= path.getCount()) return;
            if (path.get(i).equals(currentTile)) {
                nextTile = path.get(i + 1);
            }
            i++;
        } while (nextTile == null);

        switch (currentTile.directionTo(nextTile)[0]) {
            case N -> vc.setY(vc.getySpeed());
            case S -> vc.setY(-vc.getySpeed());
            case E -> vc.setX(vc.getxSpeed());
            case W -> vc.setX(-vc.getxSpeed());
        }
        if (currentTile.directionTo(nextTile).length > 1)
            switch (currentTile.directionTo(nextTile)[1]) {
                case N -> vc.setY(vc.getySpeed());
                case S -> vc.setY(-vc.getySpeed());
                case E -> vc.setX(vc.getxSpeed());
                case W -> vc.setX(-vc.getxSpeed());
            }
    }

    /**
     * @param center center point
     * @param radius Search radius
     * @return List of tiles in the given radius arround the center point
     */
    public static List<Tile> getTilesInRange(Point center, float radius) {
        List<Tile> tiles = new ArrayList<>();
        ILevel level = ECS.currentLevel;
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
        ILevel level = ECS.currentLevel;
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
        List<Tile> tiles = getAccessibleTilesInRange(point, radius);
        Coordinate newPosition = tiles.get(random.nextInt(tiles.size())).getCoordinate();
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
        if (entity.getComponent(PositionComponent.name) != null) {
            Point point =
                    ((PositionComponent) entity.getComponent(PositionComponent.name)).getPosition();
            return calculatePathToRandomTileInRange(point, radius);
        }
        return null;
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
                (PositionComponent) from.getComponent(PositionComponent.name);
        PositionComponent toPositionomponent =
                (PositionComponent) to.getComponent(PositionComponent.name);
        if (fromPositionComponent != null && toPositionomponent != null) {
            return calculatePath(
                    fromPositionComponent.getPosition(), toPositionomponent.getPosition());
        }
        return null;
    }

    /**
     * @param entity
     * @return Path from the entity to the hero
     */
    public static GraphPath<Tile> calculatePathToHero(Entity entity) {
        return calculatePath(entity, ECS.hero);
    }

    /**
     * @param p1 Point A
     * @param p2 Point B
     * @param range Radius
     * @return if the distance between the two points is within the radius
     */
    public static boolean inRange(Point p1, Point p2, float range) {
        float xDiff = p1.x - p2.x;
        float yDiff = p1.y - p2.y;
        float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        return distance <= range;
    }

    /**
     * @param entity1
     * @param entity2
     * @param range search radius
     * @return if the position of the two entities is within the given radius
     */
    public static boolean entityInRange(Entity entity1, Entity entity2, float range) {
        if (entity1.getComponent(PositionComponent.name) != null
                && entity2.getComponent(PositionComponent.name) != null) {
            Point entity1Positon =
                    ((PositionComponent) entity1.getComponent(PositionComponent.name))
                            .getPosition();
            Point entity2Positon =
                    ((PositionComponent) entity2.getComponent(PositionComponent.name))
                            .getPosition();
            return inRange(entity1Positon, entity2Positon, range);
        }
        return false;
    }

    /**
     * @param entity Entity whose position specifies the center point
     * @param range search radius
     * @return if the position of the player is within the given radius of the position of the given
     *     entity.
     */
    public static boolean playerInRange(Entity entity, float range) {
        return entityInRange(entity, ECS.hero, range);
    }

    /**
     * Check if the entity is on the end of the path
     *
     * @param entity Entity
     * @param path Path
     * @return if the entity is on the end of the path
     */
    public static boolean pathFinished(Entity entity, GraphPath<Tile> path) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        ILevel level = ECS.currentLevel;
        return path.get(path.getCount() - 1)
                .equals(level.getTileAt(pc.getPosition().toCoordinate()));
    }
}
