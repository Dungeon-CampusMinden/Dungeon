package core.components;

import core.Component;
import core.Entity;
import core.utils.Vector2;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
 * use {@link #acceleration(Vector2)} to set the speed at which the entity should move.
 *
 * <p>Use {@link #onWallHit} to set a callback that will be executed if the entity runs against a
 * wall.
 */
public final class VelocityComponent implements Component {

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};
  private Consumer<Entity> onWallHit;

  private Map<String, Vector2> appliedForces = new HashMap<>();

  private Vector2 currentVelocity = Vector2.ZERO;

  private float maxSpeed;
  private boolean canEnterOpenPits;

  /**
   * Create a new VelocityComponent with the given configuration.
   *
   * @param maxSpeed The Speed with which the entity can maximal move.
   * @param onWallHit Callback that will be executed if the entity runs against a wall.
   * @param canEnterOpenPits Whether the entity enter open pit tiles.
   */
  public VelocityComponent(float maxSpeed, Consumer<Entity> onWallHit, boolean canEnterOpenPits) {
    this.onWallHit = onWallHit;
    this.canEnterOpenPits = canEnterOpenPits;
    this.maxSpeed = maxSpeed;
  }

  /**
   * Create a new VelocityComponent with the given configuration. By default, the entity will not be
   * able to enter open pit tiles.
   *
   * @param maxSpeed The Speed with which the entity can maximal move.
   */
  public VelocityComponent(float maxSpeed) {
    this(maxSpeed, DEFAULT_ON_WALL_HIT, false);
  }

  /**
   * Create a new VelocityComponent with the default configuration.
   *
   * <p>In the default configuration, the movement speed is set to 0, so the entity will not move.
   * And the entity will not be able to enter open pit tiles.
   */
  public VelocityComponent() {
    this(0f);
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

  public void applyForce(String id, Vector2 force) {
    removeForce(id);
    appliedForces.put(id, force);
  }

  public void removeForce(String id) {
    appliedForces.remove(id);
  }

  public void clearForces() {
    appliedForces.clear();
  }

  public Stream<Vector2> appliedForces() {
    return appliedForces.values().stream();
  }

  public float maxSpeed() {
    return maxSpeed;
  }

  public void maSpeed(float maxSpeed) {
    this.maxSpeed = maxSpeed;
  }
}
