package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL33;

/**
 * Represents an indexed mesh, which is a collection of vertices and indices defining the geometry
 * of a 3D object. This class extends the Mesh class, providing functionality specific to indexed
 * meshes, such as managing vertex and index buffers, and rendering the mesh using OpenGL.
 */
public class IndexedMesh extends UnInstancedMesh {

  private int glVAO;
  private int glVBO;
  private int glEBO;

  private @Null IntBuffer indices;

  private boolean indicesDirty = false;

  private ShaderProgram lastShaderProgram = null;

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
    GLUtils.checkBuffer(vertices);
    GLUtils.checkBuffer(indices);
    this.vertices = vertices;
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
   * Constructs an IndexedMesh with specified usage hint and vertex attributes.
   *
   * @param usageHint Hint indicating how the mesh will be used, which affects performance
   *     optimizations.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    this(null, null, usageHint, attributes);
  }

  /**
   * Constructs an IndexedMesh with specified usage hint and vertex attributes.
   *
   * @param usageHint Hint indicating how the mesh will be used, which affects performance
   *     optimizations.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    this(null, null, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs an IndexedMesh with specified vertex attributes.
   *
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(VertexAttributeList attributes) {
    this(null, null, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs an IndexedMesh with specified vertex attributes.
   *
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(VertexAttribute... attributes) {
    this(null, null, GLUsageHint.DRAW_STATIC, new VertexAttributeList(attributes));
  }

  private void initGL() {
    ThreadUtils.checkMainThread();

    this.glVAO = GL33.glGenVertexArrays();
    this.glVBO = GL33.glGenBuffers();
    this.glEBO = GL33.glGenBuffers();

    GL33.glBindVertexArray(this.glVAO);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glVBO);
    GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.glEBO);
    GL33.glBindVertexArray(0);
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

  private void updateBuffers() {
    ThreadUtils.checkMainThread();
    GL33.glBindVertexArray(this.glVAO);
    if (this.verticesDirty && this.vertices != null) {
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.vertices, this.usageHint.getGLConstant());
      this.verticesDirty = false;
    }
    if (this.indicesDirty && this.indices != null) {
      this.indices.position(0);
      GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, this.indices, this.usageHint.getGLConstant());
      this.indicesDirty = false;
    }
    GL33.glBindVertexArray(0);
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {
    ThreadUtils.checkMainThread();

    if (this.vertices == null || this.indices == null) {
      return;
    }

    this.updateBuffers();

    if (this.lastShaderProgram != shaderProgram) {
      this.attributes.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO);
      this.lastShaderProgram = shaderProgram;
    }

    if (bindShader) shaderProgram.bind();

    shaderProgram.setUniformMatrix4f(
        shaderProgram.configuration().uniformModelMatrix(), this.transformMatrix);

    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElements(primitiveType, count, GL33.GL_UNSIGNED_INT, offset);
    GL33.glBindVertexArray(0);

    if (bindShader) shaderProgram.unbind();
  }

  @Override
  public void dispose() {
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteBuffers(this.glEBO);
    GL33.glDeleteVertexArrays(this.glVAO);
  }
}
