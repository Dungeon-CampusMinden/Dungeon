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
import de.fwatermann.dungine.ui.UIContainer;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/**
 * The ColorPane class represents a UI element that displays a colored pane. It extends the
 * UIElement class and provides methods to set the color and render the pane.
 */
public class UIColorPane extends UIContainer<UIColorPane> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private int fillColor;
  private int borderColor;
  private float borderWidth;
  private float borderRadius;

  /**
   * Constructs a ColorPane with the specified color.
   *
   * @param fillColor the color of the pane in RGBA format
   * @param borderColor the color of the border in RGBA format
   * @param borderWidth the width of the border
   * @param borderRadius the radius of the border
   */
  public UIColorPane(int fillColor, int borderColor, float borderWidth, float borderRadius) {
    this.fillColor = fillColor;
    this.borderColor = borderColor;
    this.borderWidth = borderWidth;
    this.borderRadius = borderRadius;
  }

  /**
   * Constructs a ColorPane with the specified color and border radius.
   *
   * @param fillColor the color of the pane in RGBA format
   * @param borderRadius the radius of the border
   */
  public UIColorPane(int fillColor, float borderRadius) {
    this(fillColor, 0x000000FF, 0.0f, borderRadius);
  }

  /**
   * Constructs a ColorPane with the specified color, border color, and border width.
   *
   * @param fillColor the color of the pane in RGBA format
   * @param borderColor the color of the border in RGBA format
   * @param borderWidth the width of the border
   */
  public UIColorPane(int fillColor, int borderColor, float borderWidth) {
    this(fillColor, borderColor, borderWidth, 0.0f);
  }

  /**
   * Constructs a ColorPane with the specified color.
   *
   * @param fillColor the color of the pane in RGBA format
   */
  public UIColorPane(int fillColor) {
    this(fillColor, 0x000000FF, 0.0f, 0.0f);
  }

  /**
   * Renders the ColorPane using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  protected void render(Camera<?> camera) {
    if (SHADER == null) {
      try {
        Shader vertexShader =
            Shader.loadShader(
                Resource.load("/shaders/ui/ColorPane.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader fragmentShader =
            Shader.loadShader(
                Resource.load("/shaders/ui/ColorPane.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        SHADER = new ShaderProgram(vertexShader, fragmentShader);
      } catch (IOException ex) {
        throw new RuntimeException("Failed to load shaders", ex);
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
    MESH.transformation(this.absolutePosition(), this.rotation, this.size);
    SHADER.bind();
    SHADER.setUniform1i("uFillColor", this.fillColor);
    SHADER.setUniform1i("uBorderColor", this.borderColor);
    SHADER.setUniform2f("uSize", this.size.x, this.size.y);
    SHADER.setUniform1f("uBorderWidth", this.borderWidth);
    SHADER.setUniform1f("uBorderRadius", this.borderRadius);
    MESH.render(camera, SHADER);
    SHADER.unbind();
  }

  /**
   * Gets the color of the ColorPane.
   *
   * @return the color in RGBA format
   */
  public int fillColor() {
    return this.fillColor;
  }

  /**
   * Sets the color of the ColorPane.
   *
   * @param rgba the new color in RGBA format
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane fillColor(int rgba) {
    this.fillColor = rgba;
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
  public UIColorPane fillColor(int r, int g, int b, int a) {
    this.fillColor = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
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
  public UIColorPane fillColor(int r, int g, int b) {
    return this.fillColor(r, g, b, 0xFF);
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
  public UIColorPane fillColor(float r, float g, float b, float a) {
    return this.fillColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  /**
   * Gets the border color of the ColorPane.
   *
   * @return the border color in RGBA format
   */
  public int borderColor() {
    return this.borderColor;
  }

  /**
   * Sets the border color of the ColorPane.
   *
   * @param rgba the new border color in RGBA format
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderColor(int rgba) {
    this.borderColor = rgba;
    return this;
  }

  /**
   * Sets the border color of the ColorPane using individual RGBA components.
   *
   * @param r the red component (0-255)
   * @param g the green component (0-255)
   * @param b the blue component (0-255)
   * @param a the alpha component (0-255)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderColor(int r, int g, int b, int a) {
    this.borderColor = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
    return this;
  }

  /**
   * Sets the border color of the ColorPane using individual RGB components and a default alpha
   * value of 255.
   *
   * @param r the red component (0-255)
   * @param g the green component (0-255)
   * @param b the blue component (0-255)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderColor(int r, int g, int b) {
    return this.borderColor(r, g, b, 0xFF);
  }

  /**
   * Sets the border color of the ColorPane using individual RGBA components as floats.
   *
   * @param r the red component (0.0-1.0)
   * @param g the green component (0.0-1.0)
   * @param b the blue component (0.0-1.0)
   * @param a the alpha component (0.0-1.0)
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderColor(float r, float g, float b, float a) {
    return this.borderColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  /**
   * Gets the border radius of the ColorPane.
   *
   * @return the border radius
   */
  public float borderRadius() {
    return this.borderRadius;
  }

  /**
   * Sets the border radius of the ColorPane.
   *
   * @param borderRadius the new border radius
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderRadius(float borderRadius) {
    this.borderRadius = borderRadius;
    return this;
  }

  /**
   * Gets the border width of the ColorPane.
   *
   * @return the border width
   */
  public float borderWidth() {
    return this.borderWidth;
  }

  /**
   * Sets the border width of the ColorPane.
   *
   * @param borderWidth the new border width
   * @return this ColorPane instance for method chaining
   */
  public UIColorPane borderWidth(float borderWidth) {
    this.borderWidth = borderWidth;
    return this;
  }
}
