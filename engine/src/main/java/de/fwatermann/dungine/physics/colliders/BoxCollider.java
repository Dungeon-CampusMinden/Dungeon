package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.utils.IntPair;
import de.fwatermann.dungine.utils.functions.IFunction2P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BoxCollider extends CuboidCollider {

  private static final Map<
          Class<? extends Collider>, IFunction2P<CollisionResult, BoxCollider, Collider>>
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
  }

  private final Quaternionf rotation = new Quaternionf();

  public BoxCollider(RigidBodyComponent rbc, Vector3f offset, Vector3f size, Quaternionf rotation) {
    super(rbc, offset, size, vertices, edges);
    this.rotation.set(rotation);
  }

  public BoxCollider(RigidBodyComponent rbc, Vector3f size) {
    this(rbc, new Vector3f(), size, new Quaternionf());
  }

  public static void registerCollisionFunction(
    Class<? extends Collider> other,
    IFunction2P<CollisionResult, BoxCollider, Collider> function) {
    collisionFunctions.put(other, function);
  }

  public static void unregisterCollisionFunction(Class<? extends Collider> other) {
    collisionFunctions.remove(other);
  }

  @Override
  public CollisionResult collide(Collider other) {
    Optional<IFunction2P<CollisionResult, BoxCollider, Collider>> funcOpt =
        Optional.ofNullable(collisionFunctions.get(other.getClass()));
    if (funcOpt.isEmpty()) {
      throw new UnsupportedOperationException(
          String.format(
              "%s does not support collision with collider of type \"%s\"!",
              this.getClass().getName(), other.getClass().getName()));
    }
    return funcOpt.get().run(this, other);
  }

  @Override
  public Vector3f min() {
    Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    for (Vector3f vertex : this.verticesTransformed(true)) {
      min.min(vertex);
    }
    return min;
  }

  @Override
  public Vector3f max() {
    Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    for (Vector3f vertex : this.verticesTransformed(true)) {
      max.max(vertex);
    }
    return max;
  }

  @Override
  public Vector3f[] verticesTransformed(boolean worldSpace) {
    Vector3f[] result = new Vector3f[this.vertices().length];
    for (int i = 0; i < this.vertices().length; i++) {
      Vector3f vertex = this.rotation(true).transform(this.vertices()[i].mul(this.size, new Vector3f()));
      result[i] = vertex;
      if (worldSpace) {
        result[i].add(this.worldPosition());
      }
    }
    return result;
  }

  @Override
  public Vector3f worldPosition() {
    Vector3f offset = this.rbc.entity().rotation().transform(this.offset, new Vector3f());
    return offset.add(this.rbc.entity().position());
  }

  public Quaternionf rotation(boolean worldSpace) {
    if(worldSpace) {
      return this.rotation.premul(this.rbc.entity().rotation(), new Quaternionf());
    } else {
      return this.rotation;
    }
  }

  public BoxCollider rotation(Quaternionf rotation) {
    this.rotation.set(rotation);
    return this;
  }

}
