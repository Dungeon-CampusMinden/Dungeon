package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class ArrayMesh extends Mesh {

  private int glVAO;
  private int glVBO;

  @Nullable private FloatBuffer vertices;

  private boolean dirty = false;

  @Nullable private ShaderProgram lastShaderProgram = null;

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, GLUsageHint usageHint, VertexAttributeList attributes) {
    super(usageHint, attributes);
    if (vertices.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("VertexBuffer must be in native byte order!");
    }
    this.vertices = vertices;
    this.initGL();
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, GLUsageHint usageHint, VertexAttribute... attributes) {
    this(vertices, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, VertexAttribute... attributes) {
    this(vertices, GLUsageHint.DRAW_STATIC, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new ArrayMesh with the specified usage hint and attributes.
   *
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    this(null, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new ArrayMesh with the specified attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, VertexAttributeList attributes) {
    this(vertices, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs a new ArrayMesh with the specified usage hint and attributes.
   *
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    this(null, usageHint, attributes);
  }

  private void initGL() {
    this.glVAO = GL30.glGenVertexArrays();
    this.glVBO = GL30.glGenBuffers();

    GL30.glBindVertexArray(this.glVAO);
    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.glVBO);

    if (this.vertices != null) {
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, this.vertices, GL30.GL_STATIC_DRAW);
    }

    GL30.glBindVertexArray(0);
  }

  @Override
  public int getVertexCount() {
    return this.vertices.capacity() / this.attributes.sizeInBytes();
  }

  @Override
  public FloatBuffer getVertexBuffer() {
    return this.vertices;
  }

  /**
   * Sets the vertex buffer of this mesh.
   *
   * @param vertices the new vertex buffer
   */
  @Override
  public void setVertexBuffer(FloatBuffer vertices) {
    if (vertices.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("VertexBuffer must be in native byte order!");
    }
    this.vertices = vertices;
    this.dirty = true;
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {

    if (this.dirty && this.vertices != null) {
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.glVBO);
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, this.vertices, GL30.GL_STATIC_DRAW);
      this.dirty = false;
    }

    if (this.vertices == null) {
      return;
    }

    if (bindShader) {
      shaderProgram.bind();
    }

    GL30.glBindVertexArray(this.glVAO);

    if (this.lastShaderProgram != shaderProgram) {
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.glVBO);
      this.attributes.forEach(
          attrib -> {
            int loc = shaderProgram.getAttributeLocation(attrib.name);
            if (loc != -1) {
              GL30.glEnableVertexAttribArray(loc);
              GL30.glVertexAttribPointer(
                  loc,
                  attrib.numComponents,
                  attrib.glType,
                  false,
                  this.attributes.sizeInBytes(),
                  attrib.offset);
            }
          });
      this.lastShaderProgram = shaderProgram;
    }

    GL30.glDrawArrays(primitiveType, offset, count);
    GL30.glBindVertexArray(0);

    if (bindShader) {
      shaderProgram.unbind();
    }
  }

  @Override
  public void dispose() {
    GL30.glDeleteBuffers(this.glVBO);
    GL30.glDeleteVertexArrays(this.glVAO);
  }

  @Override
  protected void calcTransformMatrix() {
    this.transformMatrix =
        new Matrix4f().translationRotateScale(this.translation, this.rotation, this.scale);
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
}
