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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class Lines extends Renderable<Lines> {

  private static final Logger LOGGER = LogManager.getLogger(Lines.class);

  private static ShaderProgram SHADER;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Set<Line> lines = new HashSet<>();
  private ArrayMesh mesh;
  private boolean linesDirty = false;
  private int color = 0xFFFFFFFF;
  private float lineWidth = 1.0f;

  public Lines(int color, Set<Line> lines) {
    this.color = color;
    this.lines.addAll(lines);
  }

  public Lines(int color, Line... lines) {
    this.color = color;
    Collections.addAll(this.lines, lines);
  }

  public Lines(int color) {
    this.color = color;
  }

  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/Lines.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/Lines.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
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
            PrimitiveType.LINES,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(1, DataType.UNSIGNED_INT, "aColor"));
  }

  private void updateMesh() {
    if (!this.linesDirty) return;
    try {
      this.lock.readLock().lock();
      ByteBuffer vertices = BufferUtils.createByteBuffer(this.lines.size() * 2 * 4 * 4);
      for (Line line : this.lines) {
        vertices
            .putFloat(line.start().x)
            .putFloat(line.start().y)
            .putFloat(line.start().z)
            .putInt(line.color());
        vertices
            .putFloat(line.end().x)
            .putFloat(line.end().y)
            .putFloat(line.end().z)
            .putInt(line.color());
      }
      vertices.flip();
      this.mesh.vertexBuffer(vertices);
      this.linesDirty = false;
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
    float lineWidth = GL33.glGetFloat(GL33.GL_LINE_WIDTH);
    GL33.glLineWidth(this.lineWidth);
    this.mesh.render(camera, shader);
    GL33.glLineWidth(lineWidth);
    shader.unbind();
  }

  public int color() {
    return this.color;
  }

  public Lines color(int color) {
    this.color = color;
    return this;
  }

  public void addLine(Vector3f a, Vector3f b) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(new Line(a, b, this.color));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void addLine(Vector3f a, Vector3f b, int color) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(new Line(a, b, color));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void addLine(Line line) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(line);
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void removeLine(Vector3f a, Vector3f b) {
    try {
      this.lock.writeLock().lock();
      this.lines.remove(new Pair<>(a, b));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void removeLine(Pair<Vector3f, Vector3f> line) {
    try {
      this.lock.writeLock().lock();
      this.lines.remove(line);
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public void clear() {
    if (this.lines.isEmpty()) return;
    try {
      this.lock.writeLock().lock();
      this.lines.clear();
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public float lineWidth() {
    return this.lineWidth;
  }

  public Lines lineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  public record Line(Vector3f start, Vector3f end, int color) {}
}
