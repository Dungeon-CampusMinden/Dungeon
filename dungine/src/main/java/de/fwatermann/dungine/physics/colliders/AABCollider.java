package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.ecs.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

/**
 * A collider that represents an axis-aligned bounding box. This collider is used to check for
 * collisions with other colliders.
 */
public class AABCollider extends BoxCollider {

  static {
    registerCollisionFunction(AABCollider.class, AABCollider.class, AABCollider::collideAABs);
  }

  /**
   * Create a new AABCollider for the given entity with offset and size.
   *
   * @param entity The entity this collider is attached to.
   * @param offset The offset of the collider.
   * @param size The size of the collider.
   */
  public AABCollider(Entity entity, Vector3f offset, Vector3f size) {
    super(entity, offset, size);
  }

  private static CollisionResult collideAABs(Collider pA, Collider pB) {
    if (!(pA instanceof AABCollider a)) {
      throw new IllegalStateException(
          String.format(
              "Cannot collide AABCollider with collider of type \"%s\"!", pA.getClass().getName()));
    }
    if (!(pB instanceof AABCollider b)) {
      throw new IllegalStateException(
          String.format(
              "Cannot collide AABCollider with collider of type \"%s\"!", pB.getClass().getName()));
    }
    if (!aabCheck(a, b)) return CollisionResult.NO_COLLISION;

    Collision collision =
        new Collision(a.worldPosition().sub(b.worldPosition(), new Vector3f()), 0.0f, Set.of());
    return new CollisionResult(true, collision);
  }

  @Override
  public Quaternionf rotation() {
    // Return identity as this is an Axis Aligned Box.
    return new Quaternionf();
  }

  @Override
  public BoxCollider rotation(Quaternionf rotation) {
    // Do nothing as this is an Axis Aligned Box.
    return this;
  }
}
