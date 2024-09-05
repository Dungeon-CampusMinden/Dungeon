package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/**
 * The Collider class is an abstract base class for all collider objects in the physics engine. It
 * defines a method for detecting collisions with other colliders.
 */
public abstract class Collider {

  private static final Logger LOGGER = LogManager.getLogger(Collider.class);

  private static final Map<
          Class<? extends Collider>,
          Map<Class<? extends Collider>, IFunction2P<CollisionResult, Collider, Collider>>>
      collisionFunctions = new HashMap<>();

  /**
   * Register a collision function for two colliders. Use this function if you want to define a
   * custom collider.
   *
   * @param colliderA the first collider class
   * @param colliderB the second collider class
   * @param function the collision function
   */
  public static void registerCollisionFunction(
      Class<? extends Collider> colliderA,
      Class<? extends Collider> colliderB,
      IFunction2P<CollisionResult, Collider, Collider> function) {
    collisionFunctions.computeIfAbsent(colliderA, k -> new HashMap<>()).put(colliderB, function);
    collisionFunctions.computeIfAbsent(colliderB, k -> new HashMap<>()).put(colliderA, function);
  }

  /**
   * Unregister a collision function for two colliders.
   *
   * @param colliderA the first collider class
   * @param colliderB the second collider class
   */
  public static void unregisterCollisionFunction(
      Class<? extends Collider> colliderA, Class<? extends Collider> colliderB) {
    Optional.ofNullable(collisionFunctions.get(colliderA)).ifPresent(map -> map.remove(colliderB));
    Optional.ofNullable(collisionFunctions.get(colliderB)).ifPresent(map -> map.remove(colliderA));
  }

  private static IFunction2P<CollisionResult, Collider, Collider> getCollisionFunction(
    Class<? extends Collider> colliderA, Class<? extends Collider> colliderB) {
    Map<Class<? extends Collider>, IFunction2P<CollisionResult, Collider, Collider>> map =
      collisionFunctions.get(colliderA);
    return map != null ? map.get(colliderB) : null;
  }

  protected Entity entity;
  protected Vector3f offset;

  public Collider(Entity entity, Vector3f offset) {
    this.entity = entity;
    this.offset = offset;
  }

  public Collider(Entity entity) {
    this(entity, new Vector3f());
  }

  /**
   * Determines if this collider collides with another collider.
   *
   * @param other the other collider to check for collision with
   * @return CollisionResult object containing information about the collision
   */
  public CollisionResult collide(Collider other) {
    IFunction2P<CollisionResult, Collider, Collider> func = getCollisionFunction(this.getClass(), other.getClass());
    if(func == null) {
      LOGGER.warn("Missing collision function for colliders {} and {}", this.getClass().getName(), other.getClass().getName());
      return CollisionResult.NO_COLLISION;
    }
    return func.run(this, other);
  }

  /**
   * Get the minimum world position of the colliders bounding box. (The bounding box is the smallest
   * AAB that contains the collision shape):
   *
   * @return the minimum world position of the colliders bounding box
   */
  public abstract Vector3f min();

  /**
   * Get the maximum world position of the colliders bounding box. (The bounding box is the smallest
   * AAB that contains the collision shape):
   *
   * @return the maximum world position of the colliders bounding box
   */
  public abstract Vector3f max();

  /**
   * Get the center of the collider.
   * @return the center of the collider
   */
  public abstract Vector3f center();

  /**
   * Get the offset of the collider relative to the owning entity's position.
   *
   * @return the offset of the collider
   */
  public Vector3f offset() {
    return new Vector3f(this.offset);
  }

  /**
   * Set the offset of the collider relative to the owning entity's position.
   *
   * @param offset the new offset of the collider
   * @return this collider
   */
  public Collider offset(Vector3f offset) {
    this.offset = offset;
    this.transformationChanged();
    return this;
  }

  /**
   * Get the world position of the collider while respecting the offset and rotation.
   *
   * @return the world position of the collider
   */
  public Vector3f worldPosition() {
    return this.entity
        .rotation()
        .transform(this.offset, new Vector3f())
        .add(this.entity.position());
  }

  /** Is called when the transformation of the collider changes. */
  protected void transformationChanged() {}

}
