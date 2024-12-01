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

/**
 * The `Lines` class represents a collection of lines that can be rendered in a 3D space. It extends
 * the `Renderable` class and provides methods to add, remove, and render lines.
 */
public class Lines extends Renderable<Lines> {

  private static final Logger LOGGER = LogManager.getLogger(Lines.class);

  private static ShaderProgram SHADER;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Set<Line> lines = new HashSet<>();
  private ArrayMesh mesh;
  private boolean linesDirty = false;
  private int color = 0xFFFFFFFF;
  private float lineWidth = 1.0f;

  /**
   * Constructs a new `Lines` instance with the specified color and set of lines.
   *
   * @param color the color of the lines
   * @param lines the set of lines to be added
   */
  public Lines(int color, Set<Line> lines) {
    this.color = color;
    this.lines.addAll(lines);
  }

  /**
   * Constructs a new `Lines` instance with the specified color and array of lines.
   *
   * @param color the color of the lines
   * @param lines the array of lines to be added
   */
  public Lines(int color, Line... lines) {
    this.color = color;
    Collections.addAll(this.lines, lines);
  }

  /**
   * Constructs a new `Lines` instance with the specified color.
   *
   * @param color the color of the lines
   */
  public Lines(int color) {
    this.color = color;
  }

  /** Initializes the shader program for rendering lines. */
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

  /** Initializes the mesh for rendering lines. */
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

  /** Updates the mesh with the current set of lines. */
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

  /**
   * Renders the lines using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    initShader();
    this.render(camera, SHADER);
  }

  /**
   * Renders the lines using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
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

  /**
   * Gets the color of the lines.
   *
   * @return the color of the lines
   */
  public int color() {
    return this.color;
  }

  /**
   * Sets the color of the lines.
   *
   * @param color the color to set
   * @return the updated `Lines` instance
   */
  public Lines color(int color) {
    this.color = color;
    return this;
  }

  /**
   * Adds a line to the set of lines.
   *
   * @param a the start point of the line
   * @param b the end point of the line
   */
  public void addLine(Vector3f a, Vector3f b) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(new Line(a, b, this.color));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Adds a line with a specified color to the set of lines.
   *
   * @param a the start point of the line
   * @param b the end point of the line
   * @param color the color of the line
   */
  public void addLine(Vector3f a, Vector3f b, int color) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(new Line(a, b, color));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Adds a line to the set of lines.
   *
   * @param line the line to add
   */
  public void addLine(Line line) {
    try {
      this.lock.writeLock().lock();
      this.lines.add(line);
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Removes a line from the set of lines.
   *
   * @param a the start point of the line
   * @param b the end point of the line
   */
  public void removeLine(Vector3f a, Vector3f b) {
    try {
      this.lock.writeLock().lock();
      this.lines.remove(new Pair<>(a, b));
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Removes a line from the set of lines.
   *
   * @param line the line to remove
   */
  public void removeLine(Pair<Vector3f, Vector3f> line) {
    try {
      this.lock.writeLock().lock();
      this.lines.remove(line);
      this.linesDirty = true;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /** Clears all lines from the set of lines. */
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

  /**
   * Gets the width of the lines.
   *
   * @return the width of the lines
   */
  public float lineWidth() {
    return this.lineWidth;
  }

  /**
   * Sets the width of the lines.
   *
   * @param lineWidth the width to set
   * @return the updated `Lines` instance
   */
  public Lines lineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  /**
   * Determines whether the lines should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum
   * @return true if the lines should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  /**
   * The `Line` record represents a line with a start point, end point, and color.
   *
   * @param start the start point of the line
   * @param end the end point of the line
   * @param color the color of the line
   */
  public record Line(Vector3f start, Vector3f end, int color) {}
}
