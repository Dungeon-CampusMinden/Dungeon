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
 *
 * @param <T> the extending tpye of Mesh
 */
public abstract class Mesh<T extends Mesh<?>> extends Renderable<Mesh<T>> implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Mesh.class);

  /** The OpenGL Vertex Array Object (VAO) identifier. */
  protected int glVAO;

  /** The OpenGL Vertex Buffer Object (VBO) identifier. */
  protected int glVBO;

  /** The list of vertex attributes for the mesh. */
  protected VertexAttributeList attributes;

  /** The vertex buffer of the mesh. */
  protected @Null ByteBuffer vertices;

  /** Flag indicating whether the vertices are dirty and need to be updated. */
  protected boolean verticesDirty = false;

  /** The usage hint for the mesh. */
  protected GLUsageHint usageHint = GLUsageHint.DRAW_STATIC;

  /** The shader program used for rendering the mesh. */
  protected ShaderProgram shaderProgram;

  /** The last shader program used, to avoid unnecessary binding. */
  protected ShaderProgram lastShader;

  /** The primitive type of the mesh. */
  protected PrimitiveType primitiveType;

  /**
   * Constructs a new Mesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected Mesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      GLUsageHint usageHint,
      VertexAttributeList attributes) {
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
   * @return this mesh for Method chaining
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

  /**
   * Returns the shader program used for rendering the mesh.
   *
   * @return the shader program
   */
  public ShaderProgram shaderProgram() {
    return this.shaderProgram;
  }

  /**
   * Sets the shader program used for rendering the mesh.
   *
   * @param shaderProgram the new shader program
   * @return this mesh for method chaining
   */
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

  /**
   * Renders the object using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    this.render(camera, this.shaderProgram);
  }

  /**
   * Get a read-only slice of the vertex buffer containing the vertex at the specified index.
   *
   * @param index the index of the vertex to get
   * @return the read-only slice of the vertex buffer
   */
  public ByteBuffer getVertex(int index) {
    return this.vertices
        .asReadOnlyBuffer()
        .slice(index * this.attributes.sizeInBytes(), this.attributes.sizeInBytes());
  }

  /**
   * Renders the object using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   * @param offset the offset of the vertices to render
   * @param count the number of vertices to render
   */
  public abstract void render(Camera<?> camera, ShaderProgram shader, int offset, int count);
}
