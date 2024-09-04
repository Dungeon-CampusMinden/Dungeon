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
import de.fwatermann.dungine.utils.pair.Pair;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class Points extends Renderable<Points> {

  private static ShaderProgram SHADER;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Set<Pair<Vector3f, Integer>> points = new HashSet<>();

  private ArrayMesh mesh;
  private boolean pointsDirty = false;
  private int color = 0xFFFFFFFF;
  private float pointSize = 1.0f;

  public Points(int color, Set<Pair<Vector3f, Integer>> points) {
    this.color = color;
    this.points.addAll(points);
  }

  @SafeVarargs
  public Points(int color, Pair<Vector3f, Integer>... points) {
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
    if (this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(1, DataType.UNSIGNED_INT, "aColor"));
  }

  private void updateMesh() {
    if (!this.pointsDirty) return;
    try {
      this.lock.readLock().lock();
      ByteBuffer vertices = BufferUtils.createByteBuffer(this.points.size() * 4 * 4);
      for (Pair<Vector3f, Integer> point : this.points) {
        vertices.putFloat(point.a().x).putFloat(point.a().y).putFloat(point.a().z);
        vertices.putInt(point.b());
      }
      vertices.flip();
      this.mesh.vertexBuffer(vertices);
      this.pointsDirty = false;
    } finally {
      this.lock.readLock().unlock();
    }
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
    try {
      this.lock.writeLock().lock();
      this.points.add(new Pair<>(point, this.color));
      this.pointsDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void addPoint(Vector3f point, int color) {
    try {
      this.lock.writeLock().lock();
      this.points.add(new Pair<>(point, color));
      this.pointsDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void addPoint(Pair<Vector3f, Integer> point) {
    try {
      this.lock.writeLock().lock();
      this.points.add(point);
      this.pointsDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void removePoint(Pair<Vector3f, Integer> point) {
    try {
      this.lock.writeLock().lock();
      this.points.remove(point);
      this.pointsDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void clear() {
    if (this.points.isEmpty()) return;
    try {
      this.lock.writeLock().lock();
      this.points.clear();
      this.pointsDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
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
