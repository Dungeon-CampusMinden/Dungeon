package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class WireframeBox extends Renderable<WireframeBox> {

  private static ShaderProgram SHADER;
  private static IndexedMesh MESH;

  private float lineWidth = 1.0f;
  private int color = 0xFFFFFFFF;

  public WireframeBox(Vector3f position, Vector3f size, float lineWidth, int color) {
    super(position, size, new Quaternionf());
    this.color = color;
    this.lineWidth = lineWidth;
  }

  public WireframeBox() {
    this(new Vector3f(), new Vector3f(1.0f), 1.0f, 0xFFFFFFFF);
  }

  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/WireframeBox.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/WireframeBox.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load WireframeBox shader", ex);
    }
  }

  private static void initMesh() {
    if (MESH != null) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(8 * 3 * 4);
    vertices.asFloatBuffer().put(new float[] {
      0.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 1.0f, 0.0f,
      1.0f, 1.0f, 0.0f,
      1.0f, 1.0f, 1.0f,
      0.0f, 1.0f, 1.0f
    });

    ByteBuffer indices = BufferUtils.createByteBuffer(24 * 4);
    indices.asIntBuffer().put(new int[] {
      0, 1, 1, 2, 2, 3, 3, 0,
      4, 5, 5, 6, 6, 7, 7, 4,
      0, 4, 1, 5, 2, 6, 3, 7
    });

    MESH =
        new IndexedMesh(
            vertices,
            PrimitiveType.LINES,
            indices,
            IndexDataType.UNSIGNED_INT,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"));
  }

  @Override
  public void render(Camera<?> camera) {
    initShader();
    this.render(camera, SHADER);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    initMesh();

    shader.bind();
    shader.setUniform1i("uColor", this.color);
    MESH.transformation(this.position(), this.rotation(), this.scaling());
    float lineWidthBefore = GL33.glGetFloat(GL33.GL_LINE_WIDTH);
    GL33.glLineWidth(this.lineWidth);
    MESH.render(camera, shader);
    GL33.glLineWidth(lineWidthBefore);
    shader.unbind();
  }

  public float lineWidth() {
    return this.lineWidth;
  }

  public WireframeBox lineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  public int color() {
    return this.color;
  }

  public WireframeBox color(int color) {
    this.color = color;
    return this;
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

}
