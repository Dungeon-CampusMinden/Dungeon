package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.CuboidCollider;
import de.fwatermann.dungine.utils.FloatPair;
import de.fwatermann.dungine.utils.IntPair;
import de.fwatermann.dungine.utils.Pair;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/** This class provides methods to check for collisions using the Separating Axis Theorem (SAT). */
public class SATCheck {

  private static final Logger LOGGER = LogManager.getLogger(SATCheck.class);

  /**
   * Get the axes for a pair of edge sets.
   *
   * @param edgesA The first set of edges.
   * @param edgesB The second set of edges.
   * @return A list of axes.
   */
  private static List<Vector3f> getAxes(Vector3f[] edgesA, Vector3f[] edgesB) {
    List<Vector3f> axes = new ArrayList<>();
    for (Vector3f edgeA : edgesA) {
      for (Vector3f edgeB : edgesB) {
        Vector3f axis = new Vector3f();
        edgeA.cross(edgeB, axis);
        axes.add(axis);
      }
    }
    return axes;
  }

  /**
   * Project vertices onto an axis.
   *
   * @param vertices The vertices to project.
   * @param axis The axis to project onto.
   * @return A pair containing the minimum and maximum projection values.
   */
  private static FloatPair project(Vector3f[] vertices, Vector3f axis) {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (Vector3f vertex : vertices) {
      float projection = vertex.dot(axis);
      min = Math.min(min, projection);
      max = Math.max(max, projection);
    }
    return new FloatPair(min, max);
  }

  /**
   * Check for collision between two cuboid colliders.
   *
   * @param boxA The first cuboid collider.
   * @param boxB The second cuboid collider.
   * @return A pair containing the overlap distance and the collision normal if a collision
   *     occurred, null otherwise.
   */
  @Nullable
  public static Pair<Float, Vector3f> checkCollision(CuboidCollider boxA, CuboidCollider boxB) {

    Vector3f[] verticesA = boxA.verticesTransformed(true);
    Vector3f[] verticesB = boxB.verticesTransformed(true);
    IntPair[] edgeIndicesA = boxA.edges();
    IntPair[] edgeIndicesB = boxB.edges();
    Vector3f[] edgesA = new Vector3f[edgeIndicesA.length];
    Vector3f[] edgesB = new Vector3f[edgeIndicesB.length];

    // Calculate edges
    for (int i = 0; i < edgeIndicesA.length; i++) {
      edgesA[i] =
          verticesA[edgeIndicesA[i].a()].sub(verticesA[edgeIndicesA[i].b()], new Vector3f());
    }
    for (int i = 0; i < edgeIndicesB.length; i++) {
      edgesB[i] =
          verticesB[edgeIndicesB[i].a()].sub(verticesB[edgeIndicesB[i].b()], new Vector3f());
    }

    List<Vector3f> axes = getAxes(edgesA, edgesB);

    float minOverlap = Float.MAX_VALUE;
    Vector3f minAxis = new Vector3f();
    for (Vector3f axis : axes) {
      FloatPair projectionA = project(verticesA, axis);
      FloatPair projectionB = project(verticesB, axis);
      float overlap =
          Math.min(projectionA.b() - projectionB.a(), projectionB.b() - projectionA.a());
      if (overlap < 0) {
        return null;
      }
      if (overlap < minOverlap) {
        minOverlap = overlap;
        minAxis = axis;
      }
    }

    return new Pair<>(minOverlap, minAxis);
  }
}
