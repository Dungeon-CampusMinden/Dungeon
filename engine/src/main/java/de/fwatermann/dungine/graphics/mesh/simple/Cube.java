package de.fwatermann.dungine.graphics.mesh.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.IRenderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Cube implements IRenderable {

  private static ShaderProgram SHADER;

  protected final Vector3f position;
  protected IndexedMesh mesh;

  /**
   * Constructs a new Cube with the specified position and color.
   *
   * @param position the position of the cube
   */
  public Cube(Vector3f position) {
    this.position = position;
    this.initMesh();
  }

  private void initMesh() {
    ByteBuffer vertices = BufferUtils.createByteBuffer(8 * 3 * 4);
    vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              0.0f, 0.0f, 0.0f, //0
              0.0f, 0.0f, 1.0f, //1
              0.0f, 1.0f, 1.0f, //2
              0.0f, 1.0f, 0.0f, //3
              1.0f, 0.0f, 0.0f, //4
              1.0f, 0.0f, 1.0f, //5
              1.0f, 1.0f, 1.0f, //6
              1.0f, 1.0f, 0.0f  //7
            });
    ByteBuffer indices = BufferUtils.createByteBuffer(36 * 4);
    indices
        .asIntBuffer()
        .position(0)
        .put(
            new int[] {
              3, 2, 6, 6, 7, 3, // TOP
              4, 5, 1, 1, 0, 4, // BOTTOM
              0, 1, 2, 2, 3, 0, // FRONT
              5, 4, 7, 7, 6, 5, // BACK
              4, 0, 3, 3, 7, 4, // LEFT
              1, 5, 6, 6, 2, 1  // RIGHT
            });
    this.mesh =
        new IndexedMesh(
            vertices,
            PrimitiveType.TRIANGLES,
            indices,
            IndexDataType.UNSIGNED_INT,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"));
    this.mesh.translation(this.position);
  }

  /**
   * Set position of the cube.
   * @param position the new position of the cube
   */
  public void position(Vector3f position) {
    this.position.set(position);
    this.mesh.translation(position);
  }

  /**
   * Move the cube by the specified offset.
   * @param offset the offset to move the cube by
   */
  public void move(Vector3f offset) {
    this.position.add(offset);
    this.mesh.translate(offset);
  }

  /**
   * Get the position of the cube.
   * @return the position of the cube
   */
  public Vector3f position() {
    return this.position;
  }

  /**
   * Get the scale of the cube.
   * @param scalar the scalar to scale the cube by
   */
  public void scale(float scalar) {
    this.mesh.scale(scalar, scalar, scalar);
  }

  /**
   * Scale the cube by the specified scale.
   * @param scale the scale to scale the cube by
   */
  public void scale(Vector3f scale) {
    this.mesh.scale(scale);
  }

  /**
   * Set the scale of the cube.
   * @param scale the new scale of the cube
   */
  public void setScale(Vector3f scale) {
    this.mesh.setScale(scale);
  }

  /**
   * Rotate the cube by the specified axis and angle.
   * @param axis the axis to rotate the cube around
   * @param angle the angle to rotate the cube by
   */
  public void rotate(Vector3f axis, float angle) {
    this.mesh.rotate(axis, angle);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (shader == null) {
      return;
    }
    shader.bind();
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

out vec4 fragColor;

void main() {
   fragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}
""";
}
