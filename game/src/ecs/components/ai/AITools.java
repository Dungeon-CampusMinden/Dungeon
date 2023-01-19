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
     * Finds the path to a random (accessible) tile in the given radius, starting from the position
     * of the given entity.
     *
     * @param entity Entity whose position is the center point
     * @param radius Search radius
     * @return Path from the position of the entity to the randomly selected tile
     */
    public static GraphPath<Tile> calculateNewPath(Entity entity, float radius) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.name);
        if (pc != null && vc != null) {
            ILevel level = ECS.currentLevel;
            Point position = pc.getPosition();
            List<Tile> tiles = new ArrayList<>();
            for (float x = position.x - radius; x <= position.x + radius; x++) {
                for (float y = position.y - radius; y <= position.y + radius; y++) {
                    tiles.add(level.getTileAt(new Point(x, y).toCoordinate()));
                }
            }
            tiles.removeIf(Objects::isNull);
            tiles.removeIf(tile -> !tile.isAccessible());
            Coordinate newPosition = tiles.get(random.nextInt(tiles.size())).getCoordinate();
            return level.findPath(
                    level.getTileAt(position.toCoordinate()), level.getTileAt(newPosition));
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
    public static GraphPath<Tile> calculateNewPath(Entity from, Entity to) {
        PositionComponent myPositionComponent =
                (PositionComponent) from.getComponent(PositionComponent.name);
        PositionComponent heroPositionComponent =
                (PositionComponent) to.getComponent(PositionComponent.name);
        if (myPositionComponent != null && heroPositionComponent != null) {
            ILevel level = ECS.currentLevel;
            Coordinate myPosition = myPositionComponent.getPosition().toCoordinate();
            Coordinate heroposition = heroPositionComponent.getPosition().toCoordinate();
            return level.findPath(level.getTileAt(myPosition), level.getTileAt(heroposition));
        }
        return null;
    }

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
     * Checks if the position of the player is within the given radius of the position of the given
     * entity.
     *
     * @param entity Entity whose position specifies the center point
     * @param range Reichweite die betrachtet werden soll
     * @return Ob sich der Spieler in Reichweite befindet
     */
    public static boolean playerInRange(Entity entity, float range) {
        PositionComponent myPositionComponent =
                (PositionComponent) entity.getComponent(PositionComponent.name);
        if (ECS.hero != null) {
            PositionComponent heroPositionComponent =
                    (PositionComponent) ECS.hero.getComponent(PositionComponent.name);
            if (heroPositionComponent != null) {
                Point myPosition = myPositionComponent.getPosition();
                Point heroPosition = heroPositionComponent.getPosition();

                float xDiff = myPosition.x - heroPosition.x;
                float yDiff = myPosition.y - heroPosition.y;
                float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                return distance <= range;
            }
        }
        return false;
    }
}
