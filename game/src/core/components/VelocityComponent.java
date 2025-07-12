package core.components;

import core.Component;
import core.Entity;
import core.utils.Vector2;
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
 * <p>Use {@link #currentVelocity(Vector2)} to change the current velocity. Normally, you want to
 * use {@link #velocity(Vector2)} to set the speed at which the entity should move.
 *
 * <p>Use {@link #onWallHit} to set a callback that will be executed if the entity runs against a
 * wall.
 */
public final class VelocityComponent implements Component {

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};
  private Vector2 velocity;
  private Vector2 currentVelocity = Vector2.ZERO;
  private Vector2 previousVelocity = Vector2.ZERO;
  private Consumer<Entity> onWallHit;
  private boolean canEnterOpenPits;

  /**
   * Create a new VelocityComponent with the given configuration.
   *
   * @param velocity The Speed with which the entity can move.
   * @param onWallHit Callback that will be executed if the entity runs against a wall.
   * @param canEnterOpenPits Whether the entity enter open pit tiles.
   */
  public VelocityComponent(Vector2 velocity, Consumer<Entity> onWallHit, boolean canEnterOpenPits) {
    this.velocity = velocity;
    this.onWallHit = onWallHit;
    this.canEnterOpenPits = canEnterOpenPits;
  }

  /**
   * Create a new VelocityComponent with the given configuration. By default, the entity will not be
   * able to enter open pit tiles.
   *
   * @param velocity The Speed with which the entity can move.
   */
  public VelocityComponent(Vector2 velocity) {
    this(velocity, DEFAULT_ON_WALL_HIT, false);
  }

  /**
   * Create a new VelocityComponent with the default configuration.
   *
   * <p>In the default configuration, the movement speed is set to 0, so the entity will not move.
   * And the entity will not be able to enter open pit tiles.
   */
  public VelocityComponent() {
    this(Vector2.ZERO);
  }

  /**
   * Get the current velocity speed.
   *
   * <p>Note that a positive velocity means that the entity is moving right, and a negative velocity
   * means that the entity is moving left. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @return Current velocity.
   */
  public Vector2 currentVelocity() {
    return currentVelocity;
  }

  /**
   * Set the current velocity. This value will be used by the {@link core.systems.VelocitySystem} to
   * calculate the next position of this entity.
   *
   * <p>Note that a positive velocity means that the entity is moving right, and a negative velocity
   * means that the entity is moving left. If the x and y velocity are 0, that means the entity is
   * currently not moving.
   *
   * @param newCurrentVelocity Set the current speed on.
   */
  public void currentVelocity(Vector2 newCurrentVelocity) {
    this.currentVelocity = newCurrentVelocity;
  }

  /**
   * Get the velocity with which the entity should move.
   *
   * <p>This value will be used by other systems to set the current velocity.
   *
   * @return Velocity with which the entity should move on.
   */
  public Vector2 velocity() {
    return velocity;
  }

  /**
   * Set the velocity with which the entity should move.
   *
   * <p>This value will be used by other systems to set the current velocity.
   *
   * @param newVelocity Set the speed with which the entity should move on.
   */
  public void velocity(Vector2 newVelocity) {
    this.velocity = newVelocity;
  }

  /**
   * Get the previous velocity from the last movement.
   *
   * @return The velocity from the last movement.
   */
  public Vector2 previouXVelocity() {
    return previousVelocity;
  }

  /**
   * Set the previous velocity of the entity.
   *
   * <p>This method is used to store the last moved velocity of the entity. This information is
   * helpful when the entity stops, as it allows maintaining the direction.
   *
   * @param previousVelocity The last velocity of the entity.
   */
  public void previousVelocity(Vector2 previousVelocity) {
    this.previousVelocity = previousVelocity;
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
   * Set whether the entity can enter open pit tiles.
   *
   * @param canEnterOpenPits Whether the entity can enter open pit tiles.
   */
  public void canEnterOpenPits(boolean canEnterOpenPits) {
    this.canEnterOpenPits = canEnterOpenPits;
  }

  /**
   * Get whether the entity can enter open pit tiles.
   *
   * @return Whether the entity can enter open pit tiles.
   */
  public boolean canEnterOpenPits() {
    return this.canEnterOpenPits;
  }
}
