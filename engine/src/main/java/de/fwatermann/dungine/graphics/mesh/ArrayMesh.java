package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL33;

/**
 * The ArrayMesh class represents a 3D mesh object in the game engine that is rendered using an
 * array of vertices. It provides methods for manipulating the mesh's position, rotation, and scale,
 * as well as methods for rendering the mesh.
 */
public class ArrayMesh extends UnInstancedMesh<ArrayMesh> {

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      GLUsageHint usageHint,
      VertexAttributeList attributes) {
    super(vertices, primitiveType, usageHint, attributes);
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      GLUsageHint usageHint,
      VertexAttribute... attributes) {
    this(vertices, primitiveType, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new ArrayMesh with the specified attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(
      ByteBuffer vertices, PrimitiveType primitiveType, VertexAttributeList attributes) {
    this(vertices, primitiveType, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(
      ByteBuffer vertices, PrimitiveType primitiveType, VertexAttribute... attributes) {
    this(vertices, primitiveType, GLUsageHint.DRAW_STATIC, new VertexAttributeList(attributes));
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (shader == null) return;
    this.render(camera, shader, 0, this.vertexCount());
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader, int offset, int count) {
    if (shader == null) return;
    if (this.vertices == null) return;
    if (offset < 0) throw new IllegalArgumentException("Offset must be greater than or equal to 0");
    if (count < 0) throw new IllegalArgumentException("Count must be greater than or equal to 0");
    ThreadUtils.checkMainThread();

    this.updateVertexBuffer();

    if (this.lastShader != shader) {
      this.attributes.bindAttribPointers(shader, this.glVAO, this.glVBO);
      this.lastShader = shader;
    }

    boolean wasBound = shader.bound();
    if (!wasBound) shader.bind();

    shader.setUniformMatrix4f(shader.configuration().uniformModelMatrix, this.transformMatrix());
    shader.useCamera(camera);
    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawArrays(this.primitiveType.glType, offset, count);
    GL33.glBindVertexArray(0);

    if (!wasBound) shader.unbind();
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
