package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * The InstancedMesh class represents a 3D mesh object in the game engine that is instanced. It
 * provides methods for manipulating the mesh's position, rotation, and scale, as well as methods for
 * rendering the mesh. This class is abstract and should be extended by specific types of instanced
 * meshes.
 */
public abstract class InstancedMesh extends Mesh {

  protected InstanceAttributeList instanceAttributes;
  protected @Null ByteBuffer instanceData;
  protected boolean instanceDataDirty = false;
  protected int instanceCount = 0;

  /**
   * Constructs a new InstancedMesh with the specified vertex buffer, instance data buffer, instance count, usage hint, and attributes.
   *
   * @param vertices the vertex buffer of the mesh
   * @param instanceData the instance data buffer of the mesh
   * @param instanceCount the number of instances of the mesh
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   * @param instanceAttributes the instance attributes of the mesh
   */
  protected InstancedMesh(
      FloatBuffer vertices,
      ByteBuffer instanceData,
      int instanceCount,
      GLUsageHint usageHint,
      VertexAttributeList attributes,
      InstanceAttributeList instanceAttributes) {
    super(vertices, usageHint, attributes);
    GLUtils.checkBuffer(instanceData);
    this.instanceAttributes = instanceAttributes;
    this.instanceData = instanceData;
    this.instanceCount = instanceCount;
  }

  /**
   * Returns the instance data buffer of the mesh.
   *
   * @return the instance data buffer of the mesh
   */
  public ByteBuffer getInstanceData() {
    return this.instanceData;
  }

  /**
   * Sets the instance data buffer of the mesh.
   *
   * @param buffer the new instance data buffer of the mesh
   */
  public void setInstanceData(ByteBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    this.instanceData = buffer;
    this.instanceDataDirty = true;
  }

  /**
   * Returns the number of instances fo this mesh.
   *
   * @return the number of instances of this mesh
   */
  public int instanceCount() {
    return this.instanceCount;
  }

  /**
   * Sets the number of instances of this mesh. The instance data buffer must be updated after
   * calling this method otherwise the buffer will be out of sync with the instance count and may
   * lead to undefined behavior.
   *
   * @param count the new number of instances of this mesh
   */
  public void setInstanceCount(int count) {
    this.instanceCount = count;
  }

  /**
   * Binds the vertex array object, vertex buffer object, and instance data buffer object of this mesh to
   * @param program the shader program to bind the attributes to
   * @param vao the vertex array object to bind
   * @param vbo the vertex buffer object to bind
   * @param ibo the instance data buffer object to bind
   */
  protected void bindAttribPointers(ShaderProgram program, int vao, int vbo, int ibo) {
    GL33.glBindVertexArray(vao);

    GL33.glBindBuffer(GL33.GL_VERTEX_ARRAY, vbo);
    this.attributes.forEach(
      attrib -> {
        int location = program.getAttributeLocation(attrib.name);
        if (location == -1) return;
        GL33.glEnableVertexAttribArray(location);
        GL33.glVertexAttribPointer(
          location,
          attrib.numComponents,
          attrib.glType,
          false,
          this.attributes.sizeInBytes(),
          attrib.offset());
      });

    GL33.glBindBuffer(GL33.GL_VERTEX_ARRAY, ibo);
    this.instanceAttributes.forEach(
      attrib -> {
        int location = program.getAttributeLocation(attrib.name);
        if (location == -1) return;
        GL33.glEnableVertexAttribArray(location);
        GL33.glVertexAttribPointer(
          location,
          attrib.numComponents,
          attrib.glType,
          false,
          this.instanceAttributes.sizeInBytes(),
          attrib.offset());
        GL33.glVertexAttribDivisor(location, 1);
      });

    GL33.glBindVertexArray(0);
    GL33.glBindBuffer(GL33.GL_VERTEX_ARRAY, 0);
  }

}
