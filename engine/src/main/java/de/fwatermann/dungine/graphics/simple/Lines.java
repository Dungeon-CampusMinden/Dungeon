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
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.Pair;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Lines extends Renderable<Lines> {

  private static ShaderProgram SHADER;
  private ArrayMesh mesh;
  private final Set<Pair<Vector3f, Vector3f>> lines = new HashSet<>();
  private boolean linesDirty = false;
  private int color = 0xFFFFFFFF;

  public Lines(int color, Set<Pair<Vector3f, Vector3f>> lines) {
    this.color = color;
    this.lines.addAll(lines);
  }

  @SafeVarargs
  public Lines(int color, Pair<Vector3f, Vector3f> ... lines) {
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
    if(this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.LINES,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"));
  }

  private void updateMesh() {
    if(!this.linesDirty) return;
    ByteBuffer vertices;
    if(this.mesh.vertexBuffer() == null || this.mesh.vertexBuffer().capacity() < this.lines.size() * 2 * 3 * 4) {
      vertices = BufferUtils.createByteBuffer(this.lines.size() * 2 * 3 * 4);
    } else {
      vertices = this.mesh.vertexBuffer();
    }
    vertices.clear();
    FloatBuffer floatView = vertices.asFloatBuffer();
    for(Pair<Vector3f, Vector3f> line : this.lines) {
      floatView.put(line.a().x).put(line.a().y).put(line.a().z);
      floatView.put(line.b().x).put(line.b().y).put(line.b().z);
    }
    this.mesh.vertexBuffer(vertices);
    this.linesDirty = false;
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
    this.mesh.render(camera, shader);
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
    this.lines.add(new Pair<>(a, b));
    this.linesDirty = true;
  }

  public void addLine(Pair<Vector3f, Vector3f> line) {
    this.lines.add(line);
    this.linesDirty = true;
  }

  public void removeLine(Vector3f a, Vector3f b) {
    this.lines.remove(new Pair<>(a, b));
    this.linesDirty = true;
  }

  public void removeLine(Pair<Vector3f, Vector3f> line) {
    this.lines.remove(line);
    this.linesDirty = true;
  }

  public void clear() {
    if(this.lines.isEmpty()) return;
    this.lines.clear();
    this.linesDirty = true;
  }

}
