package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.ui.layout.Unit;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/**
 * Represents a UI spinner element that can be rendered with OpenGL. The UISpinner class extends
 * UIElement and provides methods to set and get the spinner color.
 */
public class UISpinner extends UIElement<UISpinner> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private int color = 0xFFFFFFFF;

  /**
   * Constructs a UISpinner with default settings. The default color is white and the aspect ratio
   * is set to 1:1.
   */
  public UISpinner() {
    super();
    this.layout.aspectRatio(Unit.px(1.0f));
  }

  /**
   * Initializes the OpenGL shader and mesh for the UISpinner. This method is called internally
   * before rendering the spinner.
   */
  private static void initGL() {
    if (SHADER == null) {
      try {
        Shader vertexShader =
            Shader.loadShader(
                Resource.load("/shaders/ui/Spinner.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader fragmentShader =
            Shader.loadShader(
                Resource.load("/shaders/ui/Spinner.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        SHADER = new ShaderProgram(vertexShader, fragmentShader);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    if (MESH == null) {
      ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 3 * 4);
      vertices
          .asFloatBuffer()
          .put(
              new float[] {
                0.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f
              })
          .flip();

      MESH =
          new ArrayMesh(
              vertices,
              PrimitiveType.TRIANGLES,
              GLUsageHint.DRAW_STATIC,
              new VertexAttribute(3, DataType.FLOAT, "aPosition"));
    }
  }

  /**
   * Renders the UISpinner using the specified camera. This method sets the shader uniforms and
   * renders the mesh.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  protected void render(Camera<?> camera) {
    initGL();

    MESH.transformation(this.absolutePosition(), this.rotation, this.size);
    SHADER.bind();
    SHADER.setUniform1i("uColor", this.color);
    SHADER.setUniform1i("uTime", (int) System.currentTimeMillis());
    SHADER.setUniform1f("uThickness", 0.1f);
    MESH.render(camera, SHADER);
    SHADER.unbind();
  }

  /**
   * Returns the color of the UISpinner.
   *
   * @return the color of the UISpinner
   */
  public int color() {
    return this.color;
  }

  /**
   * Sets the color of the UISpinner.
   *
   * @param color the color to set
   * @return this UISpinner instance for method chaining
   */
  public UISpinner color(int color) {
    this.color = color;
    return this;
  }
}
