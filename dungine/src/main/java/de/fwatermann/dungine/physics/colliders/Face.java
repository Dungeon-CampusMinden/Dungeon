package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.utils.pair.IntPair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The `Face` class represents a face of a polyhedron collider in the physics engine. It holds
 * information about the parent collider, vertices, edges, and normal of the face.
 */
public class Face {

  private final PolyhedronCollider<?> parent;
  private final int[] vertices;
  private final Vector3f normal;
  private final IntPair[] edges;

  /**
   * Constructs a new `Face` with the specified parent collider, vertex indices, edges, and normal.
   *
   * @param parent the parent polyhedron collider
   * @param vertexIndices the indices of the vertices that make up the face
   * @param edges the edges of the face
   * @param normal the normal vector of the face
   */
  public Face(PolyhedronCollider<?> parent, int[] vertexIndices, IntPair[] edges, Vector3f normal) {
    this.parent = parent;
    this.vertices = vertexIndices;
    this.edges = edges;
    this.normal = normal;
  }

  /**
   * Calculates and returns the normal vector of the specified edge.
   *
   * @param edgeIndex the index of the edge
   * @return the normal vector of the edge
   */
  public Vector3f edgeNormal(int edgeIndex) {
    Vector3f a = this.parent.vertices()[this.edges[edgeIndex].a()];
    Vector3f b = this.parent.vertices()[this.edges[edgeIndex].b()];
    Vector3f edge = new Vector3f(b).sub(a);
    return this.normal.cross(edge, new Vector3f());
  }

  /**
   * Returns an array of the vertices of the face.
   *
   * @return an array of the vertices of the face
   */
  public Vector3f[] vertices() {
    Vector3f[] result = new Vector3f[this.vertices.length];
    for (int i = 0; i < this.vertices.length; i++) {
      result[i] = new Vector3f(this.parent.vertices()[this.vertices[i]]);
    }
    return result;
  }

  /**
   * Calculates and returns the center of the face.
   *
   * @return the center of the face
   */
  public Vector3f center() {
    Vector3f result = new Vector3f();
    for (int i = 0; i < this.vertices.length; i++) {
      result.add(this.parent.vertices()[this.vertices[i]]);
    }
    return result.div(this.vertices.length);
  }

  /**
   * Returns the normal vector of the face, transformed by the rotation of the parent collider and
   * entity.
   *
   * @return the normal vector of the face
   */
  public Vector3f normal() {
    Vector3f ret = new Vector3f(this.normal);
    Quaternionf rotation = this.parent.rotation().premul(this.parent.entity.rotation());
    rotation.transform(ret);
    return ret;
  }

  /**
   * Returns the edges of the face.
   *
   * @return an array of the edges of the face
   */
  public IntPair[] edges() {
    return this.edges;
  }

  /**
   * Checks if the face contains the specified vertex.
   *
   * @param vertex the vertex to check
   * @return true if the face contains the vertex, false otherwise
   */
  public boolean hasVertex(Vector3f vertex) {
    for (int i = 0; i < this.vertices.length; i++) {
      if (this.parent.vertices()[this.vertices[i]].equals(vertex)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the face contains the vertex with the specified index.
   *
   * @param index the index of the vertex to check
   * @return true if the face contains the vertex, false otherwise
   */
  public boolean hasVertex(int index) {
    for (int i = 0; i < this.vertices.length; i++) {
      if (this.vertices[i] == index) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the vertex at the specified index.
   *
   * @param index the index of the vertex
   * @return the vertex at the specified index
   */
  public Vector3f vertex(int index) {
    return new Vector3f(this.parent.vertices()[this.vertices[index]]);
  }

  /**
   * Returns the parent polyhedron collider of the face.
   *
   * @return the parent polyhedron collider
   */
  public PolyhedronCollider<?> parent() {
    return this.parent;
  }
}
