package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.utils.BoundingBox;
import de.fwatermann.dungine.utils.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a mesh that is not instanced, providing basic transformation capabilities such as
 * translation, rotation, and scaling. This abstract class serves as a foundation for meshes
 * that are manipulated individually rather than as part of an instanced group.
 */
public abstract class UnInstancedMesh extends Mesh {

  protected Vector3f translation = new Vector3f(0, 0, 0);
  protected Vector3f scale = new Vector3f(1, 1, 1);
  protected Quaternionf rotation = new Quaternionf();
  protected Matrix4f transformMatrix = new Matrix4f();

  @Nullable protected BoundingBox boundingBox;

  /**
   * Constructs a new UnInstancedMesh with the specified usage hint and attributes.
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected UnInstancedMesh(GLUsageHint usageHint, VertexAttributeList attributes) {
    super(usageHint, attributes);
  }

  /**
   * Constructs a new UnInstancedMesh with the specified usage hint and attributes.
   * @param usageHint the usage hint of the mesh
   * @param attributes the attributes of the mesh
   */
  protected UnInstancedMesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    super(usageHint, attributes);
  }

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
  private void calcBoundingBox() {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float minZ = Float.MAX_VALUE;
    float maxX = Float.MIN_VALUE;
    float maxY = Float.MIN_VALUE;
    float maxZ = Float.MIN_VALUE;
    this.vertices.position(0);
    int vertexCount = this.vertices.capacity() / this.attributes.sizeInBytes();
    for (int i = 0; i < vertexCount; i++) {
      float x = this.vertices.get();
      float y = this.vertices.get();
      float z = this.vertices.get();
      this.vertices.position(
        this.vertices.position() + this.attributes.sizeInBytes() - 3 * Float.BYTES);
      minX = Math.min(minX, x);
      minY = Math.min(minY, y);
      minZ = Math.min(minZ, z);
      maxX = Math.max(maxX, x);
      maxY = Math.max(maxY, y);
      maxZ = Math.max(maxZ, z);
    }
    this.boundingBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
  }

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
    this.rotation.rotateAxis(angle, x, y, z);
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
  private void calcTransformMatrix() {
    this.transformMatrix =
      new Matrix4f().translationRotateScale(this.translation, this.rotation, this.scale);
  }

}
