package character.monster;

import collision.Hitbox;
import com.badlogic.gdx.ai.pfa.GraphPath;
import level.elements.Tile;
import level.tools.LevelElement;
import myDungeon.character.Character;
import myDungeon.collision.Colideable;
import myDungeon.collision.Hitbox;
import tools.Point;

public abstract class Monster extends Character {

    private Point currentGoal;
    public Monster(float movementSpeed, Hitbox hitbox) {
        super(movementSpeed, hitbox);
    }

    @Override
    //TODO dont work
    protected Colideable.Direction getDirection() {
        calculateGoal(false);
        try {
            // todo effektiver machen
            Tile currentTile = currentLevel.getTileAt(currentPosition.toCoordinate());
            GraphPath<Tile> path =
                currentLevel.findPath(
                    currentTile, currentLevel.getTileAt(currentGoal.toCoordinate()));

            Tile nextTile = path.get(1);
            Tile.Direction d = currentTile.directionTo(nextTile)[0];
            switch (d) {
                case N:
                    return Direction.UP;
                case S:
                    return Direction.DOWN;
                case E:
                    return Direction.RIGHT;
                case W:
                    return Direction.LEFT;
                default: System.out.println("??");
            }
        } catch (Exception e) {
            //  e.printStackTrace();
            calculateGoal(true);
        }
        return Direction.NONE;
    }

    protected void calculateGoal(boolean force) {
        if (currentGoal == null || currentGoal.equals(currentPosition) || force)
            currentGoal = currentLevel.getRandomTilePoint(LevelElement.FLOOR);
    }
}
