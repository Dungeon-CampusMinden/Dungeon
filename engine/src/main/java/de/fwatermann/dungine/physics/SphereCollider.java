package de.fwatermann.dungine.physics;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Math;
import org.joml.Vector3f;

public class SphereCollider extends Collider {

  private static final Map<
          Class<? extends Collider>, IFunction2P<CollisionResult, SphereCollider, Collider>>
      collisionFunctions = new HashMap<>();

  static {
    collisionFunctions.put(AABCollider.class, SphereCollider::collideBox);
    collisionFunctions.put(SphereCollider.class, SphereCollider::collideSphere);
  }

  private float radius;

  public SphereCollider(Entity entity, Vector3f offset, float radius) {
    super(entity, offset);
    this.radius = radius;
  }

  public SphereCollider(Entity entity, float radius) {
    super(entity);
    this.radius = radius;
  }

  public static void registerCollisionFunction(
      Class<? extends Collider> other, IFunction2P<CollisionResult, SphereCollider, Collider> function) {
    collisionFunctions.put(other, function);
  }

  public static void unregisterCollisionFunction(Class<? extends Collider> other) {
    collisionFunctions.remove(other);
  }

  @Override
  public CollisionResult collide(Collider other) {
    Optional<IFunction2P<CollisionResult, SphereCollider, Collider>> func =
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

  private static CollisionResult collideBox(SphereCollider a, Collider b) {
    if (!(b instanceof AABCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with other BoxColliders!");
    }
    Vector3f sMin = a.worldPosition().sub(a.radius, a.radius, a.radius);
    Vector3f sMax = a.worldPosition().add(a.radius, a.radius, a.radius);

    Vector3f otherMin = other.min();
    Vector3f otherMax = other.max();

    // AABB check for approximation
    if (sMax.x < otherMin.x || sMin.x > otherMax.x) {
      return CollisionResult.NO_COLLISION;
    }
    if (sMax.y < otherMin.y || sMin.y > otherMax.y) {
      return CollisionResult.NO_COLLISION;
    }
    if (sMax.z < otherMin.z || sMin.z > otherMax.z) {
      return CollisionResult.NO_COLLISION;
    }

    Vector3f center = a.worldPosition();

    // Check if sphere is inside box
    if (center.x >= otherMin.x
        && center.x <= otherMax.x
        && center.y >= otherMin.y
        && center.y <= otherMax.y
        && center.z >= otherMin.z
        && center.z <= otherMax.z) {
      return new CollisionResult(true, new Vector3f(0, 1, 0), a.radius);
    }

    // Check if sphere intersects box
    float x = Math.max(otherMin.x, Math.min(center.x, otherMax.x));
    float y = Math.max(otherMin.y, Math.min(center.y, otherMax.y));
    float z = Math.max(otherMin.z, Math.min(center.z, otherMax.z));

    float distance =
        (float)
            Math.sqrt(
                (x - center.x) * (x - center.x)
                    + (y - center.y) * (y - center.y)
                    + (z - center.z) * (z - center.z));

    if (distance < a.radius) {
      Vector3f normal = new Vector3f(center).sub(x, y, z).normalize();
      return new CollisionResult(true, normal, a.radius - distance);
    } else {
      return CollisionResult.NO_COLLISION;
    }
  }

  private static CollisionResult collideSphere(SphereCollider a, Collider b) {
    if (!(b instanceof SphereCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with SphereColliders!");
    }
    float distance = other.worldPosition().distance(a.worldPosition());
    if(distance < a.radius + other.radius) {
      Vector3f normal = new Vector3f(other.worldPosition()).sub(a.worldPosition()).normalize();
      return new CollisionResult(true, normal, a.radius + other.radius - distance);
    } else {
      return CollisionResult.NO_COLLISION;
    }
  }

  public float radius() {
    return this.radius;
  }

  public SphereCollider radius(float radius) {
    this.radius = radius;
    return this;
  }

  @Override
  public Vector3f min() {
    return this.worldPosition().sub(this.radius, this.radius, this.radius);
  }

  @Override
  public Vector3f max() {
    return this.worldPosition().add(this.radius, this.radius, this.radius);
  }
}
