package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class Points extends Renderable<Points> {

  private static ShaderProgram SHADER;
  private ArrayMesh mesh;
  private final Set<Vector3f> points = new HashSet<>();
  private boolean pointsDirty = false;
  private int color = 0xFFFFFFFF;
  private float pointSize = 1.0f;

  public Points(int color, Set<Vector3f> points) {
    this.color = color;
    this.points.addAll(points);
  }

  public Points(int color, Vector3f ... points) {
    this.color = color;
    Collections.addAll(this.points, points);
  }

  public Points(int color) {
    this.color = color;
  }

  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/Points.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/Points.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void initMesh() {
    if(this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"));
  }

  private void updateMesh() {
    if(!this.pointsDirty) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(this.points.size() * 3 * 4);
    FloatBuffer floatView = vertices.asFloatBuffer();
    for(Vector3f point : this.points) {
      floatView.put(point.x).put(point.y).put(point.z);
    }
    this.mesh.vertexBuffer(vertices);
    this.pointsDirty = false;
  }

  @Override
  public void render(Camera<?> camera) {
    initShader();
    this.render(camera, SHADER);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.initMesh();
    this.updateMesh();

    shader.bind();
    shader.setUniform1i("uColor", this.color);
    float pointSize = GL33.glGetFloat(GL33.GL_POINT_SIZE);
    GL33.glPointSize(this.pointSize);
    this.mesh.render(camera, shader);
    GL33.glPointSize(pointSize);
    shader.unbind();
  }

  public int color() {
    return this.color;
  }

  public Points color(int color) {
    this.color = color;
    return this;
  }

  public void addPoint(Vector3f point) {
    this.points.add(point);
    this.pointsDirty = true;
  }

  public void removePoint(Vector3f point) {
    this.points.remove(point);
    this.pointsDirty = true;
  }

  public void clear() {
    if(this.points.isEmpty()) return;
    this.points.clear();
    this.pointsDirty = true;
  }

  public float pointSize() {
    return this.pointSize;
  }

  public Points pointSize(float lineWidth) {
    this.pointSize = lineWidth;
    return this;
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

}
