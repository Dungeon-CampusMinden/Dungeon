package de.fwatermann.dungine.utils;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BoundingBox {

  private final Vector3f min;
  private final Vector3f max;

  public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    this.min = new Vector3f(minX, minY, minZ);
    this.max = new Vector3f(maxX, maxY, maxZ);
  }

  public BoundingBox(Vector3f min, Vector3f max) {
    this.min = min;
    this.max = max;
  }

  public Vector3f getMin() {
    return this.min;
  }

  public Vector3f getMax() {
    return this.max;
  }

  public Vector3f getCenter() {
    Vector3f center = new Vector3f();
    this.min.add(this.max, center);
    center.mul(0.5f);
    return center;
  }

  public Vector3f getDimensions() {
    Vector3f dimensions = new Vector3f();
    this.max.sub(this.min, dimensions);
    return dimensions;
  }

  public boolean contains(Vector3f point) {
    return point.x >= this.min.x
        && point.y >= this.min.y
        && point.z >= this.min.z
        && point.x <= this.max.x
        && point.y <= this.max.y
        && point.z <= this.max.z;
  }

  public boolean intersects(BoundingBox other) {
    return this.min.x <= other.max.x
        && this.max.x >= other.min.x
        && this.min.y <= other.max.y
        && this.max.y >= other.min.y
        && this.min.z <= other.max.z
        && this.max.z >= other.min.z;
  }

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
