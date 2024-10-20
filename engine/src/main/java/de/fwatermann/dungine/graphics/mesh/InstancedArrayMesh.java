package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.nio.ByteBuffer;
import java.util.List;
import org.lwjgl.opengl.GL33;

/**
 * The InstancedArrayMesh class represents a 3D mesh object in the game engine that is rendered
 * using an array of vertices and instance data. It provides methods for manipulating the mesh's
 * position, rotation, and scale, as well as methods for rendering the mesh.
 */
public class InstancedArrayMesh extends InstancedMesh<InstancedArrayMesh> {

  /**
   * Constructs a new InstancedArrayMesh with the specified vertex buffer, instance data, instance
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances to render
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      List<ByteBuffer> instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(
        vertices,
        primitiveType,
        instanceData,
        instanceCount,
        usageHint,
        attributes,
        instanceAttributes);
  }

  /**
   * Constructs a new InstancedArrayMesh with the specified vertex buffer, instance data, instance
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances to render
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(
        vertices,
        primitiveType,
        List.of(instanceData),
        instanceCount,
        usageHint,
        attributes,
        instanceAttributes);
  }

  /**
   * Constructs a new InstancedArrayMesh with the specified vertex buffer, instance data, and
   * instance
   *
   * @param vertices the vertex buffer of the mesh
   * @param primitiveType the primitive type of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances to render
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      ByteBuffer vertices,
      PrimitiveType primitiveType,
      ByteBuffer instanceData,
      int instanceCount,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(
        vertices,
        primitiveType,
        instanceData,
        instanceCount,
        GLUsageHint.DRAW_STATIC,
        attributes,
        instanceAttributes);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.render(camera, shader, 0, this.vertexCount());
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader, int offset, int count) {
    if (shader == null) return;
    if (this.vertices == null || this.instanceCount <= 0) return;
    if (offset < 0) throw new IllegalArgumentException("Offset must be greater than or equal to 0");
    if (count < 0) throw new IllegalArgumentException("Count must be greater than or equal to 0");
    ThreadUtils.checkMainThread();

    this.updateVertexBuffer();
    this.updateInstanceBuffer();

    if (this.lastShader != shader) {
      this.attributes.bindAttribPointers(shader, this.glVAO, this.glVBO);
      this.instanceAttributes.bindAttribPointers(shader, this.glVAO, this.instanceData);
      this.lastShader = shader;
    }

    boolean wasBound = shader.bound();
    if (!wasBound) shader.bind();

    shader.useCamera(camera);
    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawArraysInstanced(this.primitiveType.glType, offset, count, this.instanceCount);
    GL33.glBindVertexArray(0);

    if (!wasBound) shader.unbind();
  }

  @Override
  public void dispose() {
    ThreadUtils.checkMainThread();
    GL33.glDeleteVertexArrays(this.glVAO);
    GL33.glDeleteBuffers(this.glVBO);
    this.instanceData.forEach(buffer -> GL33.glDeleteBuffers(buffer.glIBO));
  }
}
