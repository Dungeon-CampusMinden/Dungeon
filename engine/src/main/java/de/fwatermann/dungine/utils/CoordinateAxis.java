package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class CoordinateAxis extends Renderable<CoordinateAxis> {

  private ArrayMesh mesh;
  private ShaderProgram shaderProgram;
  private boolean ignoreDepth;

  public CoordinateAxis(Vector3f position, float size, boolean ignoreDepth) {
    super(position, new Vector3f(size), new Quaternionf());
    this.ignoreDepth = ignoreDepth;
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

  @Override
  public void render(Camera<?> camera) {
    ThreadUtils.checkMainThread();
    this.render(camera, this.shaderProgram);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    boolean depthTestState = GL33.glIsEnabled(GL33.GL_DEPTH_TEST);
    if (this.ignoreDepth && depthTestState) {
      GL33.glDisable(GL33.GL_DEPTH_TEST);
    }
    shader.bind();
    this.mesh.transformation(this.position(), this.rotation(), this.scaling());
    this.mesh.render(camera, shader);
    shader.unbind();

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

  public ArrayMesh mesh() {
    return this.mesh;
  }
}
