package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Mesh class represents a 3D mesh object in the game engine. It provides methods for
 * manipulating the mesh's position, rotation, and scale, as well as methods for rendering the mesh.
 * This class is abstract, and should be extended by specific types of meshes.
 */
public abstract class Mesh implements Disposable {

  protected VertexAttributeList attributes;
  protected Vector3f translation = new Vector3f(0, 0, 0);
  protected Vector3f scale = new Vector3f(1, 1, 1);
  protected Quaternionf rotation = new Quaternionf();
  protected Matrix4f transformMatrix = new Matrix4f();

  @Nullable protected BoundingBox boundingBox;

  protected GLUsageHint usageHint = GLUsageHint.DRAW_STATIC;

  public Mesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    this.attributes = new VertexAttributeList(attributes);
    this.usageHint = usageHint;
  }

  public Mesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    this.attributes = attributes;
    this.usageHint = usageHint;
  }

  /**
   * Returns the number of vertices in the mesh.
   *
   * @return the number of vertices in the mesh
   */
  public abstract int getVertexCount();

  /**
   * Returns the vertex buffer of the mesh.
   *
   * @return the vertex buffer of the mesh
   */
  public abstract FloatBuffer getVertexBuffer();

  /**
   * Sets the vertex buffer of the mesh to the specified buffer.
   *
   * @param buffer the new vertex buffer of the mesh
   */
  public abstract void setVertexBuffer(FloatBuffer buffer);

  /**
   * Returns the bounding box of the mesh.
   *
   * @return the bounding box of the mesh
   */
  @Nullable
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  /** Calculates the bounding box of the mesh. */
  protected abstract void calcBoundingBox();

  /**
   * Translates the mesh by the specified amounts along the x, y, and z axes.
   *
   * @param x the amount to translate along the x axis
   * @param y the amount to translate along the y axis
   * @param z the amount to translate along the z axis
   */
  public void translate(float x, float y, float z) {
    this.translation.add(x, y, z);
    this.calcTransformMatrix();
  }

  /**
   * Sets the position of the mesh to the specified coordinates.
   *
   * @param x the x coordinate of the new position
   * @param y the y coordinate of the new position
   * @param z the z coordinate of the new position
   */
  public void setTranslation(float x, float y, float z) {
    this.translation = new Vector3f(x, y, z);
    this.calcTransformMatrix();
  }

  /**
   * Rotates the mesh around the specified axis by the specified angle.
   *
   * @param x the x coordinate of the axis of rotation
   * @param y the y coordinate of the axis of rotation
   * @param z the z coordinate of the axis of rotation
   * @param angle the angle of rotation, in degrees
   */
  public void rotate(float x, float y, float z, float angle) {
    this.rotation.rotateAxis(x, y, z, angle);
    this.calcTransformMatrix();
  }

  /**
   * Scales the mesh by the specified factors along the x, y, and z axes.
   *
   * @param x the scaling factor along the x axis
   * @param y the scaling factor along the y axis
   * @param z the scaling factor along the z axis
   */
  public void scale(float x, float y, float z) {
    this.scale.mul(x, y, z);
    this.calcTransformMatrix();
  }

  /**
   * Sets the scale of the mesh to the specified values.
   *
   * @param x the new scale along the x axis
   * @param y the new scale along the y axis
   * @param z the new scale along the z axis
   */
  public void setScale(float x, float y, float z) {
    this.scale = new Vector3f(x, y, z);
    this.calcTransformMatrix();
  }

  /**
   * Translates the mesh by the specified vector.
   *
   * @param translation the vector by which to translate the mesh
   */
  public void translate(Vector3f translation) {
    this.translate(translation.x, translation.y, translation.z);
    this.calcTransformMatrix();
  }

  /**
   * Sets the position of the mesh to the specified vector.
   *
   * @param translation the new position of the mesh
   */
  public void setTranslation(Vector3f translation) {
    this.setTranslation(translation.x, translation.y, translation.z);
    this.calcTransformMatrix();
  }

  /**
   * Rotates the mesh around the specified axis by the specified angle.
   *
   * @param axis the axis of rotation
   * @param angle the angle of rotation, in degrees
   */
  public void rotate(Vector3f axis, float angle) {
    this.rotate(axis.x, axis.y, axis.z, angle);
    this.calcTransformMatrix();
  }

  /**
   * Scales the mesh by the specified vector.
   *
   * @param scale the scaling factors along the x, y, and z axes
   */
  public void scale(Vector3f scale) {
    this.scale(scale.x, scale.y, scale.z);
    this.calcTransformMatrix();
  }

  /**
   * Sets the scale of the mesh to the specified vector.
   *
   * @param scale the new scale along the x, y, and z axes
   */
  public void setScale(Vector3f scale) {
    this.setScale(scale.x, scale.y, scale.z);
    this.calcTransformMatrix();
  }

  /**
   * Sets the transformation matrix of the mesh to the specified matrix.
   *
   * @param matrix the new transformation matrix of the mesh
   */
  public void transformMatrix(Matrix4f matrix) {
    this.transformMatrix = matrix;
  }

  /**
   * Returns the transformation matrix of the mesh.
   *
   * @return the transformation matrix of the mesh
   */
  public Matrix4f transformMatrix() {
    return this.transformMatrix;
  }

  /**
   * Calculates the transformation matrix of the mesh. This method is abstract and must be
   * implemented by subclasses to define how the transformation matrix is updated. The
   * transformation matrix is typically used to apply transformations such as translation, rotation,
   * and scaling to the mesh.
   */
  protected abstract void calcTransformMatrix();

  /**
   * Returns the usage hint of the mesh.
   *
   * @return the usage hint of the mesh
   */
  public GLUsageHint usageHint() {
    return this.usageHint;
  }

  /**
   * Renders the mesh using the specified shader program and primitive type.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   */
  public void render(ShaderProgram shaderProgram, int primitiveType) {
    this.render(shaderProgram, primitiveType, 0, this.getVertexCount(), true);
  }

  /**
   * Renders a portion of the mesh using the specified shader program and primitive type.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   * @param offset the index of the first vertex to render
   * @param count the number of vertices to render
   */
  public void render(ShaderProgram shaderProgram, int primitiveType, int offset, int count) {
    this.render(shaderProgram, primitiveType, offset, count, true);
  }

  /**
   * Renders a portion of the mesh using the specified shader program and primitive type, with an
   * option to bind the shader.
   *
   * @param shaderProgram the shader program to use for rendering
   * @param primitiveType the primitive type to use for rendering
   * @param offset the index of the first vertex to render
   * @param count the number of vertices to render
   * @param bindShader whether to bind the shader before rendering
   */
  public abstract void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader);
}
