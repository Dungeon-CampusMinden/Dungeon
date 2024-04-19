package core.components;

import core.Component;
import core.Entity;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import java.util.function.Consumer;

/**
 * Allows the associated entity to move in the dungeon.
 *
 * <p>The VelocityComponent stores the speed at which the entity can move along the x and y axes.
 *
 * <p>It also keeps track of the current movement speed on the x and y axes. This information will
 * be used by the {@link core.systems.VelocitySystem} to move the entity at the given speed.
 *
 * <p>The current movement speed can be set by other systems, such as the {@link
 * core.systems.PlayerSystem}.
 *
 * <p>Note that a positive velocity means that the entity is moving right or up, and a negative
 * velocity means that the entity is moving left or down. If the current x and y velocity are 0,
 * that means the entity is not moving.
 *
 * <p>Use {@link #yVelocity(float)} or {@link #xVelocity(float)} to change the current velocity.
 * Normally, you want to use the {@link #xVelocity(float)} or {@link #yVelocity(float)} as the
 * parameter for this.
 *
 * <p>Use {@link #onWallHit} to set a callback that will be executed if the entity runs against a
 * wall.
 */
@DSLType(name = "velocity_component")
public final class VelocityComponent implements Component {

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};
  private float currentXVelocity;
  private float currentYVelocity;
  private @DSLTypeMember(name = "x_velocity") float xVelocity;
  private @DSLTypeMember(name = "y_velocity") float yVelocity;
  private float previousXVelocity;
  private float previousYVelocity;
  private Consumer<Entity> onWallHit;
  private boolean canEnterEmptyTiles;

  /**
   * Create a new VelocityComponent with the given configuration.
   *
   * @param xVelocity Speed with which the entity can move on the x-axis.
   * @param yVelocity Speed with which the entity can move on the y-axis.
   * @param onWallHit Callback that will be executed if the entity runs against a wall.
   */
  public VelocityComponent(
      float xVelocity, float yVelocity, Consumer<Entity> onWallHit, boolean canEnterEmptyTiles) {
    this.currentXVelocity = 0;
    this.currentYVelocity = 0;
    this.xVelocity = xVelocity;
    this.yVelocity = yVelocity;
    this.onWallHit = onWallHit;
    this.canEnterEmptyTiles = canEnterEmptyTiles;
  }

  /**
   * Create a new VelocityComponent with the given configuration.
   *
   * @param xVelocity Speed with which the entity can move on the x-axis.
   * @param yVelocity Speed with which the entity can move on the y-axis.
   */
  public VelocityComponent(float xVelocity, float yVelocity) {
    this(xVelocity, yVelocity, DEFAULT_ON_WALL_HIT, false);
  }

  /**
   * Create a new VelocityComponent with the default configuration.
   *
   * <p>In the default configuration, the movement speed is set to 0, so the entity will not move.
   */
  public VelocityComponent() {
    this(0, 0, DEFAULT_ON_WALL_HIT, false);
  }

  /**
   * Get the current x-velocity speed.
   *
   * <p>Note that a positive velocity means that the entity is moving right, and a negative velocity
   * means that the entity is moving left. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @return Current velocity on the x-axis.
   */
  public float currentXVelocity() {
    return currentXVelocity;
  }

  /**
   * Set the current velocity on the x-axis. This value will be used by the {@link
   * core.systems.VelocitySystem} to calculate the next position of this entity.
   *
   * <p>Note that a positive velocity means that the entity is moving right, and a negative velocity
   * means that the entity is moving left. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @param currentXVelocity Set the current speed on the x-axis.
   */
  public void currentXVelocity(float currentXVelocity) {
    this.currentXVelocity = currentXVelocity;
  }

  /**
   * Get the current y-velocity speed.
   *
   * <p>Note that a positive velocity means that the entity is moving up, and a negative velocity
   * means that the entity is moving down. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @return Current velocity on the y-axis.
   */
  public float currentYVelocity() {
    return currentYVelocity;
  }

  /**
   * Set the current velocity on the y-axis. This value will be used by the {@link
   * core.systems.VelocitySystem} to calculate the next position of this entity.
   *
   * <p>Note that a positive velocity means that the entity is moving up, and a negative velocity
   * means that the entity is moving down. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @param currentYVelocity Set the current speed on the y-axis.
   */
  public void currentYVelocity(float currentYVelocity) {
    this.currentYVelocity = currentYVelocity;
  }

  /**
   * Get the velocity with which the entity should move on the x-axis.
   *
   * <p>This value will be used by other systems to set the current velocity.
   *
   * @return Velocity with which the entity should move on the x-axis.
   */
  public float xVelocity() {
    return xVelocity;
  }

  /**
   * Set the velocity with which the entity should move on the x-axis.
   *
   * <p>This value will be used by other systems to set the current velocity.
   *
   * @param xVelocity Set the speed with which the entity should move on the x-axis.
   */
  public void xVelocity(float xVelocity) {
    this.xVelocity = xVelocity;
  }

  /**
   * Get the velocity with which the entity should move on the y-axis.
   *
   * <p>This value will be used by other systems to set the current velocity.
   *
   * @return Velocity with which the entity should move on the y-axis.
   */
  public float yVelocity() {
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
   * Set the previous x velocity of the entity.
   *
   * <p>This method is used to store the last moved x velocity of the entity. This information is
   * helpful when the entity stops, as it allows maintaining the direction.
   *
   * @param previousXVelocity The last x velocity of the entity.
   */
  public void previousXVelocity(float previousXVelocity) {
    this.previousXVelocity = previousXVelocity;
  }

  /**
   * Get the previous x velocity from the last movement.
   *
   * @return The x velocity from the last movement.
   */
  public float previousXVelocity() {
    return previousXVelocity;
  }

  /**
   * Set the previous y velocity of the entity.
   *
   * <p>This method is used to store the last moved x velocity of the entity. This information is
   * helpful when the entity stops, as it allows maintaining the direction.
   *
   * @param previousYVelocity The last y velocity of the entity.
   */
  public void previousYVelocity(float previousYVelocity) {
    this.previousYVelocity = previousYVelocity;
  }

  /**
   * Get the previous y velocity from the last movement.
   *
   * @return The y velocity from the last movement.
   */
  public float previousYVelocity() {
    return previousYVelocity;
  }

  /**
   * Sets the behavior when a wall is hit.
   *
   * @param onWallHit The callback to be executed when a wall is hit.
   */
  public void onWallHit(final Consumer<Entity> onWallHit) {
    this.onWallHit = onWallHit;
  }

  /**
   * Get the callback that should be executed if the entity hits a wall.
   *
   * @return The behavior when a wall is hit.
   */
  public Consumer<Entity> onWallHit() {
    return onWallHit;
  }

  /**
   * Set if the entity can enter empty tiles.
   *
   * @param canEnterEmptyTiles True if the entity can enter empty tiles, false if not.
   */
  public void canEnterEmptyTiles(boolean canEnterEmptyTiles) {
    this.canEnterEmptyTiles = canEnterEmptyTiles;
  }

  /**
   * Get if the entity can enter empty tiles.
   *
   * @return True if the entity can enter empty tiles, false if not.
   */
  public boolean canEnterEmptyTiles() {
    return canEnterEmptyTiles;
  }
}
