package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.physics.colliders.Face;
import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugSystem;
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

    Vector3f normalNeg = normal.negate(new Vector3f());
    Vector3f furthestVertex1 = getFurthestVertex(pc1, normal);
    Vector3f furthestVertex2 = getFurthestVertex(pc2, normalNeg);

    PhysicsDebugSystem.manifoldLines.addLine(pc1.vertices()[0], pc1.vertices()[0].add(normal, new Vector3f()), 0x0080FFFF);

    PhysicsDebugSystem.contactPointsDebug.addPoint(furthestVertex1, 0xFF0000FF);
    PhysicsDebugSystem.contactPointsDebug.addPoint(furthestVertex2, 0x00FF00FF);

    Face refFace = getBestFace(pc1, furthestVertex1, normalNeg);
    Face incFace = getBestFace(pc2, furthestVertex2, normal);

    /*if(Math.abs(refFace.normal().dot(normal)) < Math.abs(incFace.normal().dot(normal))) {
      Face temp = refFace;
      refFace = incFace;
      incFace = temp;
    }*/

    Vector3fPair[] incidentEdges = convertEdges(incFace);
    Vector3fPair[] referenceEdges = convertEdges(refFace);

    for(int i = 0; i < referenceEdges.length; i++) {
      Vector3fPair vertices = referenceEdges[i];
      Vector3f a = vertices.a();
      Vector3f b = vertices.b();
      Vector3f edge = b.sub(a, new Vector3f()).normalize();
      Vector3f aN = edge.cross(refFace.normal(), new Vector3f());
      PhysicsDebugSystem.contactPointsDebug.addPoint(a);
      PhysicsDebugSystem.contactPointsDebug.addPoint(b);
      PhysicsDebugSystem.manifoldLines.addLine(a, a.add(aN, new Vector3f()));
      PhysicsDebugSystem.manifoldLines.addLine(a, a.add(edge, new Vector3f()), 0x8080FFFF);
      //clipFace(incidentEdges, aN, a);
    }
    //clipFace(incidentEdges, refFace.normal(), refFace.vertex(0));

    Set<Vector3f> result = new HashSet<>();
    for(Vector3fPair pair : incidentEdges) {
      result.add(pair.a());
      result.add(pair.b());
    }
    return result;
  }

  private static Vector3fPair[] convertEdges(Face face) {
    Vector3fPair[] edges = new Vector3fPair[face.edges().length];
    for(int i = 0; i < face.edges().length; i++) {
      IntPair edge = face.edges()[i];
      Vector3f a = new Vector3f(face.parent().vertices()[edge.a()]);
      Vector3f b = new Vector3f(face.parent().vertices()[edge.b()]);
      edges[i] = new Vector3fPair(a, b);
    }
    return edges;
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
    for(int i = 0; i < pc.faces().length; i++) {
      if(bestFace.hasVertex(vertex)) {
        float angle = 1.0f - Math.abs(normal.normalize().dot(faces[i].normal().normalize()));
        if(angle < minAngle) {
          minAngle = angle;
          bestFace = faces[i];
        }
      }
    }
    return bestFace;
  }

  private static void clipFace(Vector3fPair[] edges, Vector3f faceNormal, Vector3f faceOrigin) {
    for(Vector3fPair pair : edges) {
      Vector3f v1 = pair.a();
      Vector3f v2 = pair.b();
      Vector3f edge = v2.sub(v1, new Vector3f()).normalize();
      if(!isInside(v1, faceOrigin, faceNormal)) {
        clipVertex(v1, edge, faceNormal, faceOrigin);
      }
      if(!isInside(v2, faceOrigin, faceNormal)) {
        clipVertex(v2, edge, faceNormal, faceOrigin);
      }
    }
  }


  private static boolean isInside(Vector3f point, Vector3f origin, Vector3f normal) {
    return normal.dot(point.sub(origin, new Vector3f())) <= 0;
  }

  private static void clipVertex(Vector3f vertex, Vector3f edge, Vector3f normal, Vector3f origin) {
    //Gerade g = vertex + r * edge
    //Ebene  E = normal * (x - origin) = 0
    //Eingesetzt in E: normal * (vertex + r * edge - origin) = 0
    //r = (normal * (origin - vertex)) / (normal * edge)
    if(normal.dot(edge) == 0) return;
    float r = normal.dot(origin.sub(vertex, new Vector3f())) / normal.dot(edge);
    //float r = (normal.dot(origin) - normal.dot(vertex)) / normal.dot(edge.normalize(new Vector3f()));
    edge.normalize(r, vertex).add(vertex);
  }


}
