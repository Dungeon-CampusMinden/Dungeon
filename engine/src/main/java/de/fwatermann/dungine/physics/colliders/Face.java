package de.fwatermann.dungine.physics.colliders;

import de.fwatermann.dungine.utils.IntPair;
import org.joml.Vector3f;

public class Face {

  private final PolyhedronCollider<?> parent;
  private final int[] vertices;
  private final Vector3f normal;
  private final IntPair[] edges;

  public Face(PolyhedronCollider<?> parent, int[] vertexIndices, IntPair[] edges, Vector3f normal) {
    this.parent = parent;
    this.vertices = vertexIndices;
    this.edges = edges;
    this.normal = normal;
  }

  public Vector3f edgeNormal(int edgeIndex) {
    Vector3f a = this.parent.vertices()[this.edges[edgeIndex].a()];
    Vector3f b = this.parent.vertices()[this.edges[edgeIndex].b()];
    Vector3f edge = new Vector3f(b).sub(a);
    return this.normal.cross(edge, new Vector3f());
  }

  public Vector3f[] vertices() {
    Vector3f[] result = new Vector3f[this.vertices.length];
    for(int i = 0; i < this.vertices.length; i++) {
      result[i] = new Vector3f(this.parent.vertices()[this.vertices[i]]);
    }
    return result;
  }

  public Vector3f normal() {
    return new Vector3f(this.normal);
  }

  public IntPair[] edges() {
    return this.edges;
  }

  public boolean hasVertex(Vector3f vertex) {
    for(int i = 0; i < this.vertices.length; i++) {
      if(this.parent.vertices()[this.vertices[i]].equals(vertex)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasVertex(int index) {
    for(int i = 0; i < this.vertices.length; i++) {
      if(this.vertices[i] == index) {
        return true;
      }
    }
    return false;
  }

  public Vector3f vertex(int index) {
    return new Vector3f(this.parent.vertices()[this.vertices[index]]);
  }

  public PolyhedronCollider<?> parent() {
    return this.parent;
  }

}
