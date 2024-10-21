package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * Represents a wireframe box that can be rendered using OpenGL. The WireframeBox class extends
 * Renderable and provides methods to set and get the line width and color of the wireframe box.
 */
public class WireframeBox extends Renderable<WireframeBox> {

  private static ShaderProgram SHADER;
  private static IndexedMesh MESH;

  private float lineWidth = 1.0f;
  private int color = 0xFFFFFFFF;

  /**
   * Constructs a WireframeBox with the specified position, size, line width, and color.
   *
   * @param position the position of the wireframe box
   * @param size the size of the wireframe box
   * @param lineWidth the line width of the wireframe box
   * @param color the color of the wireframe box
   */
  public WireframeBox(Vector3f position, Vector3f size, float lineWidth, int color) {
    super(position, size, new Quaternionf());
    this.color = color;
    this.lineWidth = lineWidth;
  }

  /**
   * Constructs a WireframeBox with default settings. The default position is (0, 0, 0), the default
   * size is (1, 1, 1), the default line width is 1.0, and the default color is white.
   */
  public WireframeBox() {
    this(new Vector3f(), new Vector3f(1.0f), 1.0f, 0xFFFFFFFF);
  }

  /**
   * Initializes the shader program for the WireframeBox. This method is called internally before
   * rendering the wireframe box.
   */
  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/WireframeBox.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/WireframeBox.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load WireframeBox shader", ex);
    }
  }

  /**
   * Initializes the mesh for the WireframeBox. This method is called internally before rendering
   * the wireframe box.
   */
  private static void initMesh() {
    if (MESH != null) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(8 * 3 * 4);
    vertices
        .asFloatBuffer()
        .put(
            new float[] {
              0.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 1.0f,
              0.0f, 0.0f, 1.0f,
              0.0f, 1.0f, 0.0f,
              1.0f, 1.0f, 0.0f,
              1.0f, 1.0f, 1.0f,
              0.0f, 1.0f, 1.0f
            });

    ByteBuffer indices = BufferUtils.createByteBuffer(24 * 4);
    indices
        .asIntBuffer()
        .put(
            new int[] {
              0, 1, 1, 2, 2, 3, 3, 0,
              4, 5, 5, 6, 6, 7, 7, 4,
              0, 4, 1, 5, 2, 6, 3, 7
            });

    MESH =
        new IndexedMesh(
            vertices,
            PrimitiveType.LINES,
            indices,
            IndexDataType.UNSIGNED_INT,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"));
  }

  /**
   * Renders the WireframeBox using the specified camera. This method initializes the shader and
   * calls the render method with the shader.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    initShader();
    this.render(camera, SHADER);
  }

  /**
   * Renders the WireframeBox using the specified camera and shader program. This method sets the
   * shader uniforms and renders the mesh.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    initMesh();

    shader.bind();
    shader.setUniform1i("uColor", this.color);
    MESH.transformation(this.position(), this.rotation(), this.scaling());
    float lineWidthBefore = GL33.glGetFloat(GL33.GL_LINE_WIDTH);
    GL33.glLineWidth(this.lineWidth);
    MESH.render(camera, shader);
    GL33.glLineWidth(lineWidthBefore);
    shader.unbind();
  }

  /**
   * Returns the line width of the WireframeBox.
   *
   * @return the line width of the WireframeBox
   */
  public float lineWidth() {
    return this.lineWidth;
  }

  /**
   * Sets the line width of the WireframeBox.
   *
   * @param lineWidth the line width to set
   * @return this WireframeBox instance for method chaining
   */
  public WireframeBox lineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  /**
   * Returns the color of the WireframeBox.
   *
   * @return the color of the WireframeBox
   */
  public int color() {
    return this.color;
  }

  /**
   * Sets the color of the WireframeBox.
   *
   * @param color the color to set
   * @return this WireframeBox instance for method chaining
   */
  public WireframeBox color(int color) {
    this.color = color;
    return this;
  }

  /**
   * Determines whether the WireframeBox should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum to check
   * @return true if the WireframeBox should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }
}
