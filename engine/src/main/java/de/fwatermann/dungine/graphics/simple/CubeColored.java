package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import org.joml.Vector3f;

public class CubeColored extends Cube {

  private static ShaderProgram SHADER;

  private int color;

  /**
   * Constructs a new Cube with the specified position and color.
   *
   * @param position the position of the cube
   * @param rgba     the color of the cube in RGBA format.
   */
  public CubeColored(Vector3f position, int rgba) {
    super(position);
    this.color = rgba;
  }

  /**
   * Get the color of the cube.
   * @return the color of the cube
   */
  public int color() {
    return this.color;
  }

  /**
   * Set the color of the cube.
   * @param rgba the new color of the cube in RGBA format.
   */
  public void color(int rgba) {
    this.color = rgba;
  }

  /**
   * Set the color of the cube.
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(int r, int g, int b, int a) {
    this.color = (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Set the color of the cube.
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(float r, float g, float b, float a) {
    this.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (shader == null) {
      return;
    }
    shader.bind();
    shader.setUniform4fv(
      "uColor",
      ((this.color >> 24) & 0xFF) / 255.0f,
      ((this.color >> 16) & 0xFF) / 255.0f,
      ((this.color >> 8) & 0xFF) / 255.0f,
      ((this.color) & 0xFF) / 255.0f);
    this.mesh.render(camera, shader);
    shader.unbind();
  }

  @Override
  public void render(Camera<?> camera) {
    if (SHADER == null) {
      Shader vertexShader = new Shader(VERTEX_SHADER, Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = new Shader(FRAGMENT_SHADER, Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    }
    this.render(camera, SHADER);
  }

  private static final String VERTEX_SHADER =
    """
#version 330 core

in vec3 a_Position;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
}

""";

  private static final String FRAGMENT_SHADER =
    """
#version 330 core

uniform vec4 uColor;
out vec4 fragColor;

void main() {
 fragColor = uColor;
}
""";

}
