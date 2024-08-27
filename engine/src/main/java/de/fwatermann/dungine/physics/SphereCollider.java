package de.fwatermann.dungine.physics;

import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Math;
import org.joml.Vector3f;

public class SphereCollider extends Collider {

  private static final Map<
          Class<? extends Collider>, IFunction2P<Boolean, SphereCollider, Collider>>
      collisionFunctions = new HashMap<>();

  static {
    collisionFunctions.put(BoxCollider.class, SphereCollider::collideBox);
    collisionFunctions.put(SphereCollider.class, SphereCollider::collideSphere);
  }

  public static void registerCollisionFunction(
      Class<? extends Collider> other, IFunction2P<Boolean, SphereCollider, Collider> function) {
    collisionFunctions.put(other, function);
  }

  public static void unregisterCollisionFunction(Class<? extends Collider> other) {
    collisionFunctions.remove(other);
  }

  private Vector3f center;
  private float radius;

  @Override
  public boolean collide(Collider other) {
    Optional<IFunction2P<Boolean, SphereCollider, Collider>> func =
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

  private static boolean collideBox(SphereCollider a, Collider b) {
    if (!(b instanceof BoxCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with other BoxColliders!");
    }
    Vector3f sMin = new Vector3f(a.center).sub(a.radius, a.radius, a.radius);
    Vector3f sMax = new Vector3f(a.center).add(a.radius, a.radius, a.radius);

    // AABB check for approximation
    if (sMax.x < other.min().x || sMin.x > other.max().x) {
      return false;
    }
    if (sMax.y < other.min().y || sMin.y > other.max().y) {
      return false;
    }
    if (sMax.z < other.min().z || sMin.z > other.max().z) {
      return false;
    }

    // Check if sphere is inside box
    if (a.center.x >= other.min().x
        && a.center.x <= other.max().x
        && a.center.y >= other.min().y
        && a.center.y <= other.max().y
        && a.center.z >= other.min().z
        && a.center.z <= other.max().z) {
      return true;
    }

    // Check if sphere intersects box
    float x = Math.max(other.min().x, Math.min(a.center.x, other.max().x));
    float y = Math.max(other.min().y, Math.min(a.center.y, other.max().y));
    float z = Math.max(other.min().z, Math.min(a.center.z, other.max().z));

    float distance =
        (float)
            Math.sqrt(
                (x - a.center.x) * (x - a.center.x)
                    + (y - a.center.y) * (y - a.center.y)
                    + (z - a.center.z) * (z - a.center.z));
    return distance < a.radius;
  }

  private static boolean collideSphere(SphereCollider a, Collider b) {
    if (!(b instanceof SphereCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with SphereColliders!");
    }
    return other.center.distance(a.center) < a.radius + other.radius;
  }
}
