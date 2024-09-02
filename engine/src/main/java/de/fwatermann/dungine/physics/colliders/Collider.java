package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import org.joml.Vector3f;

/**
 * The Collider class is an abstract base class for all collider objects in the physics engine. It
 * defines a method for detecting collisions with other colliders.
 */
public abstract class Collider {

  protected final RigidBodyComponent rbc;
  protected Vector3f offset;

  public Collider(RigidBodyComponent rbc, Vector3f offset) {
    this.rbc = rbc;
    this.offset = offset;
  }

  public Collider(RigidBodyComponent rbc) {
    this(rbc, new Vector3f());
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
    return this.rbc.entity().rotation().transform(this.offset, new Vector3f()).add(this.rbc.entity().position());
  }

}
