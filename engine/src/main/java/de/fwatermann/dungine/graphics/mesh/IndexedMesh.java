package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * Represents an indexed mesh, which is a collection of vertices and indices defining the geometry
 * of a 3D object. This class extends the Mesh class, providing functionality specific to indexed
 * meshes, such as managing vertex and index buffers, and rendering the mesh using OpenGL.
 */
public class IndexedMesh extends UnInstancedMesh<IndexedMesh> {

  private static final Logger LOGGER = LogManager.getLogger(IndexedMesh.class);

  protected int glEBO;
  protected IndexDataType indexDataType;
  protected @Null ByteBuffer indices;
  protected boolean indicesDirty = false;

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
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer indices,
      IndexDataType indexDataType,
      GLUsageHint usageHint,
      VertexAttributeList attributes) {
    super(vertices, primitiveType, usageHint, attributes);
    GLUtils.checkBuffer(indices);
    this.indices = indices;
    this.indexDataType = indexDataType;
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
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer indices,
      IndexDataType indexDataType,
      GLUsageHint usageHint,
      VertexAttribute... attributes) {
    this(
        vertices,
        primitiveType,
        indices,
        indexDataType,
        usageHint,
        new VertexAttributeList(attributes));
  }

  /**
   * Constructs an IndexedMesh with specified vertices, indices, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer indices,
      IndexDataType indexDataType,
      VertexAttributeList attributes) {
    this(vertices, primitiveType, indices, indexDataType, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs an IndexedMesh with specified vertices, indices, and vertex attributes.
   *
   * @param vertices The buffer containing the mesh's vertices.
   * @param indices The buffer containing the mesh's indices.
   * @param attributes The vertex attributes of the mesh.
   */
  public IndexedMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer indices,
      IndexDataType indexDataType,
      VertexAttribute... attributes) {
    this(
        vertices,
        primitiveType,
        indices,
        indexDataType,
        GLUsageHint.DRAW_STATIC,
        new VertexAttributeList(attributes));
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

  private void initGL() {
    ThreadUtils.checkMainThread();
    this.glEBO = GL33.glGenBuffers();
    LOGGER.trace("Generated EBO: {}", this.glEBO);
    this.updateIndexBuffer();
  }

  protected void updateIndexBuffer() {
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
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.render(camera, shader, 0, this.indexCount());
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader, int offset, int count) {
    if (shader == null) return;
    if (this.vertices == null || this.indices == null) return;
    if (offset < 0) throw new IllegalArgumentException("Offset must be greater than or equal to 0");
    if (count < 0) throw new IllegalArgumentException("Count must be greater than or equal to 0");
    ThreadUtils.checkMainThread();

    this.updateVertexBuffer();
    this.updateIndexBuffer();

    if (this.lastShader != shader) {
      this.attributes.bindAttribPointers(shader, this.glVAO, this.glVBO);
      this.lastShader = shader;
    }

    boolean wasBound = shader.bound();
    if (!shader.bound()) shader.bind();

    shader.setUniformMatrix4f(shader.configuration().uniformModelMatrix, this.transformMatrix());
    shader.useCamera(camera);
    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElements(this.primitiveType.glType, count, this.indexDataType.glType, offset);
    GL33.glBindVertexArray(0);

    if (!wasBound) shader.unbind();
  }

  @Override
  public void dispose() {
    super.dispose();
    GL33.glDeleteBuffers(this.glEBO);
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }
}
