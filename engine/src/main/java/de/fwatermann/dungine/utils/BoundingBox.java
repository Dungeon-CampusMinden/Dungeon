package de.fwatermann.dungine.utils;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The `BoundingBox` class represents a 3D axis-aligned bounding box.
 * It provides methods to calculate the center, dimensions, and to check for containment and intersection with other bounding boxes.
 */
public class BoundingBox {

  private final Vector3f min;
  private final Vector3f max;

  /**
   * Constructs a `BoundingBox` with the specified minimum and maximum coordinates.
   *
   * @param minX the minimum x-coordinate
   * @param minY the minimum y-coordinate
   * @param minZ the minimum z-coordinate
   * @param maxX the maximum x-coordinate
   * @param maxY the maximum y-coordinate
   * @param maxZ the maximum z-coordinate
   */
  public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    this.min = new Vector3f(minX, minY, minZ);
    this.max = new Vector3f(maxX, maxY, maxZ);
  }

  /**
   * Constructs a `BoundingBox` with the specified minimum and maximum vectors.
   *
   * @param min the minimum vector
   * @param max the maximum vector
   */
  public BoundingBox(Vector3f min, Vector3f max) {
    this.min = min;
    this.max = max;
  }

  /**
   * Gets the minimum vector of the bounding box.
   *
   * @return the minimum vector
   */
  public Vector3f getMin() {
    return this.min;
  }

  /**
   * Gets the maximum vector of the bounding box.
   *
   * @return the maximum vector
   */
  public Vector3f getMax() {
    return this.max;
  }

  /**
   * Gets the center of the bounding box.
   *
   * @return the center vector
   */
  public Vector3f getCenter() {
    Vector3f center = new Vector3f();
    this.min.add(this.max, center);
    center.mul(0.5f);
    return center;
  }

  /**
   * Gets the dimensions of the bounding box.
   *
   * @return the dimensions vector
   */
  public Vector3f getDimensions() {
    Vector3f dimensions = new Vector3f();
    this.max.sub(this.min, dimensions);
    return dimensions;
  }

  /**
   * Checks if the bounding box contains the specified point.
   *
   * @param point the point to check
   * @return true if the bounding box contains the point, false otherwise
   */
  public boolean contains(Vector3f point) {
    return point.x >= this.min.x
        && point.y >= this.min.y
        && point.z >= this.min.z
        && point.x <= this.max.x
        && point.y <= this.max.y
        && point.z <= this.max.z;
  }

  /**
   * Checks if the bounding box intersects with another bounding box.
   *
   * @param other the other bounding box
   * @return true if the bounding boxes intersect, false otherwise
   */
  public boolean intersects(BoundingBox other) {
    return this.min.x <= other.max.x
        && this.max.x >= other.min.x
        && this.min.y <= other.max.y
        && this.max.y >= other.min.y
        && this.min.z <= other.max.z
        && this.max.z >= other.min.z;
  }

  /**
   * Creates a bounding box from the specified vertices.
   *
   * @param buffer the buffer containing the vertices
   * @param offset the offset in the buffer
   * @param stride the stride between vertices
   * @param count the number of vertices
   * @param transformation the transformation matrix to apply to the vertices
   * @return the created bounding box
   */
  public static BoundingBox fromVertices(FloatBuffer buffer, int offset, int stride, int count, Matrix4f transformation) {
    Vector3f min = new Vector3f(Float.MAX_VALUE);
    Vector3f max = new Vector3f(-Float.MAX_VALUE);
    for(int i = 0; i < count; i++) {
      float x = buffer.get(offset + i * stride);
      float y = buffer.get(offset + i * stride + 1);
      float z = buffer.get(offset + i * stride + 2);
      Vector3f vertex = new Vector3f(x, y, z);
      if(transformation != null) {
        transformation.transformPosition(vertex);
      }
      min.min(vertex);
      max.max(vertex);
    }
    return new BoundingBox(min, max);
  }
}
