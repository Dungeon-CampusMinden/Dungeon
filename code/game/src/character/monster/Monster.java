package character.monster;

import character.DungeonCharacter;
import collision.CharacterDirection;
import collision.Collidable;
import collision.CollidableLevel;
import collision.Hitbox;
import com.badlogic.gdx.ai.pfa.GraphPath;
import level.elements.ILevel;
import level.elements.Tile;
import level.tools.Coordinate;
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
            CharacterDirection direction = convertTileDirectionToCharacterDirection(d);
            ILevel level = currentLevel;
            if (level.getClass() == CollidableLevel.class) {
                var collidable = ((CollidableLevel) level).getCollidables();
                // TODO: create a better fix for hitbox
                var temp = currentPosition;
                switch (direction) {
                    case UP:
                        currentPosition = moveup();
                        break;
                    case DOWN:
                        currentPosition = movedown();
                        break;
                    case RIGHT:
                        currentPosition = moveright();
                        break;
                    case LEFT:
                        currentPosition = moveleft();
                        break;
                }

                for (Collidable collideable : collidable) {
                    // hitbox has to be moved to new position
                    var col = hitbox.collide(collideable.getHitbox());
                    if (col != CharacterDirection.NONE) {
                        // collision

                        currentPosition = temp;
                        return handlePathCollision(direction);
                    }
                }
                currentPosition = temp;
                return convertTileDirectionToCharacterDirection(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
            calculateGoal(true);
        }
        return CharacterDirection.NONE;
    }

    private CharacterDirection handlePathCollision(CharacterDirection direction) {
        switch (direction) {
            case UP:
                return checkUP(direction);
            case DOWN:
                return checkDown(direction);
            case RIGHT:
                return checkRight(direction);
            case LEFT:
                return checkLeft(direction);
            default:
                return direction;
        }
    }

    private CharacterDirection convertTileDirectionToCharacterDirection(Tile.Direction d) {
        switch (d) {
            case N:
                return CharacterDirection.UP;
            case E:
                return CharacterDirection.RIGHT;
            case S:
                return CharacterDirection.DOWN;
            case W:
                return CharacterDirection.LEFT;
            default:
                return CharacterDirection.NONE;
        }
    }

    private CharacterDirection checkLeft(CharacterDirection d) {
        var tmp = moveleft();
        if (!isHitboxOnFloor(tmp)) {
            // check top left and bottom left for collision !
            var corners = hitbox.getCorners();
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_BOTTOM_LEFT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_BOTTOM_LEFT].y + tmp.y)))
                    .isAccessible()) {
                // bottom left collision move to the TOP
                d = CharacterDirection.DOWN;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].y + tmp.y)))
                    .isAccessible()) {
                // top left collision move to the bottom
                d = CharacterDirection.DOWN;
            }
        }
        return d;
    }

    private CharacterDirection checkRight(CharacterDirection d) {
        var tmp = moveright();
        if (!isHitboxOnFloor(tmp)) {
            // check top right and bottom right for collision !
            var corners = hitbox.getCorners();
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // bottom right collision move to the TOP
                d = CharacterDirection.DOWN;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // top right collision move to the bottom
                d = CharacterDirection.DOWN;
            }
        }
        return d;
    }

    /**
     * simple check for moving down
     *
     * @param d the planned direction
     * @return the recommended direction
     */
    private CharacterDirection checkDown(CharacterDirection d) {
        var tmp = movedown();
        if (!isHitboxOnFloor(tmp)) {
            // check bottom left and bottom right for collision ! first to collide and also
            var corners = hitbox.getCorners();
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_BOTTOM_LEFT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_BOTTOM_LEFT].y + tmp.y)))
                    .isAccessible()) {
                // bottom left collision move to the right
                d = CharacterDirection.LEFT;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // bottom right collision move to the left
                d = CharacterDirection.LEFT;
            }
        }
        return d;
    }

    /**
     * simple check for moving up
     *
     * @param d the planned direction
     * @return the recommended direction
     */
    private CharacterDirection checkUP(CharacterDirection d) {
        var tmp = moveup();
        if (!isHitboxOnFloor(tmp)) {
            // check top left and top right for collision ! first to collide and also
            var corners = hitbox.getCorners();
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].y + tmp.y)))
                    .isAccessible()) {
                // top left collision move to the right
                d = CharacterDirection.LEFT;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // top right collision move to the left
                d = CharacterDirection.LEFT;
            }
        }
        return d;
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
