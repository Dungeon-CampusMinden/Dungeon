package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
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
 */
public abstract class InstancedMesh extends Mesh {

  private static final Logger LOGGER = LogManager.getLogger(InstancedMesh.class);

  protected InstanceAttributeList instanceAttributes;
  protected ArrayList<InstanceDataBuffer> instanceData = new ArrayList<>();
  protected int instanceCount = 0;

  protected InstancedMesh(
      ByteBuffer vertices,
      List<ByteBuffer> instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, usageHint, attributes);
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
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  protected InstancedMesh(
      ByteBuffer vertices,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, usageHint, attributes);
    GLUtils.checkBuffer(instanceData);
    this.instanceAttributes = instanceAttributes;
    this.instanceData.add(
        new InstanceDataBuffer(GL33.glGenBuffers(), instanceData, instanceData != null));
    this.instanceCount = instanceCount;
  }

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

  public void markInstanceDataDirty(int index) {
    InstanceDataBuffer buffers = this.instanceData.get(index);
    if (buffers != null) {
      buffers.dirty = true;
    }
  }

  public static class InstanceDataBuffer {

    public int glIBO;
    public ByteBuffer instanceData;
    public boolean dirty = false;

    public InstanceDataBuffer(int glIBO, ByteBuffer instanceData, boolean dirty) {
      this.glIBO = glIBO;
      this.instanceData = instanceData;
      this.dirty = dirty;
    }

    public InstanceDataBuffer(int glIBO, ByteBuffer instanceData) {
      this(glIBO, instanceData, instanceData != null);
    }
  }
}
