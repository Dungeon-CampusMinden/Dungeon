package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * Represents an instanced, indexed mesh for rendering in OpenGL. This class extends {@link
 * InstancedMesh} to add support for element array buffers (indices), allowing for efficient
 * rendering of complex geometries. It manages the creation and updating of vertex array objects
 * (VAOs), vertex buffer objects (VBOs), element buffer objects (EBOs), and instance buffer objects
 * (IBOs) based on the provided data.
 */
public class InstancedIndexedMesh extends InstancedMesh<InstancedIndexedMesh> {

  private static final Logger LOGGER = LogManager.getLogger(InstancedArrayMesh.class);

  private int glEBO;

  private final IndexDataType indexDataType;
  private @Null ByteBuffer indices;
  private boolean indicesDirty = false;
  private final ShaderProgram lastShaderProgram = null;

  /**
   * Constructs a new InstancedIndexedMesh with the specified vertices, indices, instance data,
   * instance count, usage hint, attributes, and instance attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param indices the index buffer of the mesh
   * @param indexDataType the data type of the indices
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedIndexedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer indices,
      IndexDataType indexDataType,
      List<ByteBuffer> instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(
        vertices,
        primitiveType,
        instanceData,
        instanceCount,
        usageHint,
        attributes,
        instanceAttributes);
    GLUtils.checkBuffer(indices);
    this.indices = indices;
    this.indexDataType = indexDataType;
    this.indicesDirty = indices != null;
    this.initGL();
  }

  private void initGL() {
    ThreadUtils.checkMainThread();
    this.glEBO = GL33.glGenBuffers();
    LOGGER.trace("Generated EBO: {}", this.glEBO);
    this.updateIndexBuffer();
  }

  private void updateIndexBuffer() {
    ThreadUtils.checkMainThread();
    if (this.indices != null && this.indicesDirty) {
      this.indices.position(0);
      GL33.glBindVertexArray(this.glVAO);
      GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.glEBO);
      GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, this.indices, this.usageHint.getGLConstant());
      GL33.glBindVertexArray(0);
      GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
      this.indicesDirty = false;
      LOGGER.trace("Updated EBO {}", this.glEBO);
    }
  }

  /**
   * Sets the indices of this mesh.
   *
   * @param buffer the new indices of this mesh
   */
  public void setIndexBuffer(ByteBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    this.indices = buffer;
    this.indicesDirty = true;
  }

  /**
   * Returns the indices of this mesh.
   *
   * @return the indices of this mesh
   */
  public ByteBuffer getIndexBuffer() {
    return this.indices;
  }

  /**
   * Marks the indices of this mesh as dirty, causing them to be updated the next time the mesh is
   * rendered.
   */
  public void markIndicesDirty() {
    this.indicesDirty = true;
  }

  /**
   * Returns the number of indices in this mesh.
   *
   * @return the number of indices in this mesh
   */
  public int indexCount() {
    return this.indices.limit() / this.indexDataType.bytes;
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.render(camera, shader, 0, this.indexCount());
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader, int offset, int count) {
    if (shader == null) return;
    if (this.vertices == null || this.instanceCount <= 0) return;
    if (offset < 0) throw new IllegalArgumentException("Offset must be greater than or equal to 0");
    if (count < 0) throw new IllegalArgumentException("Count must be greater than or equal to 0");
    ThreadUtils.checkMainThread();

    this.updateVertexBuffer();
    this.updateIndexBuffer();
    this.updateInstanceBuffer();

    if (this.lastShader != shader) {
      this.attributes.bindAttribPointers(shader, this.glVAO, this.glVBO);
      this.instanceAttributes.bindAttribPointers(shader, this.glVAO, this.instanceData);
      this.lastShader = shader;
    }

    boolean wasBound = shader.bound();
    if (!wasBound) shader.bind();

    shader.useCamera(camera);
    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElementsInstanced(
        this.primitiveType.glType, count, this.indexDataType.glType, offset, this.instanceCount);
    GL33.glBindVertexArray(0);

    if (!wasBound) shader.unbind();
  }

  @Override
  public void dispose() {
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteBuffers(this.glEBO);
    this.instanceData.forEach(buffer -> GL33.glDeleteBuffers(buffer.glIBO));
    GL33.glDeleteVertexArrays(this.glVAO);
  }
}
