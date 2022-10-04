package character;

import basiselements.AnimatableElement;
import collision.CharacterDirection;
import collision.Colideable;
import collision.Hitbox;
import graphic.Animation;
import level.elements.ILevel;
import tools.Point;

/** Characters in the Dugenon. Characters can move, have animations and collision. */
public abstract class DungeonCharacter extends AnimatableElement implements Colideable {

    protected Point currentPosition;
    protected Animation currentAnimation;
    protected ILevel currentLevel;
    protected float movementSpeed;
    protected Hitbox hitbox;
    protected boolean alive = true;

    /**
     * @param movementSpeed Speed per Frame
     * @param hitbox Hitbox
     */
    public DungeonCharacter(float movementSpeed, Hitbox hitbox) {
        this.movementSpeed = movementSpeed;
        this.hitbox = hitbox;
    }

    /**
     * @return the direction this character wants to move
     */
    protected abstract CharacterDirection getDirection();

    /**
     * Set the currentAnimation based on the movement direction
     *
     * @param direction Movement Direction
     */
    protected abstract void setAnimation(CharacterDirection direction);

    /**
     * Move the character
     *
     * @return if the character was moved
     */
    protected boolean move() {
        Point tmp;
        CharacterDirection direction = getDirection();
        switch (direction) {
            case UP:
                tmp = moveup();
                break;
            case DOWN:
                tmp = movedown();
                break;
            case LEFT:
                tmp = moveleft();
                break;
            case RIGHT:
                tmp = moveright();
                break;
            default:
                tmp = currentPosition;
                break;
        }
        // check if character can move in this direction
        if (tmp != currentPosition && isHitboxOnFloor(tmp)) {
            currentPosition = tmp;
            setAnimation(direction);
            return true;
        }
        setAnimation(CharacterDirection.NONE);
        return false;
    }

    /**
     * Check if the full Hitbox is on a Floor-Tile
     *
     * @param newPosition Position for the bottom left corner of the hibbox
     * @return if the full Hitbox is on a Floor-Tile
     */
    protected boolean isHitboxOnFloor(Point newPosition) {
        Point[] corners = hitbox.getCorners();
        for (int i = 0; i < 4; i++) {
            Point corner = new Point(newPosition.x + corners[i].x, newPosition.y + corners[i].y);
            if (currentLevel.getTileAt(corner.toCoordinate()) == null
                    || !currentLevel.getTileAt(corner.toCoordinate()).isAccessible()) return false;
        }
        return true;
    }

    /**
     * @return New Position if the Character would move up
     */
    protected Point moveup() {
        return new Point(currentPosition.x, currentPosition.y + movementSpeed);
    }

    /**
     * @return New Position if the Character would move down
     */
    protected Point movedown() {
        return new Point(currentPosition.x, currentPosition.y - movementSpeed);
    }

    /**
     * @return New Position if the Character would move left
     */
    protected Point moveleft() {
        return new Point(currentPosition.x - movementSpeed, currentPosition.y);
    }

    /**
     * @return New Position if the Character would move right
     */
    protected Point moveright() {
        return new Point(currentPosition.x + movementSpeed, currentPosition.y);
    }

    /**
     * Set the current Level this Character is in
     *
     * @param level
     */
    public void setLevel(ILevel level) {
        this.currentLevel = level;
    }

    @Override
    public void update() {
        move();
        hitbox.setPosition(currentPosition);
    }

    @Override
    public Point getPosition() {
        return currentPosition;
    }

    @Override
    public Animation getActiveAnimation() {
        return currentAnimation;
    }

    @Override
    public Hitbox getHitbox() {
        return this.hitbox;
    }

    @Override
    public boolean removable() {
        return !alive;
    }

    protected void die() {
        alive = false;
    }
}
