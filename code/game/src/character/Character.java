package character;

import basiselements.AnimatableElement;
import graphic.Animation;
import level.elements.ILevel;
import myDungeon.collision.Colideable;
import myDungeon.collision.Hitbox;
import tools.Point;

public abstract class Character extends AnimatableElement implements Colideable {

    protected Point currentPosition;
    protected Animation currentAnimation;
    protected ILevel currentLevel;
    protected float movementSpeed;
    protected Hitbox hitbox;
    protected boolean alive=true;

    public Character(float movementSpeed, Hitbox hitbox) {
        this.movementSpeed = movementSpeed;
        this.hitbox = hitbox;
    }

    /**
     * @return the direction this character wants to move
     */
    protected abstract Direction getDirection();

    protected abstract void setAnimation(Direction direction);

    protected boolean move() {
        Point tmp;
        Direction direction = getDirection();
        switch (direction) {
            case UP:
                tmp = moveup();
                break;
            case DOWN:
                tmp = movedown();
                break;
            case LEFT:
                tmp = moveright();
                break;
            case RIGHT:
                tmp = moveleft();
                break;
            default:
                tmp = currentPosition;
                break;
        }
        if (tmp != currentPosition && isHitboxOnFloor(tmp)) {
            currentPosition = tmp;
            setAnimation(direction);
            return true;
        }
        setAnimation(Direction.NONE);
        return false;
    }

    protected boolean isHitboxOnFloor(Point newPosition) {
        Point[] corners = hitbox.getCorners();
        for (int i = 0; i < 4; i++) {
            Point corner = new Point(newPosition.x + corners[i].x, newPosition.y + corners[i].y);
            if (currentLevel.getTileAt(corner.toCoordinate()) == null
                || !currentLevel.getTileAt(corner.toCoordinate()).isAccessible()) return false;
        }
        return true;
    }

    protected Point moveup() {
        return new Point(currentPosition.x, currentPosition.y + movementSpeed);
    }

    protected Point movedown() {
        return new Point(currentPosition.x, currentPosition.y - movementSpeed);
    }

    protected Point moveleft() {
        return new Point(currentPosition.x - movementSpeed, currentPosition.y);
    }

    protected Point moveright() {
        return new Point(currentPosition.x + movementSpeed, currentPosition.y);
    }

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
    public boolean removable(){
        return !alive;
    }

    protected void die(){
        alive=false;
    }
}
