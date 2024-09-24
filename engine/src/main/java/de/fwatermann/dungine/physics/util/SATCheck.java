package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.utils.annotations.Nullable;
import de.fwatermann.dungine.utils.pair.FloatPair;
import de.fwatermann.dungine.utils.pair.IntPair;
import de.fwatermann.dungine.utils.pair.Pair;
import java.util.HashSet;
import java.util.Set;
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
  private static void getAxes(Vector3f[] edgesA, Vector3f[] edgesB, Set<Vector3f> dest) {
    for (Vector3f edgeA : edgesA) {
      for (Vector3f edgeB : edgesB) {
        Vector3f a = edgeA.normalize(new Vector3f());
        Vector3f b = edgeB.normalize(new Vector3f());
        if(Math.abs(a.dot(b)) >= 0.99999f) {
          continue;
        }
        Vector3f axis = new Vector3f();
        edgeA.cross(edgeB, axis).normalize();
        dest.add(axis);
      }
    }
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
   * @param colliderA The first cuboid collider.
   * @param colliderB The second cuboid collider.
   * @return A pair containing the overlap distance and the collision normal if a collision
   *     occurred, null otherwise.
   */
  @Nullable
  public static Pair<Float, Vector3f> checkCollision(PolyhedronCollider<?> colliderA, PolyhedronCollider<?> colliderB) {

    Vector3f[] verticesA = colliderA.vertices();
    Vector3f[] verticesB = colliderB.vertices();
    IntPair[] edgeIndicesA = colliderA.edges();
    IntPair[] edgeIndicesB = colliderB.edges();
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

    Set<Vector3f> axes = new HashSet<>();
    getAxes(edgesA, edgesB, axes);
    getAxes(edgesB, edgesA, axes);
    getAxes(edgesA, edgesA, axes);
    getAxes(edgesB, edgesB, axes);

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
