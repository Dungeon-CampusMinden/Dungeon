package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
public class InstancedIndexedMesh extends InstancedMesh {

  private static final Logger LOGGER = LogManager.getLogger(InstancedArrayMesh.class);

  private int glEBO;

  private @Null IntBuffer indices;
  private boolean indicesDirty = false;
  private ShaderProgram lastShaderProgram = null;

  /**
   * Constructs a new InstancedIndexedMesh with the specified vertices, indices, instance data,
   * instance count, usage hint, attributes, and instance attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param indices the index buffer of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedIndexedMesh(
      FloatBuffer vertices,
      IntBuffer indices,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, instanceData, instanceCount, usageHint, attributes, instanceAttributes);
    GLUtils.checkBuffer(indices);
    this.indices = indices;
    this.indicesDirty = indices != null;
    this.initGL();
  }

  /**
   * Constructs a new InstancedIndexedMesh with the specified vertices, indices, instance data,
   * instance count, attributes, and instance attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param indices the index buffer of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedIndexedMesh(
      FloatBuffer vertices,
      IntBuffer indices,
      ByteBuffer instanceData,
      int instanceCount,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(
        vertices,
        indices,
        instanceData,
        instanceCount,
        GLUsageHint.DRAW_STATIC,
        attributes,
        instanceAttributes);
  }

  /**
   * Constructs a new InstancedIndexedMesh with the specified usage hint, attributes, and instance
   * attributes.
   *
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedIndexedMesh(
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(null, null, null, 0, usageHint, attributes, instanceAttributes);
  }

  public InstancedIndexedMesh(
      VertexAttributeList attributes, InstanceAttributeList instanceAttributes) {
    this(GLUsageHint.DRAW_STATIC, attributes, instanceAttributes);
  }

  private void initGL() {
    ThreadUtils.checkMainThread();
    this.glEBO = GL33.glGenBuffers();
    LOGGER.debug("Generated EBO: {}", this.glEBO);
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
      LOGGER.debug("Updated EBO {}", this.glEBO);
    }
  }

  /**
   * Sets the indices of this mesh.
   *
   * @param buffer the new indices of this mesh
   */
  public void setIndexBuffer(IntBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    this.indices = buffer;
    this.indicesDirty = true;
  }

  /**
   * Returns the indices of this mesh.
   *
   * @return the indices of this mesh
   */
  public IntBuffer getIndexBuffer() {
    return this.indices;
  }

  /**
   * Marks the indices of this mesh as dirty, causing them to be updated the next time the mesh is
   * rendered.
   */
  public void markIndicesDirty() {
    this.indicesDirty = true;
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {
    ThreadUtils.checkMainThread();

    if (this.vertices == null || this.instanceCount <= 0) return;
    this.updateVertexBuffer();
    this.updateIndexBuffer();
    this.updateInstanceBuffer();

    if (this.lastShaderProgram != shaderProgram) {
      this.attributes.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO);
      this.instanceAttributes.bindAttribPointers(shaderProgram, this.glVAO, this.instanceData);
      this.lastShaderProgram = shaderProgram;
    }

    if (bindShader) {
      shaderProgram.bind();
    }

    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElementsInstanced(
        primitiveType, count, GL33.GL_UNSIGNED_INT, offset, this.instanceCount);
    GL33.glBindVertexArray(0);

    if (bindShader) {
      shaderProgram.unbind();
    }
  }

  @Override
  public void dispose() {
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteBuffers(this.glEBO);
    GL33.glDeleteBuffers(this.glIBO);
    GL33.glDeleteVertexArrays(this.glVAO);
  }
}
