package core.components;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import dsl.semanticanalysis.types.annotation.DSLType;
import dsl.semanticanalysis.types.annotation.DSLTypeMember;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Allow associated entity to move in the dungeon.
 *
 * <p>The VelocityComponent stores the speed at which the entity can move along the x and y axes.
 *
 * <p>It also stores the current movement speed on the x and y axes. This information will be used
 * by the {@link core.systems.VelocitySystem} to move the entity at the given speed.
 *
 * <p>The current movement speed can be set by other Systems like the {@link
 * core.systems.PlayerSystem}.
 *
 * <p>Note that a positive velocity means that the entity is moving right or up, and a negative
 * velocity means that the entity is moving left/down. If the current x and y velocity is 0, that
 * means the entity is not moving.
 *
 * <p>Use {@link #yVelocity(float)} or {@link #xVelocity(float)} to change the current velocity.
 * Normally you want to use the {@link #xVelocity} or {@link #yVelocity} as parameter this.
 */
@DSLType(name = "velocity_component")
public final class VelocityComponent implements Component {

    private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private float currentXVelocity;
    private float currentYVelocity;
    private @DSLTypeMember(name = "x_velocity") float xVelocity;
    private @DSLTypeMember(name = "y_velocity") float yVelocity;
    private float previousXVelocity;
    private float previousYVelocity;
    private Consumer<Entity> onWallHit;

    /**
     * Create a new VelocityComponent with the given configuration.
     *
     * @param xVelocity Speed with which the entity can move on the x-axis
     * @param yVelocity Speed with which the entity can move on the y-axis
     */
    public VelocityComponent(float xVelocity, float yVelocity, Consumer<Entity> onWallHit) {
        this.currentXVelocity = 0;
        this.currentYVelocity = 0;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.onWallHit = onWallHit;
    }

    /**
     * Create a new VelocityComponent with the given configuration.
     *
     * @param xVelocity Speed with which the entity can move on the x-axis
     * @param yVelocity Speed with which the entity can move on the y-axis
     */
    public VelocityComponent(float xVelocity, float yVelocity) {
        this(xVelocity, yVelocity, DEFAULT_ON_WALL_HIT);
    }

    /**
     * Create a new VelocityComponent with the default configuration.
     *
     * <p>In the default configuration, the movement-speed is set to 0, so the entity will not move.
     */
    public VelocityComponent() {
        this(0, 0, DEFAULT_ON_WALL_HIT);
    }

    /**
     * Get the current x-velocity speed
     *
     * <p>Note that a positive velocity means that the entity is moving right and a negative
     * velocity means that the entity is moving left. If the x and y velocity is 0, that means the
     * entity is currently not moving.
     *
     * @return current velocity on the x-axis
     */
    public float currentXVelocity() {
        return currentXVelocity;
    }

    /**
     * Set the current velocity on the x-axis. This value will be used by the {@link
     * core.systems.VelocitySystem} to calculate the next position of this entity.
     *
     * <p>Note that a positive velocity means that the entity is moving right and a negative
     * velocity means that the entity is moving left. If the x and y velocity is 0, that means the
     * entity is currently not moving.
     *
     * @param currentXVelocity set current speed on the x-axis
     */
    public void currentXVelocity(float currentXVelocity) {
        this.currentXVelocity = currentXVelocity;
    }

    /**
     * Get the current y-velocity speed
     *
     * <p>Note that a positive velocity means that the entity is moving up and a negative velocity
     * means that the entity is moving down. If the x and y velocity is 0, that means the entity is
     * currently not moving.
     *
     * @return current velocity on the y-axis
     */
    public float currentYVelocity() {
        return currentYVelocity;
    }

    /**
     * Set the current velocity on the y-axis. This value will be used by the {@link
     * core.systems.VelocitySystem} to calculate the next position of this entity.
     *
     * <p>Note that a positive velocity means that the entity is moving up and a negative velocity
     * means that the entity is moving down. If the x and y velocity is 0, that means the entity is
     * currently not moving.
     *
     * @param currentYVelocity set current speed on the x-axis
     */
    public void currentYVelocity(float currentYVelocity) {
        this.currentYVelocity = currentYVelocity;
    }

    /**
     * Get the velocity with which the entity should move on the x-axis.
     *
     * <p>This value will be used by other systems to set the current-velocity.
     *
     * @return velocity with which the entity should move on the x-axis
     */
    public float xVelocity() {
        LOGGER.log(CustomLogLevel.DEBUG, "Fetching x-velocity for entity '" + "': " + xVelocity);
        return xVelocity;
    }

    /**
     * Set the velocity with which the entity should move on the x-axis.
     *
     * <p>This value will be used by other systems to set the current-velocity.
     *
     * @param xVelocity set speed with which the entity can should on the x-axis
     */
    public void xVelocity(float xVelocity) {
        this.xVelocity = xVelocity;
    }

    /**
     * Get the velocity with which the entity should move on the y-axis.
     *
     * <p>This value will be used by other systems to set the current-velocity.
     *
     * @return velocity with which the entity should move on the y-axis
     */
    public float yVelocity() {
        LOGGER.log(CustomLogLevel.DEBUG, "Fetching y-velocity for entity '" + "': " + yVelocity);
        return yVelocity;
    }

    /**
     * Set the velocity with which the entity should move on the y-axis.
     *
     * <p>This value will be used by other systems to set the current-velocity.
     *
     * @param yVelocity set speed with which the entity can should on the y-axis
     */
    public void yVelocity(float yVelocity) {
        this.yVelocity = yVelocity;
    }

    /**
     * contains the last moved x velocity the entity moved. This helps once the entity stops to keep
     * the direction.
     */
    public void previousXVelocity(float previousXVelocity) {
        this.previousXVelocity = previousXVelocity;
    }

    /**
     * @return the x velocity from the last movement
     */
    public float previousXVelocity() {
        return previousXVelocity;
    }

    /**
     * contains the last moved y velocity the entity moved. This helps once the entity stops to keep
     * the direction.
     */
    public void previousYVelocity(float previousYVelocity) {
        this.previousYVelocity = previousYVelocity;
    }

    /**
     * @return the y velocity from the last movement
     */
    public float previousYVelocity() {
        return previousYVelocity;
    }

    /** Sets the behavior when a wall is hit. */
    public void onWallHit(Consumer<Entity> onWallHit) {
        this.onWallHit = onWallHit;
    }

    /**
     * @return The behavior when a wall is hit.
     */
    public Consumer<Entity> onWallHit() {
        return onWallHit;
    }
}
