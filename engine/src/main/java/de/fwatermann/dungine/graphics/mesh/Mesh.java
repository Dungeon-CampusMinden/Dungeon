package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.FloatBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * The Mesh class represents a 3D mesh object in the game engine. It provides methods for
 * manipulating the mesh's position, rotation, and scale, as well as methods for rendering the mesh.
 * This class is abstract, and should be extended by specific types of meshes.
 */
public abstract class Mesh implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Mesh.class);

  protected int glVAO;
  protected int glVBO;

  protected VertexAttributeList attributes;
  protected @Null FloatBuffer vertices;
  protected boolean verticesDirty = false;

  protected GLUsageHint usageHint = GLUsageHint.DRAW_STATIC;
  protected ShaderProgram lastShaderProgram;

  /**
   * Constructs a new Mesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected Mesh(FloatBuffer vertices, GLUsageHint usageHint, VertexAttributeList attributes) {
    GLUtils.checkBuffer(vertices);
    this.vertices = vertices;
    this.attributes = attributes;
    this.usageHint = usageHint;
    this.verticesDirty = this.vertices != null;
    this.initGL();
  }

  /**
   * Constructs a new Mesh with the specified vertex buffer and attributes.
   *
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected Mesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    this(null, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new Mesh with the specified attributes.
   *
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected Mesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    this.attributes = attributes;
    this.usageHint = usageHint;
  }

  private void initGL() {
    this.glVAO = GL33.glGenVertexArrays();
    LOGGER.trace("Generated VAO: {}", this.glVAO);
    this.glVBO = GL33.glGenBuffers();
    LOGGER.trace("Generated VBO: {}", this.glVBO);
    this.updateVertexBuffer();
  }

  /** Updates the vertex buffer of the mesh in the OpenGL context if needed. */
  protected void updateVertexBuffer() {
    if (this.vertices != null && this.verticesDirty) {
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glVBO);
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.vertices, this.usageHint.getGLConstant());
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
      this.verticesDirty = false;
      LOGGER.trace("Updated VBO {}", this.glVBO);
    }
  }

  /**
   * Returns the number of vertices in the mesh.
   *
   * @return the number of vertices in the mesh
   */
  public int getVertexCount() {
    if (this.vertices == null) {
      return 0;
    }
    return this.vertices.capacity() / this.attributes.sizeInBytes();
  }

  /**
   * Returns the vertex buffer of the mesh.
   *
   * @return the vertex buffer of the mesh
   */
  @Null
  public FloatBuffer getVertexBuffer() {
    return this.vertices;
  }

  /**
   * Sets the vertex buffer of the mesh to the specified buffer.
   *
   * @param buffer the new vertex buffer of the mesh
   */
  public void setVertexBuffer(FloatBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    this.vertices = buffer;
    this.verticesDirty = true;
  }

  /** Marks the vertices of the mesh as dirty, causing them to be updated before rendering. */
  public void markVerticesDirty() {
    this.verticesDirty = true;
  }

  @Override
  public void dispose() {
    GL33.glDeleteVertexArrays(this.glVAO);
    GL33.glDeleteBuffers(this.glVBO);
  }

  /**
   * Returns the usage hint of the mesh.
   *
   * @return the usage hint of the mesh
   */
  public GLUsageHint usageHint() {
    return this.usageHint;
  }

  /**
   * Renders the mesh using the specified shader program and primitive type.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   */
  public void render(ShaderProgram shaderProgram, int primitiveType) {
    this.render(shaderProgram, primitiveType, 0, this.getVertexCount(), true);
  }

  /**
   * Renders a portion of the mesh using the specified shader program and primitive type.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   * @param offset the index of the first vertex to render
   * @param count the number of vertices to render
   */
  public void render(ShaderProgram shaderProgram, int primitiveType, int offset, int count) {
    this.render(shaderProgram, primitiveType, offset, count, true);
  }

  /**
   * Renders a portion of the mesh using the specified shader program and primitive type, with an
   * option to bind the shader.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   * @param offset the index of the first vertex to render
   * @param count the number of vertices to render
   * @param bindShader whether to bind the shader before rendering
   */
  public abstract void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader);
}
