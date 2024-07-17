package de.fwatermann.dungine.graphics.mesh.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.IRenderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Plane implements IRenderable {

  private static ShaderProgram SHADER;

  protected ArrayMesh mesh;

  public Plane(Vector3f position, Vector3f size) {
    this.initMesh();
    this.mesh.translation(position);
    this.mesh.setScale(size);
  }

  /**
   * Set position of the cube.
   * @param position the new position of the cube
   */
  public void position(Vector3f position) {
    this.mesh.translation(position);
  }

  /**
   * Move the plane by the specified offset.
   * @param offset the offset to move the plane by
   */
  public void move(Vector3f offset) {
    this.mesh.translate(offset);
  }

  /**
   * Get the position of the plane.
   * @return the position of the plane
   */
  public Vector3f position() {
    return this.mesh.translation();
  }

  /**
   * Get the scale of the plane.
   * @param scalar the scalar to scale the plane by
   */
  public void scale(float scalar) {
    this.mesh.scale(scalar, scalar, scalar);
  }

  /**
   * Scale the plane by the specified scale.
   * @param scale the scale to scale the plane by
   */
  public void scale(Vector3f scale) {
    this.mesh.scale(scale);
  }

  /**
   * Set the scale of the plane.
   * @param scale the new scale of the plane
   */
  public void setScale(Vector3f scale) {
    this.mesh.setScale(scale);
  }

  /**
   * Rotate the plane by the specified axis and angle.
   * @param axis the axis to rotate the plane around
   * @param angle the angle to rotate the plane by
   */
  public void rotate(Vector3f axis, float angle) {
    this.mesh.rotate(axis, angle);
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
