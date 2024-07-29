package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.ui.UIElement;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/**
 * The ColorPane class represents a UI element that displays a colored pane. It extends the
 * UIElement class and provides methods to set the color and render the pane.
 */
public class UIColorPane extends UIElement<UIColorPane> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private int color = 0xFFFFFFFF;

  /**
   * Constructs a ColorPane with the specified color.
   *
   * @param rgba the color in RGBA format
   */
  public UIColorPane(int rgba) {
    this.color = rgba;
  }

  /**
   * Renders the ColorPane using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  protected void render(Camera<?> camera) {
    if (SHADER == null) {
      Shader vertexShader = new Shader(VERTEX_SHADER, Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = new Shader(FRAGMENT_SHADER, Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
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
    MESH.transformation(this.absolutePosition(), null, this.size);
    SHADER.bind();
    SHADER.setUniform1i("uColor", this.color);
    MESH.render(camera, SHADER);
    SHADER.unbind();
  }

  /**
   * Gets the color of the ColorPane.
   *
   * @return the color in RGBA format
   */
  public int color() {
    return this.color;
  }

  /**
   * Sets the color of the ColorPane.
   *
   * @param rgba the new color in RGBA format
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane color(int rgba) {
    this.color = rgba;
    return this;
  }

  /**
   * Sets the color of the ColorPane using individual RGBA components.
   *
   * @param r the red component (0-255)
   * @param g the green component (0-255)
   * @param b the blue component (0-255)
   * @param a the alpha component (0-255)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane color(int r, int g, int b, int a) {
    this.color = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
    return this;
  }

  /**
   * Sets the color of the ColorPane using individual RGB components and a default alpha value of
   * 255.
   *
   * @param r the red component (0-255)
   * @param g the green component (0-255)
   * @param b the blue component (0-255)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane color(int r, int g, int b) {
    return this.color(r, g, b, 0xFF);
  }

  /**
   * Sets the color of the ColorPane using individual RGBA components as floats.
   *
   * @param r the red component (0.0-1.0)
   * @param g the green component (0.0-1.0)
   * @param b the blue component (0.0-1.0)
   * @param a the alpha component (0.0-1.0)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane color(float r, float g, float b, float a) {
    return this.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  private static final String VERTEX_SHADER =
      """
#version 330 core

layout (location=0) in vec3 aPosition;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}

""";

  private static final String FRAGMENT_SHADER =
      """
#version 330 core

uniform int uColor;

out vec4 fragColor;

void main() {
  fragColor = vec4((uColor >> 24) & 0xFF, (uColor >> 16) & 0xFF, (uColor >> 8) & 0xFF, uColor & 0xFF) / 255.0f;
}
""";
}
