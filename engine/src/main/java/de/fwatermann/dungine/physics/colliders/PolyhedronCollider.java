package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.physics.util.CollisionManifold;
import de.fwatermann.dungine.physics.util.SATCheck;
import de.fwatermann.dungine.utils.IntPair;
import de.fwatermann.dungine.utils.Pair;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PolyhedronCollider<T extends PolyhedronCollider<?>> extends Collider {

  static {
    registerCollisionFunction(PolyhedronCollider.class, PolyhedronCollider.class, PolyhedronCollider::collidePolyhedrons);
    registerCollisionFunction(PolyhedronCollider.class, BoxCollider.class, PolyhedronCollider::collidePolyhedrons);
    registerCollisionFunction(PolyhedronCollider.class, AABCollider.class, PolyhedronCollider::collidePolyhedrons);
    registerCollisionFunction(BoxCollider.class, BoxCollider.class, PolyhedronCollider::collidePolyhedrons);
  }

  private final Vector3f[] initialVertices;
  private final Quaternionf rotation;
  private final Vector3f scaling;

  protected final Vector3f[] vertices;
  protected final IntPair[] edges;
  protected final Face[] faces;

  public PolyhedronCollider(
      Entity entity,
      Vector3f[] vertices,
      IntPair[] edges,
      Face[] faces,
      Vector3f offset,
      Vector3f scaling,
      Quaternionf rotation) {
    super(entity, offset);
    this.scaling = new Vector3f(scaling);
    this.rotation = new Quaternionf(rotation);
    this.edges = edges;
    this.faces = faces;
    this.vertices = new Vector3f[vertices.length];
    this.initialVertices = new Vector3f[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      this.initialVertices[i] = new Vector3f(vertices[i]);
      this.vertices[i] = new Vector3f(vertices[i]);
    }
  }

  /** Reset the vertices to the initial state. */
  protected void resetVertices() {
    for (int i = 0; i < this.initialVertices.length; i++) {
      this.vertices[i].set(this.initialVertices[i]);
    }
  }

  /**
   * Get the rotation of the collider. This does not include the rotation of the entity.
   *
   * @return The rotation of the collider.
   */
  public Quaternionf rotation() {
    return new Quaternionf(this.rotation);
  }

  /**
   * Set the rotation of the collider. This does not include the rotation of the entity.
   *
   * @param rotation
   * @return
   */
  public T rotation(Quaternionf rotation) {
    this.rotation.set(rotation);
    this.transformationChanged();
    return (T) this;
  }

  /**
   * Get the scaling of the collider.
   *
   * @return The scaling of the collider.
   */
  public Vector3f scaling() {
    return this.scaling;
  }

  /**
   * Set the scaling of the collider.
   *
   * @param scaling The new scaling of the collider.
   * @return This collider.
   */
  public T scaling(Vector3f scaling) {
    this.scaling.set(scaling);
    this.transformationChanged();
    return (T) this;
  }

  /**
   * Get the vertices of the collider. These vertices are transformed based on the position,
   * rotation and scaling of the collider and entity.
   *
   * @return The vertices of the collider.
   */
  public Vector3f[] vertices() {
    this.transformationChanged();
    return this.vertices;
  }

  /**
   * Get the edges of the collider.
   * @return The edges of the collider.
   */
  public IntPair[] edges() {
    return this.edges;
  }

  /**
   * Get the faces of the collider.
   * @return The faces of the collider.
   */
  public Face[] faces() {
    return this.faces;
  }

  @Override
  public Vector3f min() {
    Vector3f min = new Vector3f(Float.MAX_VALUE);
    Vector3f[] vertices = this.vertices();
    for (Vector3f vertex : vertices) {
      min.min(vertex);
    }
    return min;
  }

  @Override
  public Vector3f max() {
    Vector3f max = new Vector3f(-Float.MAX_VALUE);
    Vector3f[] vertices = this.vertices();
    for (Vector3f vertex : vertices) {
      max.max(vertex);
    }
    return max;
  }

  @Override
  protected void transformationChanged() {
    super.transformationChanged();
    Quaternionf rotation = this.rotation().premul(this.entity.rotation());
    for (int i = 0; i < this.vertices.length; i++) {
      this.vertices[i].set(this.initialVertices[i]);
      rotation.transform(this.vertices[i]).mul(this.scaling).add(this.worldPosition());
    }
  }

  //Default Collision handler

  private static CollisionResult collidePolyhedrons(Collider pA, Collider pB) {
    if (!(pA instanceof PolyhedronCollider<?> a)) {
      throw new IllegalStateException(
        String.format(
          "Cannot collide BoxCollider with collider of type \"%s\"!", pA.getClass().getName()));
    }
    if (!(pB instanceof PolyhedronCollider<?> b)) {
      throw new IllegalStateException(
        String.format(
          "Cannot collide BoxCollider with collider of type \"%s\"!", pB.getClass().getName()));
    }
    // Check AAB collision using min/max
    if (!aabCheck(a, b)) return CollisionResult.NO_COLLISION;

    // Check SAT collision
    Pair<Float, Vector3f> satResult = SATCheck.checkCollision(a, b);
    if (satResult == null) return CollisionResult.NO_COLLISION;
    Set<Vector3f> collisionPoints = CollisionManifold.calculateContactPoints(a, b, satResult.b(), satResult.a());
    Collision collision = new Collision(satResult.b(), satResult.a(), collisionPoints);
    return new CollisionResult(true, collision);
  }

  /**
   * Check if two cuboid colliders collide using an axis-aligned bounding box check.
   *
   * @param pA The first collider.
   * @param pB The second collider.
   * @return Whether the two colliders collide.
   */
  protected static boolean aabCheck(Collider pA, Collider pB) {
    Vector3f aMin = pA.min();
    Vector3f aMax = pA.max();
    Vector3f bMin = pB.min();
    Vector3f bMax = pB.max();
    if (aMax.x < bMin.x || aMin.x > bMax.x) return false;
    if (aMax.y < bMin.y || aMin.y > bMax.y) return false;
    if (aMax.z < bMin.z || aMin.z > bMax.z) return false;
    return true;
  }

}
