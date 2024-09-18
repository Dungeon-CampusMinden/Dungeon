package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * The Mesh class represents a 3D mesh object in the game engine. It provides methods for
 * manipulating the mesh's position, rotation, and scale, as well as methods for rendering the mesh.
 * This class is abstract, and should be extended by specific types of meshes.
 */
public abstract class Mesh<T extends Mesh<?>> extends Renderable<Mesh<T>> implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Mesh.class);

  protected int glVAO;
  protected int glVBO;

  protected VertexAttributeList attributes;
  protected @Null ByteBuffer vertices;
  protected boolean verticesDirty = false;

  protected GLUsageHint usageHint = GLUsageHint.DRAW_STATIC;
  protected ShaderProgram shaderProgram;
  protected ShaderProgram lastShader; // Used to avoid unnecessary shader binding
  protected PrimitiveType primitiveType;


  /**
   * Constructs a new Mesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected Mesh(ByteBuffer vertices, PrimitiveType primitiveType, GLUsageHint usageHint, VertexAttributeList attributes) {
    GLUtils.checkBuffer(vertices);
    this.vertices = vertices;
    this.primitiveType = primitiveType;
    this.usageHint = usageHint;
    this.attributes = attributes;
    this.verticesDirty = this.vertices != null;
    this.initGL();
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
  public int vertexCount() {
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
  public ByteBuffer vertexBuffer() {
    return this.vertices;
  }

  /**
   * Sets the vertex buffer of the mesh to the specified buffer.
   *
   * @param buffer the new vertex buffer of the mesh
   */
  public T vertexBuffer(ByteBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    this.vertices = buffer;
    this.verticesDirty = true;
    return (T) this;
  }

  /** Marks the vertices of the mesh as dirty, causing them to be updated before rendering. */
  public void markVerticesDirty() {
    this.verticesDirty = true;
  }

  public ShaderProgram shaderProgram() {
    return this.shaderProgram;
  }

  public T shaderProgram(ShaderProgram shaderProgram) {
    this.shaderProgram = shaderProgram;
    return (T) this;
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

  @Override
  public void render(Camera<?> camera) {
    this.render(camera, this.shaderProgram);
  }

  public abstract void render(Camera<?> camera, ShaderProgram shader, int offset, int count);

}
