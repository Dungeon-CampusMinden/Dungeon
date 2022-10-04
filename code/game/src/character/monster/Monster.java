package character.monster;

import character.DungeonCharacter;
import collision.CharacterDirection;
import collision.Hitbox;
import com.badlogic.gdx.ai.pfa.GraphPath;
import level.elements.Tile;
import level.tools.LevelElement;
import tools.Point;

public abstract class Monster extends DungeonCharacter {

    private Point currentGoal;

    public Monster(float movementSpeed, Hitbox hitbox) {
        super(movementSpeed, hitbox);
    }

    @Override
    // TODO dont work
    protected CharacterDirection getDirection() {
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
                    return CharacterDirection.UP;
                case S:
                    return CharacterDirection.DOWN;
                case E:
                    return CharacterDirection.RIGHT;
                case W:
                    return CharacterDirection.LEFT;
                default:
                    System.out.println("??");
            }
        } catch (Exception e) {
            //  e.printStackTrace();
            calculateGoal(true);
        }
        return CharacterDirection.NONE;
    }

    protected void calculateGoal(boolean force) {
        if (currentGoal == null || currentGoal.equals(currentPosition) || force)
            currentGoal = currentLevel.getRandomTilePoint(LevelElement.FLOOR);
    }
}
