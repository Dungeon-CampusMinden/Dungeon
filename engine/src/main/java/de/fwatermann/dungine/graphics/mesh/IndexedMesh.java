package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * Represents an indexed mesh, which is a collection of vertices and indices defining the geometry
 * of a 3D object. This class extends the Mesh class, providing functionality specific to indexed
 * meshes, such as managing vertex and index buffers, and rendering the mesh using OpenGL.
 */
public class IndexedMesh extends UnInstancedMesh {

  private static final Logger LOGGER = LogManager.getLogger(IndexedMesh.class);

  private int glEBO;
  private @Null IntBuffer indices;
  private boolean indicesDirty = false;

  /**
   * Constructs an IndexedMesh with specified vertices, indices, usage hint, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param usageHint Hint indicating how the mesh will be used, which affects performance
   *     optimizations.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(
      FloatBuffer vertices,
      IntBuffer indices,
      GLUsageHint usageHint,
      VertexAttributeList attributes) {
    super(vertices, usageHint, attributes);
    GLUtils.checkBuffer(indices);
    this.indices = indices;
    this.indicesDirty = this.indices != null;
    this.initGL();
  }

  /**
   * Constructs an IndexedMesh with specified vertices, indices, usage hint, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param usageHint Hint indicating how the mesh will be used, which affects performance
   *     optimizations.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(
      FloatBuffer vertices,
      IntBuffer indices,
      GLUsageHint usageHint,
      VertexAttribute... attributes) {
    this(vertices, indices, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs an IndexedMesh with specified vertices, indices, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(FloatBuffer vertices, IntBuffer indices, VertexAttributeList attributes) {
    this(vertices, indices, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs an IndexedMesh with specified vertices, indices, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(FloatBuffer vertices, IntBuffer indices, VertexAttribute... attributes) {
    this(vertices, indices, GLUsageHint.DRAW_STATIC, new VertexAttributeList(attributes));
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

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {
    ThreadUtils.checkMainThread();

    if (this.vertices == null || this.indices == null) {
      return;
    }
    this.updateVertexBuffer();
    this.updateIndexBuffer();

    if (this.lastShaderProgram != shaderProgram) {
      this.attributes.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO);
      this.lastShaderProgram = shaderProgram;
    }

    if (bindShader) shaderProgram.bind();

    shaderProgram.setUniformMatrix4f(
        shaderProgram.configuration().uniformModelMatrix, this.transformMatrix);

    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElements(primitiveType, count, GL33.GL_UNSIGNED_INT, offset);
    GL33.glBindVertexArray(0);

    if (bindShader) shaderProgram.unbind();
  }

  @Override
  public void dispose() {
    super.dispose();
    GL33.glDeleteBuffers(this.glEBO);
  }
}
