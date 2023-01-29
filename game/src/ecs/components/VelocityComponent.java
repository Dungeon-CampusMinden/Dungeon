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
    private float currentXVelocity;
    private float currentYVelocity;
    private @DSLTypeMember float XVelocity;
    private @DSLTypeMember float YVelocity;

    private @DSLTypeMember Animation moveRightAnimation;
    private @DSLTypeMember Animation moveLeftAnimation;

    /**
     * @param entity associated entity
     * @param XVelocity Speed with which the entity moves on the x-axis
     * @param YVelocity Speed with which the entity moves on the y-axis
     * @param moveLeftAnimation Animation that plays when the entity moves to the left
     * @param moveRightAnimation Animation that plays when the entity moves to the right
     */
    public VelocityComponent(
            Entity entity,
            float XVelocity,
            float YVelocity,
            Animation moveLeftAnimation,
            Animation moveRightAnimation) {
        super(entity, name);
        this.currentXVelocity = 0;
        this.currentYVelocity = 0;
        this.XVelocity = XVelocity;
        this.YVelocity = YVelocity;
        this.moveLeftAnimation = moveLeftAnimation;
        this.moveRightAnimation = moveRightAnimation;
    }

    /**
     * @param entity associated entity
     * @param currentXVelocity current x velocity
     * @param currentYVelocity current y velocity
     * @param XVelocity Speed with which the entity moves on the x-axis
     * @param YVelocity Speed with which the entity moves on the y-axis
     * @param moveAnimation Animation that plays when the entity moves
     */
    public VelocityComponent(
        Entity entity, float currentXVelocity, float currentYVelocity, float XVelocity, float YVelocity, Animation moveAnimation) {
        this(entity, XVelocity, YVelocity, moveAnimation, moveAnimation);
    }

    /**
     * @param entity associated entity
     */
    public VelocityComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        this.currentXVelocity = 0;
        this.currentYVelocity = 0;
        this.XVelocity = 0;
        this.YVelocity = 0;
        this.moveLeftAnimation = null;
        this.moveRightAnimation = null;
    }

    /**
     * @return x movement
     */
    public float getCurrentXVelocity() {
        return currentXVelocity;
    }

    /**
     * @param currentXVelocity set x velocity
     */
    public void setCurrentXVelocity(float currentXVelocity) {
        this.currentXVelocity = currentXVelocity;
    }

    /**
     * @return y velocity
     */
    public float getCurrentYVelocity() {
        return currentYVelocity;
    }

    /**
     * @param currentYVelocity set y velocity
     */
    public void setCurrentYVelocity(float currentYVelocity) {
        this.currentYVelocity = currentYVelocity;
    }

    /**
     * @return speed with which the entity moves on the x-axis
     */
    public float getXVelocity() {
        return XVelocity;
    }

    /**
     * Set speed with which the entity moves on the x-axis
     *
     * @param XVelocity
     */
    public void setXVelocity(float XVelocity) {
        this.XVelocity = XVelocity;
    }

    /**
     * @return Speed with which the entity moves on the y-axis
     */
    public float getYVelocity() {
        return YVelocity;
    }
    /**
     * Set speed with which the entity moves on the y-axis
     *
     * @param YVelocity
     */
    public void setYVelocity(float YVelocity) {
        this.YVelocity = YVelocity;
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
