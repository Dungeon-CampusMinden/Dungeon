package de.fwatermann.dungine.physics.colliders;

import java.util.List;

public class CollisionResult {

  public static final CollisionResult NO_COLLISION = new CollisionResult(false);

  private final boolean collided;
  private final List<Collision> collisions;

  public CollisionResult(boolean collided, Collision... collisions) {
    this.collided = collided;
    if (collided && collisions.length == 0) {
      throw new IllegalArgumentException("Collided is true but no collision points are provided.");
    }
    this.collisions = List.of(collisions);
  }

  public boolean collided() {
    return this.collided;
  }

  public List<Collision> collisions() {
    return this.collisions;
  }
}
