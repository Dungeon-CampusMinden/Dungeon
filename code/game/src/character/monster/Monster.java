package character.monster;

import character.DungeonCharacter;
import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import com.badlogic.gdx.ai.pfa.GraphPath;
import level.elements.Tile;
import level.tools.LevelElement;
import tools.Point;

/** Monster */
public abstract class Monster extends DungeonCharacter {

    // curent Point this Monster wants to move to
    private Point currentGoal;

    public Monster(float movementSpeed, Hitbox hitbox) {
        super(movementSpeed, hitbox);
    }

    @Override
    // TODO dont work
    protected CharacterDirection getDirection() {
        calculateGoal(false);
        try {
            // todo Path speichern, damit nicht jeden Frame der Weg neu berechnet werden muss
            Tile currentTile = currentLevel.getTileAt(currentPosition.toCoordinate());
            GraphPath<Tile> path =
                    currentLevel.findPath(
                            currentTile, currentLevel.getTileAt(currentGoal.toCoordinate()));

            // todo warum ist index 0 leer?
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
            e.printStackTrace();
            calculateGoal(true);
        }
        return CharacterDirection.NONE;
    }

    /**
     * Find a new goal, if old goal is reached
     *
     * @param force set True if you want to force a new goal
     */
    protected void calculateGoal(boolean force) {
        if (currentGoal == null || currentGoal.equals(currentPosition) || force)
            currentGoal = currentLevel.getRandomTilePoint(LevelElement.FLOOR);
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {
        // todo
    }
}
