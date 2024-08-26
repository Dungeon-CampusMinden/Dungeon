package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.utils.BoundingBox;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.nio.ByteBuffer;
import org.joml.Math;

/**
 * Represents a mesh that is not instanced, providing basic transformation capabilities such as
 * translation, rotation, and scaling. This abstract class serves as a foundation for meshes that
 * are manipulated individually rather than as part of an instanced group.
 */
public abstract class UnInstancedMesh<T extends UnInstancedMesh<?>>
    extends Mesh<UnInstancedMesh<T>> {

  @Nullable protected BoundingBox boundingBox;

  /**
   * Constructs a new UnInstancedMesh with the specified usage hint and attributes.
   *
   * @param vertices the vertices of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected UnInstancedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      GLUsageHint usageHint,
      VertexAttributeList attributes) {
    super(vertices, primitiveType, usageHint, attributes);
    // this.calcBoundingBox(); //TODO: Fix Buffer Undeflow
  }

  /**
   * Constructs a new UnInstancedMesh with the specified usage hint and attributes.
   *
   * @param vertices the vertices of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected UnInstancedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      GLUsageHint usageHint,
      VertexAttribute... attributes) {
    this(vertices, primitiveType, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Returns the bounding box of the mesh.
   *
   * @return the bounding box of the mesh
   */
  @Nullable
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  /** Calculates the bounding box of the mesh. */
  private void calcBoundingBox() {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float minZ = Float.MAX_VALUE;
    float maxX = Float.MIN_VALUE;
    float maxY = Float.MIN_VALUE;
    float maxZ = Float.MIN_VALUE;
    this.vertices.position(0);
    int vertexCount = this.vertices.capacity() / this.attributes.sizeInBytes();
    for (int i = 0; i < vertexCount; i++) {
      float x = this.vertices.get();
      float y = this.vertices.get();
      float z = this.vertices.get();
      this.vertices.position(
          this.vertices.position() + this.attributes.sizeInBytes() - 3 * Float.BYTES);
      minX = Math.min(minX, x);
      minY = Math.min(minY, y);
      minZ = Math.min(minZ, z);
      maxX = Math.max(maxX, x);
      maxY = Math.max(maxY, y);
      maxZ = Math.max(maxZ, z);
    }
    this.boundingBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
  }

}
