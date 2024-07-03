package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

/**
 * Represents an indexed mesh, which is a collection of vertices and indices defining the geometry
 * of a 3D object. This class extends the Mesh class, providing functionality specific to indexed
 * meshes, such as managing vertex and index buffers, and rendering the mesh using OpenGL.
 */
public class IndexedMesh extends Mesh {

  private int glVAO;
  private int glVBO;
  private int glEBO;

  @Nullable private FloatBuffer vertices;

  @Nullable private IntBuffer indices;

  private boolean verticesDirty = false;
  private boolean indicesDirty = false;

  private ShaderProgram lastShaderProgarm = null;

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
    super(usageHint, attributes);
    if (vertices.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("VertexBuffer must be in native byte order!");
    }
    if (indices.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("IndexBuffer must be in native byte order!");
    }
    this.vertices = vertices;
    this.indices = indices;
    this.indicesDirty = true;
    this.verticesDirty = true;
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
   * Disposes of OpenGL resources associated with this mesh. This should be called when the mesh is
   * no longer needed to free up resources.
   */
  @Override
  public void dispose() {
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteBuffers(this.glEBO);
    GL33.glDeleteVertexArrays(this.glVAO);
  }

  @Override
  public int getVertexCount() {
    if (this.vertices == null) {
      return 0;
    }
    return this.vertices.capacity() / this.attributes.sizeInBytes();
  }

  @Override
  @Nullable
  public FloatBuffer getVertexBuffer() {
    return this.vertices;
  }

  @Override
  public void setVertexBuffer(FloatBuffer buffer) {
    if (buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
    this.vertices = buffer;
    this.verticesDirty = true;
    this.calcBoundingBox();
  }

  /**
   * Sets the indices of this mesh.
   *
   * @param buffer the new indices of this mesh
   */
  public void setIndexBuffer(IntBuffer buffer) {
    if (buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
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

  @Override
  protected void calcBoundingBox() {
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

  @Override
  protected void calcTransformMatrix() {
    this.transformMatrix =
        new Matrix4f().translationRotateScale(this.translation, this.rotation, this.scale);
  }

  private void updateBuffers() {
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

    if (this.lastShaderProgarm != shaderProgram) {
      GL33.glBindVertexArray(this.glVAO);
      this.attributes.forEach(
          attrib -> {
            int loc = shaderProgram.getAttributeLocation(attrib.name);
            if (loc != -1) {
              GL33.glEnableVertexAttribArray(loc);
              GL33.glVertexAttribPointer(
                  loc,
                  attrib.numComponents,
                  attrib.glType,
                  false,
                  this.attributes.sizeInBytes(),
                  attrib.offset);
            }
          });
      GL33.glBindVertexArray(0);
      this.lastShaderProgarm = shaderProgram;
    }

    if (bindShader) shaderProgram.bind();

    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawElements(primitiveType, count, GL33.GL_UNSIGNED_INT, offset);
    GL33.glBindVertexArray(0);

    if (bindShader) shaderProgram.unbind();
  }
}
