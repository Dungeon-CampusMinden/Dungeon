package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL33;

/**
 * The ArrayMesh class represents a 3D mesh object in the game engine that is rendered using an
 * array of vertices. It provides methods for manipulating the mesh's position, rotation, and scale,
 * as well as methods for rendering the mesh.
 */
public class ArrayMesh extends UnInstancedMesh {

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(ByteBuffer vertices, GLUsageHint usageHint, VertexAttributeList attributes) {
    super(vertices, usageHint, attributes);
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(ByteBuffer vertices, GLUsageHint usageHint, VertexAttribute... attributes) {
    this(vertices, usageHint, new VertexAttributeList(attributes));
  }

  /**
   * Constructs a new ArrayMesh with the specified attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(ByteBuffer vertices, VertexAttributeList attributes) {
    this(vertices, GLUsageHint.DRAW_STATIC, attributes);
  }

  /**
   * Constructs a new ArrayMesh with the specified vertex buffer and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param attributes the attributes of the mesh
   */
  public ArrayMesh(ByteBuffer vertices, VertexAttribute... attributes) {
    this(vertices, GLUsageHint.DRAW_STATIC, new VertexAttributeList(attributes));
  }

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {

    if (this.vertices == null) {
      return;
    }
    this.updateVertexBuffer();

    if (bindShader) {
      shaderProgram.bind();
    }
    shaderProgram.setUniformMatrix4f(
        shaderProgram.configuration().uniformModelMatrix, this.transformMatrix);

    GL33.glBindVertexArray(this.glVAO);

    if (this.lastShaderProgram != shaderProgram) {
      this.attributes.bindAttribPointers(shaderProgram, this.glVAO, this.glVBO);
      this.lastShaderProgram = shaderProgram;
    }

    GL33.glDrawArrays(primitiveType, offset, count);
    GL33.glBindVertexArray(0);

    if (bindShader) {
      shaderProgram.unbind();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
