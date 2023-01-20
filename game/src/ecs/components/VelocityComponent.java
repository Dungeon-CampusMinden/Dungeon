package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/** VelocityComponent is a component that stores the x, y movement direction */
@DSLType
public class VelocityComponent extends Component {
    public static String name = "VelocityComponent";
    private float x;
    private float y;
    private @DSLTypeMember float xSpeed;
    private @DSLTypeMember float ySpeed;

    private @DSLTypeMember Animation moveRightAnimation;
    private @DSLTypeMember Animation moveLeftAnimation;

    /**
     * @param entity associated entity
     * @param x x coordinate
     * @param y y coordinate
     * @param xSpeed Speed with which the entity moves on the x-axis
     * @param ySpeed Speed with which the entity moves on the y-axis
     * @param moveLeftAnimation Animation that plays when the entity moves to the left
     * @param moveRightAnimation Animation that plays when the entity moves to the right
     */
    public VelocityComponent(
            Entity entity,
            float x,
            float y,
            float xSpeed,
            float ySpeed,
            Animation moveLeftAnimation,
            Animation moveRightAnimation) {
        super(entity, name);
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.moveLeftAnimation = moveLeftAnimation;
        this.moveRightAnimation = moveRightAnimation;
    }

    /**
     * @param entity associated entity
     * @param x x coordinate
     * @param y y coordinate
     * @param xSpeed Speed with which the entity moves on the x-axis
     * @param ySpeed Speed with which the entity moves on the y-axis
     * @param moveAnimation Animation that plays when the entity moves
     */
    public VelocityComponent(
            Entity entity, float x, float y, float xSpeed, float ySpeed, Animation moveAnimation) {
        this(entity, x, y, xSpeed, ySpeed, moveAnimation, moveAnimation);
    }

    /**
     * @param entity associated entity
     */
    public VelocityComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        this.x = 0;
        this.y = 0;
        this.xSpeed = 0;
        this.ySpeed = 0;
        this.moveLeftAnimation = null;
        this.moveRightAnimation = null;
    }

    /**
     * @return x movement
     */
    public float getX() {
        return x;
    }

    /**
     * @param x set x coordinate
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return y movement
     */
    public float getY() {
        return y;
    }

    /**
     * @param y set y coordinate
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * @return speed with which the entity moves on the x-axis
     */
    public float getxSpeed() {
        return xSpeed;
    }

    /**
     * Set speed with which the entity moves on the x-axis
     *
     * @param xSpeed
     */
    public void setxSpeed(float xSpeed) {
        this.xSpeed = xSpeed;
    }

    /**
     * @return Speed with which the entity moves on the y-axis
     */
    public float getySpeed() {
        return ySpeed;
    }
    /**
     * Set speed with which the entity moves on the y-axis
     *
     * @param ySpeed
     */
    public void setySpeed(float ySpeed) {
        this.ySpeed = ySpeed;
    }

    /**
     * @return Animation that plays when the entity moves to the right
     */
    public Animation getMoveRightAnimation() {
        return moveRightAnimation;
    }

    /**
     * @return Animation that plays when the entity moves to the left
     */
    public Animation getMoveLeftAnimation() {
        return moveLeftAnimation;
    }
}
