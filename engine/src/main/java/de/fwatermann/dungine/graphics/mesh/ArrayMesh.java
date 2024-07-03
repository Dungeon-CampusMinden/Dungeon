package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL33;

/**
 * The ArrayMesh class represents a 3D mesh object in the game engine that is rendered using an array
 * of vertices. It provides methods for manipulating the mesh's position, rotation, and scale, as well
 * as methods for rendering the mesh.
 */
public class ArrayMesh extends UnInstancedMesh {

  private int glVAO;
  private int glVBO;

  private @Null ShaderProgram lastShaderProgram = null;

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, GLUsageHint usageHint, VertexAttributeList attributes) {
    super(usageHint, attributes);
    GLUtils.checkBuffer(vertices);
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
   * Constructs a new ArrayMesh with the specified attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(FloatBuffer vertices, VertexAttributeList attributes) {
    this(vertices, GLUsageHint.DRAW_STATIC, attributes);
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
  public ArrayMesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    this(null, usageHint, attributes);
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

  private void initGL() {
    this.glVAO = GL33.glGenVertexArrays();
    this.glVBO = GL33.glGenBuffers();

    GL33.glBindVertexArray(this.glVAO);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glVBO);

    if (this.vertices != null) {
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.vertices, GL33.GL_STATIC_DRAW);
    }

    GL33.glBindVertexArray(0);
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {

    if (this.verticesDirty && this.vertices != null) {
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glVBO);
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.vertices, GL33.GL_STATIC_DRAW);
      this.verticesDirty = false;
    }

    if (this.vertices == null) {
      return;
    }

    if (bindShader) {
      shaderProgram.bind();
    }
    shaderProgram.setUniformMatrix4f(shaderProgram.configuration().uniformModelMatrix(), this.transformMatrix);

    GL33.glBindVertexArray(this.glVAO);

    if (this.lastShaderProgram != shaderProgram) {
      this.attributes.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO);
      this.lastShaderProgram = shaderProgram;
    }

    GL33.glDrawArrays(primitiveType, offset, count);
    GL33.glBindVertexArray(0);

    if (bindShader) {
      shaderProgram.unbind();
    }
  }

  @Override
  public void dispose() {
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteVertexArrays(this.glVAO);
  }
}
