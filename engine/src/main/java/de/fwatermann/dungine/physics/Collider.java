package de.fwatermann.dungine.physics;

/**
 * The Collider class is an abstract base class for all collider objects in the physics engine. It
 * defines a method for detecting collisions with other colliders.
 */
public abstract class Collider {

  /**
   * Determines if this collider collides with another collider.
   *
   * @param collider the other collider to check for collision with
   * @return true if this collider collides with the other collider, false otherwise
   */
  public abstract boolean collide(Collider other);
}
