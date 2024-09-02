package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import java.nio.ByteBuffer;

/**
 * Represents a mesh that is not instanced, providing basic transformation capabilities such as
 * translation, rotation, and scaling. This abstract class serves as a foundation for meshes that
 * are manipulated individually rather than as part of an instanced group.
 */
public abstract class UnInstancedMesh<T extends UnInstancedMesh<?>>
    extends Mesh<UnInstancedMesh<T>> {
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
}
