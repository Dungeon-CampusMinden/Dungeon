package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.pair.IntPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BoxCollider extends PolyhedronCollider<BoxCollider> {

  private static final Logger LOGGER = LogManager.getLogger(BoxCollider.class);

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

  public BoxCollider(Entity entity, Vector3f offset, Vector3f size, Quaternionf rotation) {
    super(entity, VERTICES, EDGES, new Face[6], offset, size, rotation);
    this.faces[0] =
        new Face(
            this,
            new int[] {0, 1, 2, 3},
            new IntPair[] {EDGES[0], EDGES[1], EDGES[2], EDGES[3]},
            new Vector3f(0.0f, -1.0f, 0.0f));
    this.faces[1] =
        new Face(
            this,
            new int[] {4, 5, 6, 7},
            new IntPair[] {EDGES[4], EDGES[5], EDGES[6], EDGES[7]},
            new Vector3f(0.0f, 1.0f, 0.0f));
    this.faces[2] =
        new Face(
            this,
            new int[] {0, 1, 5, 4},
            new IntPair[] {EDGES[0], EDGES[8], EDGES[9], EDGES[4]},
            new Vector3f(0.0f, 0.0f, -1.0f));
    this.faces[3] =
        new Face(
            this,
            new int[] {1, 2, 6, 5},
            new IntPair[] {EDGES[1], EDGES[10], EDGES[5], EDGES[9]},
            new Vector3f(1.0f, 0.0f, 0.0f));
    this.faces[4] =
        new Face(
            this,
            new int[] {2, 3, 7, 6},
            new IntPair[] {EDGES[2], EDGES[11], EDGES[6], EDGES[10]},
            new Vector3f(0.0f, 0.0f, 1.0f));
    this.faces[5] =
        new Face(
            this,
            new int[] {3, 0, 4, 7},
            new IntPair[] {EDGES[3], EDGES[8], EDGES[7], EDGES[11]},
            new Vector3f(-1.0f, 0.0f, 0.0f));
  }

  public BoxCollider(Entity entity, Vector3f offset, Vector3f size) {
    this(entity, offset, size, new Quaternionf());
  }

  public BoxCollider(Entity entity, Vector3f offset) {
    this(entity, offset, new Vector3f(1.0f), new Quaternionf());
  }

  public BoxCollider(Entity entity) {
    this(entity, new Vector3f(), new Vector3f(1.0f), new Quaternionf());
  }

}
