package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.utils.GLUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * The InstancedMesh class represents a 3D mesh object in the game engine that is instanced. It
 * provides methods for manipulating the mesh's position, rotation, and scale, as well as methods
 * for rendering the mesh. This class is abstract and should be extended by specific types of
 * instanced meshes.
 *
 * @param <T> the extending type of InstancedMesh
 */
public abstract class InstancedMesh<T extends InstancedMesh<?>> extends Mesh<InstancedMesh<T>> {

  private static final Logger LOGGER = LogManager.getLogger(InstancedMesh.class);

  /** The instance attributes of the mesh. */
  protected InstanceAttributeList instanceAttributes;

  /** The instance data buffers of the mesh. */
  protected ArrayList<InstanceDataBuffer> instanceData = new ArrayList<>();

  /** The number of instances of the mesh. */
  protected int instanceCount = 0;

  /**
   * Constructs a new InstancedMesh with the specified vertex buffer, primitive type, instance data
   * buffers, instance count, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param instanceData the instance data buffers of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  protected InstancedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      List<ByteBuffer> instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, primitiveType, usageHint, attributes);
    instanceData.forEach(
        buffer -> {
          GLUtils.checkBuffer(buffer);
          this.instanceData.add(
              new InstanceDataBuffer(GL33.glGenBuffers(), buffer, buffer != null));
        });
    this.instanceAttributes = instanceAttributes;
    this.instanceCount = instanceCount;
  }

  /**
   * Constructs a new InstancedMesh with the specified vertex buffer, instance data buffer, instance
   * count, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  protected InstancedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, primitiveType, usageHint, attributes);
    GLUtils.checkBuffer(instanceData);
    this.instanceAttributes = instanceAttributes;
    this.instanceData.add(
        new InstanceDataBuffer(GL33.glGenBuffers(), instanceData, instanceData != null));
    this.instanceCount = instanceCount;
  }

  /** Update the instance data buffer of the mesh. */
  protected void updateInstanceBuffer() {
    for (InstanceDataBuffer buffer : this.instanceData) {
      if (buffer.dirty) {
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, buffer.glIBO);
        GL33.glBufferData(
            GL33.GL_ARRAY_BUFFER, buffer.instanceData, this.usageHint.getGLConstant());
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        buffer.dirty = false;
        LOGGER.trace("Updated IBO {}", buffer.glIBO);
      }
    }
  }

  /**
   * Returns the instance data buffer of the mesh.
   *
   * @param index the index of the instance data buffer to return
   * @return the instance data buffer of the mesh
   */
  public ByteBuffer getInstanceData(int index) {
    InstanceDataBuffer buffers = this.instanceData.get(index);
    if (buffers != null) {
      return buffers.instanceData;
    }
    return null;
  }

  /**
   * Sets the instance data buffer of the mesh.
   *
   * @param buffer the new instance data buffer of the mesh
   * @param index the index of the instance data buffer to set
   */
  public void setInstanceData(ByteBuffer buffer, int index) {
    GLUtils.checkBuffer(buffer);
    InstanceDataBuffer buffers = this.instanceData.get(index);
    if (buffers != null) {
      buffers.instanceData = buffer;
      buffers.dirty = true;
    } else {
      this.instanceData.set(index, new InstanceDataBuffer(GL33.glGenBuffers(), buffer, true));
    }
  }

  /**
   * Returns the number of instances fo this mesh.
   *
   * @return the number of instances of this mesh
   */
  public int instanceCount() {
    return this.instanceCount;
  }

  /**
   * Sets the number of instances of this mesh. The instance data buffer must be updated after
   * calling this method otherwise the buffer will be out of sync with the instance count and may
   * lead to undefined behavior.
   *
   * @param count the new number of instances of this mesh
   */
  public void setInstanceCount(int count) {
    this.instanceCount = count;
  }

  /**
   * Mark the instance data buffer as dirty. This will cause the instance data buffer to be updated
   * the next time the mesh is rendered.
   */
  public void markInstanceDataDirty() {
    this.instanceData.forEach(buffer -> buffer.dirty = true);
  }

  /**
   * Mark the instance data buffer at the specified index as dirty. This will cause the instance
   * data buffer to be updated the next time the mesh is rendered.
   *
   * @param index the index of the instance data buffer to mark as dirty
   */
  public void markInstanceDataDirty(int index) {
    InstanceDataBuffer buffers = this.instanceData.get(index);
    if (buffers != null) {
      buffers.dirty = true;
    }
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  /**
   * The `InstanceDataBuffer` class represents a buffer for instance data in an instanced mesh. It
   * contains the OpenGL buffer object identifier, the instance data, and a flag indicating whether
   * the buffer is dirty and needs to be updated.
   */
  public static class InstanceDataBuffer {

    /** The OpenGL buffer object identifier. */
    public int glIBO;

    /** The instance data buffer. */
    public ByteBuffer instanceData;

    /** A flag indicating whether the buffer is dirty and needs to be updated. */
    public boolean dirty = false;

    /**
     * Constructs a new `InstanceDataBuffer` with the specified buffer object identifier, instance
     * data, and dirty flag.
     *
     * @param glIBO the OpenGL buffer object identifier
     * @param instanceData the instance data buffer
     * @param dirty a flag indicating whether the buffer is dirty
     */
    public InstanceDataBuffer(int glIBO, ByteBuffer instanceData, boolean dirty) {
      this.glIBO = glIBO;
      this.instanceData = instanceData;
      this.dirty = dirty;
    }

    /**
     * Constructs a new `InstanceDataBuffer` with the specified buffer object identifier and
     * instance data. The dirty flag is set to true if the instance data is not null.
     *
     * @param glIBO the OpenGL buffer object identifier
     * @param instanceData the instance data buffer
     */
    public InstanceDataBuffer(int glIBO, ByteBuffer instanceData) {
      this(glIBO, instanceData, instanceData != null);
    }
  }
}
