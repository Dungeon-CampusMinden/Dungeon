package de.fwatermann.dungine.physics.colliders;

import java.util.List;

/**
 * The `CollisionResult` class represents the result of a collision detection operation. It contains
 * information about whether a collision occurred and the details of the collisions.
 */
public class CollisionResult {

  /** A `CollisionResult` object representing no collision. */
  public static final CollisionResult NO_COLLISION = new CollisionResult(false);

  private final boolean collided;
  private final List<Collision> collisions;

  /**
   * Constructs a `CollisionResult` with the specified collision status and collision details.
   *
   * @param collided whether a collision occurred
   * @param collisions the details of the collisions
   * @throws IllegalArgumentException if `collided` is true but no collision points are provided
   */
  public CollisionResult(boolean collided, Collision... collisions) {
    this.collided = collided;
    if (collided && collisions.length == 0) {
      throw new IllegalArgumentException("Collided is true but no collision points are provided.");
    }
    this.collisions = List.of(collisions);
  }

  /**
   * Returns whether a collision occurred.
   *
   * @return true if a collision occurred, false otherwise
   */
  public boolean collided() {
    return this.collided;
  }

  /**
   * Returns the details of the collisions.
   *
   * @return a list of `Collision` objects representing the details of the collisions
   */
  public List<Collision> collisions() {
    return this.collisions;
  }
}
