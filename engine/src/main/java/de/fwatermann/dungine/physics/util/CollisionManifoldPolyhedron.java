package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.physics.colliders.Face;
import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.utils.IntPair;
import java.util.Arrays;
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
   * 1. Identify significant faces.
   * 1.1 Select vertex that is furthest along the normal on each
   * collider.
   * 1.2 Select Faces that include the vertices on each collider and are as close as
   * possible perpendicular to the normal.
   * 2. Incident/Reference Face calculation (Nearest to normal)
   * 3. Clipping
   * 3.1 Clip the incident face against the adjacent faces of the reference face.
   * 3.2 Clip the resulting polygon against the reference face.
   *
   * @return The contact points between the two colliders.
   */
  static Set<Vector3f> calculateContactPoints(
    Collider c1, Collider c2, Vector3f normal, float depth) {

    if(!(c1 instanceof PolyhedronCollider<?> pc1) || !(c2 instanceof PolyhedronCollider<?> pc2)) {
      LOGGER.error("CollisionManifold::calculateContactPoints called with non-polyhedron colliders");
      return Set.of();
    }

    Vector3f furthestVertex1 = getFurthestVertex(pc1, normal.negate(new Vector3f()));
    Vector3f furthestVertex2 = getFurthestVertex(pc2, normal);

    Face referenceFace = getBestFace(pc1, furthestVertex1, normal);
    Face incidentFace = getBestFace(pc2, furthestVertex2, normal.negate(new Vector3f()));


    if(Math.abs(referenceFace.normal().dot(incidentFace.normal())) < Math.abs(incidentFace.normal().dot(normal))) {
      Face temp = referenceFace;
      referenceFace = incidentFace;
      incidentFace = temp;
    }

    IntPair[] referenceEdges = referenceFace.edges();
    IntPair[] incidentEdges = incidentFace.edges();
    Vector3f[] referenceVertices = new Vector3f[referenceEdges.length * 2];
    Vector3f[] incidentVertices = new Vector3f[incidentEdges.length * 2];

    for(int i = 0; i < referenceEdges.length; i++) {
      Vector3f a = pc1.vertices()[referenceEdges[i].a()];
      Vector3f b = pc1.vertices()[referenceEdges[i].b()];
      referenceVertices[i * 2] = a;
      referenceVertices[i * 2 + 1] = b;
    }
    for(int i = 0; i < incidentEdges.length; i++) {
      Vector3f a = pc2.vertices()[incidentEdges[i].a()];
      Vector3f b = pc2.vertices()[incidentEdges[i].b()];
      incidentVertices[i * 2] = a;
      incidentVertices[i * 2 + 1] = b;
    }

    for(int i = 0; i < referenceEdges.length; i++) {
      Vector3f a = referenceFace.parent().vertices()[referenceEdges[i].a()];
      Vector3f b = referenceFace.parent().vertices()[referenceEdges[i].b()];
      Vector3f edge = b.sub(a).normalize();
      Vector3f aN = edge.cross(referenceFace.normal(), new Vector3f());
      clipFace(incidentVertices, aN, a);
    }
    clipFace(incidentVertices, referenceFace.normal(), referenceVertices[0]);

    return new HashSet<>(Arrays.asList(incidentVertices));
  }

  private static Vector3f getFurthestVertex(PolyhedronCollider<?> pc, Vector3f direction) {
    Vector3f[] vertices = pc.vertices();
    Vector3f furthestVertex = vertices[0];
    float maxDistance = furthestVertex.dot(direction);
    for(int i = 1; i < vertices.length; i++) {
      float distance = vertices[i].dot(direction);
      if(distance > maxDistance) {
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
    for(int i = 1; i < pc.faces().length; i++) {
      if(bestFace.hasVertex(vertex)) {
        float angle = Math.abs(normal.dot(faces[i].normal()));
        if(angle < minAngle) {
          minAngle = angle;
          bestFace = faces[i];
        }
      }
    }
    return bestFace;
  }

  private static void clipFace(Vector3f[] vertices, Vector3f faceNormal, Vector3f faceOrigin) {
    for(int i = 0; i < vertices.length; i += 2) {
      Vector3f v1 = vertices[i];
      Vector3f v2 = vertices[i + 1];
      Vector3f edge = v2.sub(v1, new Vector3f());
      if(!isInside(v1, faceOrigin, faceNormal)) {
        Vector3f intersection = getIntersection(v1, edge, faceNormal, faceOrigin);
        if(intersection != null) {
          vertices[i] = intersection;
        }
      }
      if(!isInside(v2, faceOrigin, faceNormal)) {
        Vector3f intersection = getIntersection(v2, edge, faceNormal, faceOrigin);
        if (intersection != null) {
          vertices[i + 1] = intersection;
        }
      }
    }
  }

  private static boolean isInside(Vector3f point, Vector3f origin, Vector3f normal) {
    return normal.dot(point.sub(origin, new Vector3f())) <= 0;
  }

  private static Vector3f getIntersection(Vector3f vertex, Vector3f edge, Vector3f normal, Vector3f origin) {
    //Gerade g = vertex + r * edge
    //Ebene  E = normal * (x - origin) = 0
    //Eingesetzt in E: normal * (vertex + r * edge - origin) = 0
    //r = (normal * (origin - vertex)) / (normal * edge)
    if(normal.dot(edge) == 0) return null;
    // float r = normal.dot(origin.sub(vertex, new Vector3f())) / normal.dot(edge);
    float r =
        (normal.dot(origin) - normal.dot(vertex)) / normal.dot(edge.normalize(new Vector3f()));
    return edge.normalize(r, new Vector3f()).add(vertex);
  }


}
