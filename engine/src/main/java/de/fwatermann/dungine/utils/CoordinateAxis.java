package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class CoordinateAxis {

  private ArrayMesh mesh;
  private ShaderProgram shaderProgram;
  private boolean ignoreDepth;
  private Vector3f position;
  private float size;

  public CoordinateAxis(Vector3f position, float size, boolean ignoreDepth) {
    this.ignoreDepth = ignoreDepth;
    this.position = position;
    this.size = size;
    this.initGL();
  }

  private void initGL() {
    ByteBuffer verticesB = BufferUtils.createByteBuffer(6 * 6 * 4);
    FloatBuffer vertices = verticesB.asFloatBuffer();
    vertices.position(0);
    vertices.put(
        new float[] {
          0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
          1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
          0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
          0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
          0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
          0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
        });
    vertices.flip();
    verticesB.position(0);

    this.mesh =
        new ArrayMesh(
            verticesB,
            PrimitiveType.LINES,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"),
            new VertexAttribute(3, DataType.FLOAT, "a_Color"));
    this.mesh.translation(this.position);
    this.mesh.setScale(this.size, this.size, this.size);

    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/CoordinateAxis.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/CoordinateAxis.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      this.shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void render(Camera<?> camera) {
    ThreadUtils.checkMainThread();

    boolean depthTestState = GL33.glIsEnabled(GL33.GL_DEPTH_TEST);
    if (this.ignoreDepth && depthTestState) {
      GL33.glDisable(GL33.GL_DEPTH_TEST);
    }
    this.shaderProgram.bind();
    this.mesh.render(camera, this.shaderProgram);
    this.shaderProgram.unbind();

    if (this.ignoreDepth && depthTestState) {
      GL33.glEnable(GL33.GL_DEPTH_TEST);
    }
  }

  public boolean ignoreDepth() {
    return this.ignoreDepth;
  }

  public CoordinateAxis ignoreDepth(boolean ignoreDepth) {
    this.ignoreDepth = ignoreDepth;
    return this;
  }

  public Vector3f position() {
    return new Vector3f(this.position);
  }

  public CoordinateAxis position(Vector3f position) {
    this.position = new Vector3f(position);
    this.mesh.translation(this.position);
    return this;
  }

  public float size() {
    return this.size;
  }

  public CoordinateAxis size(float size) {
    this.size = size;
    this.mesh.setScale(new Vector3f(this.size));
    return this;
  }

  public ArrayMesh mesh() {
    return this.mesh;
  }
}
