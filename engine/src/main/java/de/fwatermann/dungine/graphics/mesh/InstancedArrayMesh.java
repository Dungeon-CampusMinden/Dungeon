package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL33;

/**
 * The InstancedArrayMesh class represents a 3D mesh object in the game engine that is rendered using
 * an array of vertices and instance data. It provides methods for manipulating the mesh's position,
 * rotation, and scale, as well as methods for rendering the mesh.
 */
public class InstancedArrayMesh extends InstancedMesh {

  private int glVAO;
  private int glVBO;
  private int glIBO;

  private ShaderProgram lastShaderProgram = null;

  /**
   * Constructs a new InstancedArrayMesh with the specified vertex buffer, instance data, instance
   * @param vertices the vertex buffer of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances to render
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      FloatBuffer vertices,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, instanceData, instanceCount, usageHint, attributes, instanceAttributes);
    this.initGL();
  }

  /**
   * Constructs a new InstancedArrayMesh with the specified vertex buffer, instance data, and instance
   * @param vertices the vertex buffer of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances to render
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      FloatBuffer vertices,
      ByteBuffer instanceData,
      int instanceCount,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(
        vertices,
        instanceData,
        instanceCount,
        GLUsageHint.DRAW_STATIC,
        attributes,
        instanceAttributes);
  }

  /**
   * Constructs a new InstancedArrayMesh with the specified instance data and instance
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    this(null, null, 0, usageHint, attributes, instanceAttributes);
  }

  /**
   * Constructs a new InstancedArrayMesh with the specified instance data and instance
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  public InstancedArrayMesh(
      VertexAttributeList attributes, InstanceAttributeList instanceAttributes) {
    this(GLUsageHint.DRAW_STATIC, attributes, instanceAttributes);
  }

  private void initGL() {
    this.glVAO = GL33.glGenVertexArrays();
    this.glVBO = GL33.glGenBuffers();
    this.glIBO = GL33.glGenBuffers();
    this.updateBuffers();
  }

  private void updateBuffers() {
    if (this.vertices != null && this.verticesDirty) {
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glVBO);
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.vertices, this.usageHint.getGLConstant());
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
      this.verticesDirty = false;
    }
    if (this.instanceData != null && this.instanceDataDirty) {
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glIBO);
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.instanceData, this.usageHint.getGLConstant());
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
      this.instanceDataDirty = false;
    }
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {

    this.updateBuffers();
    if (this.vertices == null || this.instanceCount <= 0) return;

    if (this.lastShaderProgram != shaderProgram) {
      this.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO, this.glIBO);
      this.lastShaderProgram = shaderProgram;
    }

    if (bindShader) {
      shaderProgram.bind();
    }

    GL33.glBindVertexArray(this.glVAO);
    GL33.glDrawArraysInstanced(primitiveType, offset, count, this.instanceCount);
    GL33.glBindVertexArray(0);

    if (bindShader) {
      shaderProgram.unbind();
    }
  }

  @Override
  public void dispose() {
    GL33.glDeleteVertexArrays(this.glVAO);
    GL33.glDeleteBuffers(this.glVBO);
    GL33.glDeleteBuffers(this.glIBO);
  }
}