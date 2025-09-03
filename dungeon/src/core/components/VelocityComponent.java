package core.components;

import core.Component;
import core.Entity;
import core.systems.InputSystem;
import core.utils.Vector2;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Allows the associated entity to move in the dungeon by applying forces that affect its velocity.
 *
 * <p>The {@code VelocityComponent} stores the current velocity vector of the entity along the x and
 * y axes, as well as a set of applied forces that influence this velocity. Instead of setting
 * acceleration directly, external systems apply forces using {@link #applyForce(String, Vector2)}.
 * The sum of all forces determines the acceleration and ultimately the current velocity.
 *
 * <p>The velocity system (e.g., {@link core.systems.VelocitySystem}) is responsible for integrating
 * these forces to update the velocity and move the entity accordingly.
 *
 * <p>Use {@link #applyForce(String, Vector2)} to add or update a force acting on the entity. Use
 * {@link #removeForce(String)} to remove a force. Use {@link #currentVelocity()} and {@link
 * #currentVelocity(Vector2)} to get or set the current velocity directly.
 *
 * <p>The current movement speed can be set by other systems, such as the {@link InputSystem}.
 *
 * <p>A positive velocity means the entity moves right (x) or up (y), negative velocity means moving
 * left or down. If both components are zero, the entity is stationary.
 *
 * <p>Use {@link #onWallHit} to set a callback that is executed if the entity collides with a wall.
 *
 * <h3>Usage Examples</h3>
 *
 * <pre>{@code
 * // Create a VelocityComponent with max speed 5.0f and a custom wall-hit callback
 * VelocityComponent vc = new VelocityComponent(5.0f, entity -> {
 *     System.out.println("Entity hit a wall: " + entity.id());
 * }, false);
 *
 * // Apply forces like gravity or wind which affect the velocity in the next update
 * vc.applyForce("gravity", new Vector2(0f, -9.8f));
 * vc.applyForce("wind", new Vector2(2f, 0f));
 *
 * // Remove a force when it's no longer applicable
 * vc.removeForce("wind");
 *
 * // Directly set current velocity if needed (overrides forces)
 * vc.currentVelocity(new Vector2(1.0f, 0f)); // move right at speed 1
 *
 * // Check if entity can enter pits
 * boolean canEnterPits = vc.canEnterOpenPits();
 * vc.canEnterOpenPits(true);
 * }</pre>
 */
public final class VelocityComponent implements Component {
  /** The default mass of an entity, on no other is configurated. */
  public static final float DEFAULT_MASS = 1;

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};

  private Consumer<Entity> onWallHit;

  private final Map<String, Vector2> appliedForces = new HashMap<>();

  private Vector2 currentVelocity = Vector2.ZERO;

  private float mass;
  private float maxSpeed;

  private boolean canEnterOpenPits;

  /**
   * Create a new VelocityComponent with the given configuration.
   *
   * @param maxSpeed The speed with which the entity can maximally move.
   * @param mass The mass of the entity used to calculate acceleration.
   * @param onWallHit Callback that will be executed if the entity runs against a wall.
   * @param canEnterOpenPits Whether the entity can enter open pit tiles.
   */
  public VelocityComponent(
      float maxSpeed, float mass, Consumer<Entity> onWallHit, boolean canEnterOpenPits) {
    this.mass(mass);
    this.onWallHit = onWallHit;
    this.canEnterOpenPits = canEnterOpenPits;
    this.maxSpeed = maxSpeed;
  }

  /**
   * Create a new VelocityComponent with the given configuration and a default mass.
   *
   * @param maxSpeed The speed with which the entity can maximally move.
   * @param onWallHit Callback that will be executed if the entity runs against a wall.
   * @param canEnterOpenPits Whether the entity can enter open pit tiles.
   */
  public VelocityComponent(float maxSpeed, Consumer<Entity> onWallHit, boolean canEnterOpenPits) {
    this(maxSpeed, DEFAULT_MASS, onWallHit, canEnterOpenPits);
  }

  /**
   * Create a new VelocityComponent with the given configuration. By default, the entity will not be
   * able to enter open pit tiles and has a mass of 1.
   *
   * @param maxSpeed The speed with which the entity can maximally move.
   */
  public VelocityComponent(float maxSpeed) {
    this(maxSpeed, DEFAULT_ON_WALL_HIT, false);
  }

  /**
   * Create a new VelocityComponent with the default configuration.
   *
   * <p>In the default configuration, the movement speed is set to 0, so the entity will not move.
   * And the entity will not be able to enter open pit tiles. The entity has a mass of 1.
   */
  public VelocityComponent() {
    this(0f);
  }

  /**
   * Get the current velocity vector.
   *
   * <p>A positive velocity means movement to the right (x) or up (y). Negative values mean movement
   * left or down.
   *
   * @return Current velocity vector.
   */
  public Vector2 currentVelocity() {
    return currentVelocity;
  }

  /**
   * Set the current velocity vector. This value will be used by the velocity system to move the
   * entity.
   *
   * <p>Setting this directly overrides velocity computed from forces.
   *
   * @param newCurrentVelocity The new velocity vector.
   */
  public void currentVelocity(Vector2 newCurrentVelocity) {
    this.currentVelocity = newCurrentVelocity;
  }

  /**
   * Sets the callback to be executed if the entity hits a wall.
   *
   * @param onWallHit The callback consumer.
   */
  public void onWallHit(final Consumer<Entity> onWallHit) {
    this.onWallHit = onWallHit;
  }

  /**
   * Get the callback executed when the entity hits a wall.
   *
   * @return The on-wall-hit callback.
   */
  public Consumer<Entity> onWallHit() {
    return onWallHit;
  }

  /**
   * Set whether the entity can enter open pit tiles.
   *
   * @param canEnterOpenPits true if entity can enter pits, false otherwise.
   */
  public void canEnterOpenPits(boolean canEnterOpenPits) {
    this.canEnterOpenPits = canEnterOpenPits;
  }

  /**
   * Check if the entity can enter open pit tiles.
   *
   * @return true if it can enter pits, false otherwise.
   */
  public boolean canEnterOpenPits() {
    return canEnterOpenPits;
  }

  /**
   * Apply or update a force acting on the entity.
   *
   * <p>Multiple forces can be applied simultaneously, identified by unique IDs. The total force
   * affects acceleration and velocity in the velocity system.
   *
   * @param id Unique identifier of the force.
   * @param force The force vector.
   */
  public void applyForce(String id, Vector2 force) {
    removeForce(id);
    appliedForces.put(id, force);
  }

  /**
   * Remove a previously applied force by its ID.
   *
   * @param id The unique identifier of the force to remove.
   */
  public void removeForce(String id) {
    appliedForces.remove(id);
  }

  /**
   * Get the force vector applied for a given ID, if present.
   *
   * @param id The force ID.
   * @return Optional containing the force vector or empty if none found.
   */
  public Optional<Vector2> force(String id) {
    return Optional.ofNullable(appliedForces.get(id));
  }

  /** Remove all applied forces. */
  public void clearForces() {
    appliedForces.clear();
  }

  /**
   * Get a stream of all currently applied forces.
   *
   * @return Stream of force vectors.
   */
  public Stream<Vector2> appliedForcesStream() {
    return appliedForces.values().stream();
  }

  /**
   * Get a copy of all applied forces mapped by their IDs.
   *
   * @return Map of force IDs to force vectors.
   */
  public Map<String, Vector2> appliedForces() {
    return new HashMap<>(appliedForces);
  }

  /**
   * Get the mass of the entity.
   *
   * @return Mass of the entity
   */
  public float mass() {
    return this.mass;
  }

  /**
   * Sets the mass of the entity.
   *
   * <p>Mass must be greater than 0.
   *
   * @param mass the mass to set
   * @throws IllegalArgumentException if mass is less than or equal to 0
   */
  public void mass(float mass) {
    if (mass <= 0) throw new IllegalArgumentException("Mass cannot be 0 or less");
    this.mass = mass;
  }

  /**
   * Get the maximum movement speed.
   *
   * @return Maximum speed value.
   */
  public float maxSpeed() {
    return maxSpeed;
  }

  /**
   * Set the maximum movement speed.
   *
   * @param maxSpeed Maximum speed to set.
   */
  public void maxSpeed(float maxSpeed) {
    this.maxSpeed = maxSpeed;
  }
}
