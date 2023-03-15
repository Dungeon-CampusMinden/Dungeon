package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import java.util.logging.Logger;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/** VelocityComponent is a component that stores the x, y movement direction */
@DSLType(name = "velocity_component")
public class VelocityComponent extends Component {
    private static List<String> missingTexture = List.of("animation/missingTexture.png");
    private float currentXVelocity;
    private float currentYVelocity;
    private @DSLTypeMember(name = "x_velocity") float xVelocity;
    private @DSLTypeMember(name = "y_velocity") float yVelocity;

    private @DSLTypeMember(name = "move_right_animation") Animation moveRightAnimation;
    private @DSLTypeMember(name = "move_left_animation") Animation moveLeftAnimation;
    private final Logger velocityCompLogger = Logger.getLogger(this.getClass().getName());

    /**
     * @param entity associated entity
     * @param xVelocity Speed with which the entity moves on the x-axis
     * @param yVelocity Speed with which the entity moves on the y-axis
     * @param moveLeftAnimation Animation that plays when the entity moves to the left
     * @param moveRightAnimation Animation that plays when the entity moves to the right
     */
    public VelocityComponent(
            Entity entity,
            float xVelocity,
            float yVelocity,
            Animation moveLeftAnimation,
            Animation moveRightAnimation) {
        super(entity);
        this.currentXVelocity = 0;
        this.currentYVelocity = 0;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.moveLeftAnimation = moveLeftAnimation;
        this.moveRightAnimation = moveRightAnimation;
    }

    /**
     * @param entity associated entity
     */
    public VelocityComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.currentXVelocity = 0;
        this.currentYVelocity = 0;
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.moveLeftAnimation = new Animation(missingTexture, 100);
        this.moveRightAnimation = new Animation(missingTexture, 100);
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
        velocityCompLogger.log(
                CustomLogLevel.DEBUG,
                "Fetching x-velocity for entity '"
                        + entity.getClass().getSimpleName()
                        + "': "
                        + xVelocity);
        return xVelocity;
    }

    /**
     * Set speed with which the entity moves on the x-axis
     *
     * @param xVelocity
     */
    public void setXVelocity(float xVelocity) {
        this.xVelocity = xVelocity;
    }

    /**
     * @return Speed with which the entity moves on the y-axis
     */
    public float getYVelocity() {
        velocityCompLogger.log(
                CustomLogLevel.DEBUG,
                "Fetching y-velocity for entity '"
                        + entity.getClass().getSimpleName()
                        + "': "
                        + yVelocity);
        return yVelocity;
    }
    /**
     * Set speed with which the entity moves on the y-axis
     *
     * @param yVelocity
     */
    public void setYVelocity(float yVelocity) {
        this.yVelocity = yVelocity;
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
