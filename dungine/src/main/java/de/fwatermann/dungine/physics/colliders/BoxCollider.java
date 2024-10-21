package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.pair.IntPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The `BoxCollider` class represents a box-shaped collider in the physics engine. It extends the
 * `PolyhedronCollider` class and provides methods to define the vertices, edges, and faces of the
 * box.
 */
public class BoxCollider extends PolyhedronCollider<BoxCollider> {

  private static final Logger LOGGER = LogManager.getLogger(BoxCollider.class);

  /** The vertices of the box. */
  protected static final Vector3f[] VERTICES = {
    new Vector3f(0.0f, 0.0f, 0.0f),
    new Vector3f(1.0f, 0.0f, 0.0f),
    new Vector3f(1.0f, 0.0f, 1.0f),
    new Vector3f(0.0f, 0.0f, 1.0f),
    new Vector3f(0.0f, 1.0f, 0.0f),
    new Vector3f(1.0f, 1.0f, 0.0f),
    new Vector3f(1.0f, 1.0f, 1.0f),
    new Vector3f(0.0f, 1.0f, 1.0f),
  };

  /** The edges of the box. */
  protected static final IntPair[] EDGES = {
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

  /**
   * Constructs a `BoxCollider` with the specified entity, offset, size, and rotation.
   *
   * @param entity the entity to which this collider is attached
   * @param offset the offset of the collider relative to the entity
   * @param size the size of the collider
   * @param rotation the rotation of the collider
   */
  public BoxCollider(Entity entity, Vector3f offset, Vector3f size, Quaternionf rotation) {
    super(entity, VERTICES, EDGES, new Face[6], offset, size, rotation);
    this.faces[0] =
        new Face(
            this,
            new int[] {0, 1, 2, 3},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(0.0f, -1.0f, 0.0f));
    this.faces[1] =
        new Face(
            this,
            new int[] {4, 5, 6, 7},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(0.0f, 1.0f, 0.0f));
    this.faces[2] =
        new Face(
            this,
            new int[] {0, 1, 5, 4},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(0.0f, 0.0f, -1.0f));
    this.faces[3] =
        new Face(
            this,
            new int[] {1, 2, 6, 5},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(1.0f, 0.0f, 0.0f));
    this.faces[4] =
        new Face(
            this,
            new int[] {2, 3, 7, 6},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(0.0f, 0.0f, 1.0f));
    this.faces[5] =
        new Face(
            this,
            new int[] {3, 0, 4, 7},
            new IntPair[] {IntPair.of(0, 1), IntPair.of(1, 2), IntPair.of(2, 3), IntPair.of(3, 0)},
            new Vector3f(-1.0f, 0.0f, 0.0f));
  }

  /**
   * Constructs a `BoxCollider` with the specified entity, offset, and size.
   *
   * @param entity the entity to which this collider is attached
   * @param offset the offset of the collider relative to the entity
   * @param size the size of the collider
   */
  public BoxCollider(Entity entity, Vector3f offset, Vector3f size) {
    this(entity, offset, size, new Quaternionf());
  }

  /**
   * Constructs a `BoxCollider` with the specified entity and offset.
   *
   * @param entity the entity to which this collider is attached
   * @param offset the offset of the collider relative to the entity
   */
  public BoxCollider(Entity entity, Vector3f offset) {
    this(entity, offset, new Vector3f(1.0f), new Quaternionf());
  }

  /**
   * Constructs a `BoxCollider` with the specified entity.
   *
   * @param entity the entity to which this collider is attached
   */
  public BoxCollider(Entity entity) {
    this(entity, new Vector3f(), new Vector3f(1.0f), new Quaternionf());
  }
}
