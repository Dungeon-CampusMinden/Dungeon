package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.ByteBuffer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Pane extends Renderable<Pane> {

  private static ShaderProgram SHADER;

  protected ArrayMesh mesh;

  public Pane(Vector3f position, Vector3f size) {
    super(position, size, new Quaternionf());
    this.initMesh();
  }

  private void initMesh() {
    ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 5 * 4);
    vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
              0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
              1.0f, 1.0f, 0.0f, 1.0f, 1.0f
            });
    this.mesh =
        new ArrayMesh(
            vertices,
            PrimitiveType.TRIANGLE_STRIP,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"),
            new VertexAttribute(2, DataType.FLOAT, "a_TexCoord"));
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

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if(shader == null) return;
    shader.bind();
    this.mesh.transformation(this.position(), this.rotation(), this.scaling());
    this.mesh.render(camera, shader);
    shader.unbind();
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

out vec4 fragColor;

void main() {
 fragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}
""";
}
