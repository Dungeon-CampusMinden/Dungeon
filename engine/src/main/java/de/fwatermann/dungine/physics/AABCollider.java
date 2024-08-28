package de.fwatermann.dungine.physics;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Vector3f;

/**
 * A collider that represents an axis-aligned bounding box. This collider is used to check for
 * collisions with other colliders.
 */
public class AABCollider extends Collider {

  private static final Map<Class<? extends Collider>, IFunction2P<CollisionResult, AABCollider, Collider>>
      collisionFunctions = new HashMap<>();

  static {
    collisionFunctions.put(AABCollider.class, AABCollider::collideBox);
    collisionFunctions.put(SphereCollider.class, AABCollider::collideSphere);
  }

  private Vector3f size = new Vector3f(0, 0, 0);

  public AABCollider(Entity entity, Vector3f offset, Vector3f size) {
    super(entity, offset);
    this.size = size;
  }

  public static void registerCollisionFunction(
    Class<? extends Collider> other, IFunction2P<CollisionResult, AABCollider, Collider> function) {
    collisionFunctions.put(other, function);
  }

  public static void unregisterCollisionFunction(Class<? extends Collider> other) {
    collisionFunctions.remove(other);
  }

  @Override
  public CollisionResult collide(Collider other) {
    Optional<IFunction2P<CollisionResult, AABCollider, Collider>> func =
        Optional.ofNullable(collisionFunctions.get(other.getClass()));
    if (func.isPresent()) {
      return func.get().run(this, other);
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "%s does not support collision with collider of type \"%s\"!",
              this.getClass().getName(), other.getClass().getName()));
    }
  }

  private static CollisionResult collideBox(AABCollider a, Collider b) {
    if (!(b instanceof AABCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with other BoxColliders!");
    }

    Vector3f aMin = a.min();
    Vector3f aMax = a.max();
    Vector3f oMin = other.min();
    Vector3f oMax = other.max();

    if (aMax.x < oMin.x || aMin.x > oMax.x) {
      return CollisionResult.NO_COLLISION;
    }
    if (aMax.y < oMin.y || aMin.y > oMax.y) {
      return CollisionResult.NO_COLLISION;
    }
    if (aMax.z < oMin.z || aMin.z > oMax.z) {
      return CollisionResult.NO_COLLISION;
    }

    Vector3f normal = new Vector3f();
    float minOverlap = Float.MAX_VALUE;
    float overlap;
    if ((overlap = aMax.x - oMin.x) < minOverlap) {
      minOverlap = overlap;
      normal.set(-1, 0, 0);
    }
    if ((overlap = oMax.x - aMin.x) < minOverlap) {
      minOverlap = overlap;
      normal.set(1, 0, 0);
    }
    if ((overlap = aMax.y - oMin.y) < minOverlap) {
      minOverlap = overlap;
      normal.set(0, -1, 0);
    }
    if ((overlap = oMax.y - aMin.y) < minOverlap) {
      minOverlap = overlap;
      normal.set(0, 1, 0);
    }
    if ((overlap = aMax.z - oMin.z) < minOverlap) {
      minOverlap = overlap;
      normal.set(0, 0, -1);
    }
    if ((overlap = oMax.z - aMin.z) < minOverlap) {
      minOverlap = overlap;
      normal.set(0, 0, 1);
    }

    return new CollisionResult(true, normal, minOverlap);
  }

  private static CollisionResult collideSphere(AABCollider a, Collider b) {
    if (!(b instanceof SphereCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with SphereColliders!");
    }
    return other.collide(
        a); // BoxCollider/SphereCollider collision is implemented in SphereCollider.
  }

  public Vector3f min() {
    return this.worldPosition();
  }

  public Vector3f max() {
    return this.worldPosition().add(this.size);
  }

  public Vector3f size() {
    return this.size;
  }

  public AABCollider size(Vector3f size) {
    this.size = size;
    return this;
  }

}
