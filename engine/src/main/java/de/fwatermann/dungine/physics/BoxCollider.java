package de.fwatermann.dungine.physics;

import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Vector3f;

public class BoxCollider extends Collider {

  private static final Map<Class<? extends Collider>, IFunction2P<Boolean, BoxCollider, Collider>>
      collisionFunctions = new HashMap<>();

  static {
    collisionFunctions.put(BoxCollider.class, BoxCollider::collideBox);
    collisionFunctions.put(SphereCollider.class, BoxCollider::collideSphere);
    collisionFunctions.put(CapsuleCollider.class, BoxCollider::collideCapsule);
    collisionFunctions.put(PaneCollider.class, BoxCollider::collidePlane);
  }

  public static void registerCollisionFunction(
      Class<? extends Collider> other, IFunction2P<Boolean, BoxCollider, Collider> function) {
    collisionFunctions.put(other, function);
  }

  public static void unregisterCollisionFunction(Class<? extends Collider> other) {
    collisionFunctions.remove(other);
  }

  private Vector3f min;
  private Vector3f max;

  @Override
  public boolean collide(Collider other) {
    Optional<IFunction2P<Boolean, BoxCollider, Collider>> func =
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

  private static boolean collideBox(BoxCollider a, Collider b) {
    if (!(b instanceof BoxCollider other)) {
      throw new IllegalStateException(
          "This function can only be used to check for collisions with other BoxColliders!");
    }
    if (a.max.x < other.min.x || a.min.x > other.max.x) {
      return false;
    }
    if (a.max.y < other.min.y || a.min.y > other.max.y) {
      return false;
    }
    if (a.max.z < other.min.z || a.min.z > other.max.z) {
      return false;
    }
    return true;
  }

  private static boolean collideSphere(BoxCollider a, Collider b) {
    if(!(b instanceof SphereCollider other)) {
      throw new IllegalStateException("This function can only be used to check for collisions with SphereColliders!");
    }
    return other.collide(a); // BoxCollider/SphereCollider collision is implemented in SphereCollider.
  }

  private static boolean collideCapsule(BoxCollider a, Collider b) {
    if(!(b instanceof CapsuleCollider other)) {
      throw new IllegalStateException("This function can only be used to check for collisions with CapsuleColliders!");
    }
    return other.collide(a); // BoxCollider/CapsuleCollider collision is implemented in CapsuleCollider.
  }

  private static boolean collidePlane(BoxCollider a, Collider b) {
    if(!(b instanceof PaneCollider other)) {
      throw new IllegalStateException("This function can only be used to check for collisions with PlaneColliders!");
    }
    return other.collide(a); // BoxCollider/PaneCollider collision is implemented in PlaneCollider.
  }

  public Vector3f min() {
    return this.min;
  }

  public Vector3f max() {
    return this.max;
  }

}
