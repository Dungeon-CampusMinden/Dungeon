package de.fwatermann.dungine.physics;

import de.fwatermann.dungine.ecs.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Collider class is an abstract base class for all collider objects in the physics engine. It
 * defines a method for detecting collisions with other colliders.
 */
public abstract class Collider {

  protected final Entity entity;
  protected Vector3f offset;
  protected Quaternionf rotation;

  public Collider(Entity entity, Vector3f offset, Quaternionf rotation) {
    this.entity = entity;
    this.offset = offset;
    this.rotation = rotation;
  }

  public Collider(Entity entity, Vector3f offset) {
    this(entity, offset, new Quaternionf());
  }

  public Collider(Entity entity) {
    this(entity, new Vector3f(), new Quaternionf());
  }

  /**
   * Determines if this collider collides with another collider.
   *
   * @param other the other collider to check for collision with
   * @return true if this collider collides with the other collider, false otherwise
   */
  public abstract CollisionResult collide(Collider other);

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
   * Get the offset of the collider relative to the owning entity's position.
   *
   * @return the offset of the collider
   */
  public Vector3f offset() {
    return this.offset;
  }

  /**
   * Set the offset of the collider relative to the owning entity's position.
   *
   * @param offset the new offset of the collider
   * @return this collider
   */
  public Collider offset(Vector3f offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Get the world position of the collider while respecting the offset and rotation.
   *
   * @return the world position of the collider
   */
  public Vector3f worldPosition() {
    return new Vector3f(this.offset).rotate(this.entity.rotation()).add(this.entity.position());
  }

  /**
   * Gets the rotation of the collider relative to the owning entity's rotation.
   *
   * @return the rotation of the collider
   */
  public Quaternionf rotation() {
    return this.rotation;
  }

  /**
   * Sets the rotation of the collider relative to the owning entity's rotation.
   *
   * @param rotation the new rotation of the collider
   * @return this collider instance for method chaining
   */
  public Collider rotation(Quaternionf rotation) {
    this.rotation = rotation;
    return this;
  }

  /**
   * Gets the entity associated with this collider.
   *
   * @return the entity associated with this collider
   */
  public Entity entity() {
    return this.entity;
  }

  public record CollisionResult(boolean collided, Vector3f normal, float depth) {
    public static final CollisionResult NO_COLLISION = new CollisionResult(false, null, 0);
  }

}
