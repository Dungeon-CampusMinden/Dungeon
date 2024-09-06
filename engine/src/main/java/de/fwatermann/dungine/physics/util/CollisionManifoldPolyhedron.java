package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.physics.colliders.Face;
import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.utils.pair.IntPair;
import de.fwatermann.dungine.utils.pair.Vector3fPair;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public class CollisionManifoldPolyhedron {

  private static final Logger LOGGER = LogManager.getLogger(CollisionManifoldPolyhedron.class);

  /**
   * Calculates the contact points between the two colliders.
   *
   * <p>1. Identify significant faces. 1.1 Select vertex that is furthest along the normal on each
   * collider. 1.2 Select Faces that include the vertices on each collider and are as close as
   * possible perpendicular to the normal. 2. Incident/Reference Face calculation (Nearest to
   * normal) 3. Clipping 3.1 Clip the incident face against the adjacent faces of the reference
   * face. 3.2 Clip the resulting polygon against the reference face.
   *
   * @return The contact points between the two colliders.
   */
  static Set<Vector3f> calculateContactPoints(
      Collider c1, Collider c2, Vector3f normal, float depth) {

    if (!(c1 instanceof PolyhedronCollider<?> pc1) || !(c2 instanceof PolyhedronCollider<?> pc2)) {
      LOGGER.error(
          "CollisionManifold::calculateContactPoints called with non-polyhedron colliders");
      return Set.of();
    }

    Vector3f normalNeg = normal.negate(new Vector3f());
    Face refFace = getBestFace(pc1, getFurthestVertex(pc1, normal), normal);
    Face incFace = getBestFace(pc2, getFurthestVertex(pc2, normalNeg), normalNeg);

    if (refFace.normal().angle(normal) < incFace.normal().angle(normal)) {
      Face temp = refFace;
      refFace = incFace;
      incFace = temp;
    }

    Vector3fPair[] incidentEdges = convertEdges(incFace);
    Vector3fPair[] referenceEdges = convertEdges(refFace);

    Set<Vector3f> points = new HashSet<>();

    for (int i = 0; i < referenceEdges.length; i++) {
      Vector3fPair vertices = referenceEdges[i];

      Vector3f clipNormal =
          refFace
              .normal()
              .cross(vertices.b().sub(vertices.a(), new Vector3f()), new Vector3f())
              .normalize();
      Vector3f clipOrigin = new Vector3f(vertices.a());

      for (int j = 0; j < incidentEdges.length; j++) {
        Vector3f a = incidentEdges[j].a();
        Vector3f b = incidentEdges[j].b();
        Vector3f edge = b.sub(a, new Vector3f()).normalize();
        if (clipNormal.dot(edge) == 0) continue; //Skip parallel edges
        clipVertex(a, edge, clipOrigin, clipNormal);
        clipVertex(b, edge, clipOrigin, clipNormal);
      }
    }
    Vector3f refOrigin = refFace.vertex(0);
    Vector3f refNormal = refFace.normal();
    for (Vector3fPair edge : incidentEdges) {
      Vector3f a = edge.a();
      Vector3f b = edge.b();
      if (isInside(a, refOrigin, refNormal)) {
        points.add(a);
      }
      if (isInside(b, refOrigin, refNormal)) {
        points.add(b);
      }
    }
    return points;
  }

  private static Vector3fPair[] convertEdges(Face face) {
    Vector3fPair[] edges = new Vector3fPair[face.edges().length];
    Vector3f[] vertices = face.vertices();
    for (int i = 0; i < face.edges().length; i++) {
      IntPair edge = face.edges()[i];
      Vector3f a = vertices[edge.a()];
      Vector3f b = vertices[edge.b()];
      edges[i] = new Vector3fPair(a, b);
    }
    return edges;
  }

  private static Vector3f getFurthestVertex(PolyhedronCollider<?> pc, Vector3f direction) {
    Vector3f[] vertices = pc.vertices();
    Vector3f furthestVertex = vertices[0];
    float maxDistance = furthestVertex.dot(direction);
    for (int i = 1; i < vertices.length; i++) {
      float distance = vertices[i].dot(direction);
      if (distance > maxDistance) {
        maxDistance = distance;
        furthestVertex = vertices[i];
      }
    }
    return furthestVertex;
  }

  private static Face getBestFace(PolyhedronCollider<?> pc, Vector3f vertex, Vector3f normal) {
    Face[] faces = pc.faces();
    Face bestFace = faces[0];
    float minAngle = Float.MAX_VALUE;
    for (int i = 0; i < pc.faces().length; i++) {
      Face face = faces[i];
      if (face.hasVertex(vertex)) {
        float angle = normal.angle(faces[i].normal());
        if (angle < minAngle) {
          minAngle = angle;
          bestFace = face;
        }
      }
    }
    return bestFace;
  }

  private static boolean isInside(Vector3f point, Vector3f origin, Vector3f normal) {
    return normal.dot(point.sub(origin, new Vector3f())) <= 0;
  }

  private static void clipVertex(Vector3f vertex, Vector3f edge, Vector3f origin, Vector3f normal) {
    if(isInside(vertex, origin, normal)) return;
    float r = normal.dot(origin.sub(vertex, new Vector3f())) / normal.dot(edge);
    vertex.add(edge.mul(r, new Vector3f()));
  }

}
