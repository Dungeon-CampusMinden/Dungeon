package de.fwatermann.dungine.physics.colliders;

import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

/**
 * The `Collision` record represents a collision event in the physics engine. It contains
 * information about the collision normal, depth, and the points of collision.
 *
 * @param normal the normal of the collision
 * @param depth the depth of the collision
 * @param collisionPoints the points of collision
 */
public record Collision(Vector3f normal, float depth, Set<Vector3f> collisionPoints) {

  /**
   * Checks if this `Collision` is equal to another object.
   *
   * @param obj the object to compare with
   * @return true if the specified object is equal to this `Collision`, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Collision other)) return false;
    return this.normal.equals(other.normal)
        && this.depth == other.depth
        && this.collisionPoints.equals(other.collisionPoints);
  }

  /**
   * Returns the hash code value for this `Collision`.
   *
   * @return the hash code value for this `Collision`
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.normal, this.depth, this.collisionPoints);
  }
}
