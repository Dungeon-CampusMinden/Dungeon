package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import java.util.logging.Logger;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/**
 * The VelocityComponent class represents a component that stores the x and y movement direction and
 * the movement animations of an entity. This component is used to control the speed and animations
 * of the associated entity.
 */
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
     * Constructs a VelocityComponent with the given associated entity, x and y movement speed, and
     * move animations.
     *
     * @param entity the associated entity
     * @param xVelocity the speed at which the entity moves on the x-axis
     * @param yVelocity the speed at which the entity moves on the y-axis
     * @param moveLeftAnimation the animation that plays when the entity moves to the left
     * @param moveRightAnimation the animation that plays when the entity moves to the right
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
     * Constructs a VelocityComponent with the given associated entity and default values for speed
     * and move animations.
     *
     * @param entity the associated entity
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
     * Gets the current x movement of the associated entity.
     *
     * @return the current x movement
     */
    public float getCurrentXVelocity() {
        return currentXVelocity;
    }

    /**
     * Sets the current x movement of the associated entity.
     *
     * @param currentXVelocity the current x movement to set
     */
    public void setCurrentXVelocity(float currentXVelocity) {
        this.currentXVelocity = currentXVelocity;
    }

    /**
     * Gets the current y movement of the associated entity.
     *
     * @return the current y movement
     */
    public float getCurrentYVelocity() {
        return currentYVelocity;
    }

    /**
     * Sets the current y movement of the associated entity.
     *
     * @param currentYVelocity the current y movement to set
     */
    public void setCurrentYVelocity(float currentYVelocity) {
        this.currentYVelocity = currentYVelocity;
    }

    /**
     * Returns the speed at which the entity can move on the x-axis.
     *
     * @return the speed at which the entity can move on the x-axis
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
     * Sets the speed at which the entity can move on the x-axis.
     *
     * @param xVelocity the new speed at which the entity can move on the x-axis
     */
    public void setXVelocity(float xVelocity) {
        this.xVelocity = xVelocity;
    }

    /**
     * Returns the speed at which the entity can move on the y-axis.
     *
     * @return the speed at which the entity can move on the y-axis
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
     * Sets the speed at which the entity can move on the y-axis.
     *
     * @param yVelocity the new speed at which the entity can move on the y-axis
     */
    public void setYVelocity(float yVelocity) {
        this.yVelocity = yVelocity;
    }

    /**
     * Returns the animation that plays when the entity moves to the right.
     *
     * @return the animation that plays when the entity moves to the right
     */
    public Animation getMoveRightAnimation() {
        return moveRightAnimation;
    }

    /**
     * Returns the animation that plays when the entity moves to the left.
     *
     * @return the animation that plays when the entity moves to the left
     */
    public Animation getMoveLeftAnimation() {
        return moveLeftAnimation;
    }
}
