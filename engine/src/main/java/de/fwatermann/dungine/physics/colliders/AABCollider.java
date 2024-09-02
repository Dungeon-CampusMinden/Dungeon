package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.utils.IntPair;
import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Vector3f;

/**
 * A collider that represents an axis-aligned bounding box. This collider is used to check for
 * collisions with other colliders.
 */
public class AABCollider extends CuboidCollider {

  private static final Map<
          Class<? extends Collider>, IFunction2P<CollisionResult, AABCollider, Collider>>
      collisionFunctions = new HashMap<>();

  private static final Vector3f[] vertices = {
    new Vector3f(0.0f, 0.0f, 0.0f),
    new Vector3f(1.0f, 0.0f, 0.0f),
    new Vector3f(1.0f, 0.0f, 1.0f),
    new Vector3f(0.0f, 0.0f, 1.0f),
    new Vector3f(0.0f, 1.0f, 0.0f),
    new Vector3f(1.0f, 1.0f, 0.0f),
    new Vector3f(1.0f, 1.0f, 1.0f),
    new Vector3f(0.0f, 1.0f, 1.0f),
  };

  private static final IntPair[] edges = {
    IntPair.of(0, 1),
    IntPair.of(1, 2),
    IntPair.of(2, 3),
    IntPair.of(3, 0),
    IntPair.of(4, 5),
    IntPair.of(5, 6),
    IntPair.of(6, 7),
    IntPair.of(7, 4),
    IntPair.of(0, 4),
    IntPair.of(1, 5),
    IntPair.of(2, 6),
    IntPair.of(3, 7)
  };

  static {
    collisionFunctions.put(BoxCollider.class, CuboidCollider::collideCuboid);
    collisionFunctions.put(AABCollider.class, CuboidCollider::collideCuboid);
    // collisionFunctions.put(SphereCollider.class, AABCollider::collideSphere);
  }

  public AABCollider(RigidBodyComponent rbc, Vector3f offset, Vector3f size) {
    super(rbc, offset, size, vertices, edges);
  }

  public static void registerCollisionFunction(
      Class<? extends Collider> other,
      IFunction2P<CollisionResult, AABCollider, Collider> function) {
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

  @Override
  public Vector3f min() {
    return this.worldPosition();
  }

  @Override
  public Vector3f max() {
    return this.worldPosition().add(this.size);
  }

  @Override
  public Vector3f[] verticesTransformed(boolean worldSpace) {
    Vector3f[] result = new Vector3f[this.vertices().length];
    for(int i = 0; i < this.vertices().length; i++) {
      result[i] = this.vertices()[i].mul(this.size, new Vector3f());
      if(worldSpace) {
        result[i].add(this.worldPosition());
      }
    }
    return result;
  }
}
