package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.physics.util.SATCheck;
import de.fwatermann.dungine.utils.IntPair;
import de.fwatermann.dungine.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public abstract class CuboidCollider extends Collider {

  private static final Logger LOGGER = LogManager.getLogger(CuboidCollider.class);

  protected Vector3f[] vertices;
  protected IntPair[] edges;
  protected final Vector3f size;

  public CuboidCollider(RigidBodyComponent rbc, Vector3f offset, Vector3f size, Vector3f[] vertices, IntPair[] edges) {
    super(rbc, offset);
    this.vertices = vertices;
    this.edges = edges;
    this.size = size;
  }

  public CuboidCollider(RigidBodyComponent rbc, Vector3f[] vertices, IntPair[] edges) {
    this(rbc, new Vector3f(), new Vector3f(1.0f), vertices, edges);
  }

  protected static CollisionResult collideCuboid(CuboidCollider a, Collider b) {
    if (!(b instanceof CuboidCollider other)) {
      throw new IllegalStateException(
        String.format(
          "Cannot collide BoxCollider with collider of type \"%s\"!", b.getClass().getName()));
    }
    //Check AAB collision using min/max
    Vector3f aMin = a.min();
    Vector3f aMax = a.max();
    Vector3f bMin = other.min();
    Vector3f bMax = other.max();

    if (aMax.x < bMin.x || aMin.x > bMax.x) return CollisionResult.NO_COLLISION;
    if (aMax.y < bMin.y || aMin.y > bMax.y) return CollisionResult.NO_COLLISION;
    if (aMax.z < bMin.z || aMin.z > bMax.z) return CollisionResult.NO_COLLISION;

    Pair<Float, Vector3f> satResult = SATCheck.checkCollision(a, other);
    if (satResult == null) return CollisionResult.NO_COLLISION;
    Collision collision = new Collision(satResult.b(), satResult.a());
    return new CollisionResult(true, collision);
  }

  public Vector3f[] vertices() {
    return this.vertices;
  }

  /**
   * Get the transformed vertices of the collider.
   * @param worldSpace Whether to return the vertices in world space.
   * @return The vertices of the collider.
   */
  public abstract Vector3f[] verticesTransformed(boolean worldSpace);

  public IntPair[] edges() {
    return this.edges;
  }

  public Vector3f size() {
    return this.size;
  }

  public CuboidCollider size(Vector3f size) {
    this.size.set(size);
    return this;
  }
}
