package ecs.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import mydungeon.ECS;
import tools.Point;

public class AITools {
    private static Random random = new Random();

    public static GraphPath<Tile> calculateNewPath(Entity entity, float radius) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.name);
        VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.name);
        if (pc != null && vc != null) {
            ILevel level = ECS.currentLevel;
            Point position = pc.getPosition();
            Tile currentTile = level.getTileAt(position.toCoordinate());
            List<Tile> tiles = new ArrayList<>();
            for (float x = position.x - radius; x <= position.x + radius; x++) {
                for (float y = position.y - radius; y <= position.y + radius; y++) {
                    tiles.add(level.getTileAt(new Point(x, y).toCoordinate()));
                }
            }
            tiles.removeIf(tile -> tile == null);
            tiles.removeIf(tile -> !tile.isAccessible());
            Coordinate newPosition = tiles.get(random.nextInt(tiles.size())).getCoordinate();
            GraphPath path =
                    level.findPath(
                            level.getTileAt(position.toCoordinate()), level.getTileAt(newPosition));

            System.out.println("PATH " + path.getCount());
            return path;
        }
        return null;
    }

    public static GraphPath<Tile> calculateNewPath(Entity entity, Entity entity2) {
        PositionComponent myPositionComponent =
                (PositionComponent) entity.getComponent(PositionComponent.name);
        PositionComponent heroPositionComponent =
                (PositionComponent) entity2.getComponent(PositionComponent.name);
        if (myPositionComponent != null && heroPositionComponent != null) {
            ILevel level = ECS.currentLevel;
            Coordinate myPosition = myPositionComponent.getPosition().toCoordinate();
            Coordinate heroposition = heroPositionComponent.getPosition().toCoordinate();
            return level.findPath(level.getTileAt(myPosition), level.getTileAt(heroposition));
        }
        return null;
    }

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
            case N:
                vc.setY(vc.getySpeed());
                break;
            case S:
                vc.setY(-vc.getySpeed());
                break;
            case E:
                vc.setX(vc.getxSpeed());
                break;
            case W:
                vc.setX(-vc.getxSpeed());
                break;
        }
        if (currentTile.directionTo(nextTile).length > 1)
            switch (currentTile.directionTo(nextTile)[1]) {
                case N:
                    vc.setY(vc.getySpeed());
                    break;
                case S:
                    vc.setY(-vc.getySpeed());
                    break;
                case E:
                    vc.setX(vc.getxSpeed());
                    break;
                case W:
                    vc.setX(-vc.getxSpeed());
                    break;
            }
    }

    public static boolean inRange(Entity entity, float range) {
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
