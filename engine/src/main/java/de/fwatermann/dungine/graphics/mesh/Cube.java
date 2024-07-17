package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.IRenderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class Cube extends IndexedMesh implements IRenderable {

  private static ShaderProgram SHADER;
  private int color;

  /**
   * Constructs a new Cube with the specified position and color.
   *
   * @param position the position of the cube
   * @param rgba the color of the cube in RGBA format.
   */
  public Cube(Vector3f position, int rgba) {
    super(
        BufferUtils.createByteBuffer(8 * 3 * 4),
        PrimitiveType.TRIANGLE_STRIP,
        BufferUtils.createByteBuffer(24 * 4),
        IndexDataType.UNSIGNED_INT,
        GLUsageHint.DRAW_STATIC,
        new VertexAttributeList(new VertexAttribute(3, DataType.FLOAT, "a_Position")));
    this.translation(position);
    this.color = rgba;
    this.initMesh();
  }

  private void initMesh() {
    this.vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              0.0f, 0.0f, 0.0f,
              0.0f, 0.0f, 1.0f,
              0.0f, 1.0f, 1.0f,
              0.0f, 1.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 1.0f,
              1.0f, 1.0f, 1.0f,
              1.0f, 1.0f, 0.0f
            });
    this.verticesDirty = true;
    this.indices
        .asIntBuffer()
        .position(0)
        .put(
            new int[] {
              2, 3, 6, 7, // TOP
              5, 4, 1, 0, // BOTTOM
              1, 0, 2, 3, // FRONT
              4, 5, 7, 6, // BACK
              0, 4, 3, 7, // LEFT
              5, 1, 6, 2 // RIGHT
            });
    this.indicesDirty = true;
  }

  public int color() {
    return this.color;
  }

  public void color(int rgba) {
    this.color = rgba;
  }

  public void color(int r, int g, int b, int a) {
    this.color = (a << 24) | (r << 16) | (g << 8) | b;
  }

  public void color(float r, float g, float b, float a) {
    this.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (shader == null) {
      return;
    }
    shader.bind();
    shader.setUniformMatrix4f("uModel", this.transformMatrix);
    shader.useCamera(camera);
    shader.setUniform4iv(
        "uColor",
        (this.color >> 24) & 0xFF,
        (this.color >> 16) & 0xFF,
        (this.color >> 8) & 0xFF,
        (this.color) & 0xFF);
    GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 24, GL33.GL_UNSIGNED_INT, 0);
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
