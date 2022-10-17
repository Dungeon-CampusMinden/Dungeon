package character.monster;

import character.DungeonCharacter;
import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import com.badlogic.gdx.ai.pfa.GraphPath;
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
            switch (d) {
                case N:
                    d = checkUP(d);
                    return convertTileDirectionToCharacterDirection(d);
                case S:
                    d = checkDown(d);
                    return convertTileDirectionToCharacterDirection(d);
                case E:
                    d = checkRight(d);
                    return convertTileDirectionToCharacterDirection(d);
                case W:
                    d = checkLeft(d);
                    return convertTileDirectionToCharacterDirection(d);
                default:
                    System.out.println("??");
            }
        } catch (Exception e) {
            e.printStackTrace();
            calculateGoal(true);
        }
        return CharacterDirection.NONE;
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

    private Tile.Direction checkLeft(Tile.Direction d) {
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
                d = Tile.Direction.N;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_LEFT].y + tmp.y)))
                    .isAccessible()) {
                // top left collision move to the bottom
                d = Tile.Direction.S;
            }
        }
        return d;
    }

    private Tile.Direction checkRight(Tile.Direction d) {
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
                d = Tile.Direction.N;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // top right collision move to the bottom
                d = Tile.Direction.S;
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
    private Tile.Direction checkDown(Tile.Direction d) {
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
                d = Tile.Direction.E;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_BOTTOM_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // bottom right collision move to the left
                d = Tile.Direction.W;
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
    private Tile.Direction checkUP(Tile.Direction d) {
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
                d = Tile.Direction.E;
            }
            if (!currentLevel
                    .getTileAt(
                            new Coordinate(
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].x + tmp.x),
                                    (int) (corners[Hitbox.CORNER_TOP_RIGHT].y + tmp.y)))
                    .isAccessible()) {
                // top right collision move to the left
                d = Tile.Direction.W;
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
