package de.fwatermann.dungine.graphics;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The `Renderable` class represents an abstract base class for objects that can be rendered in a
 * graphics context. It provides methods for setting and getting the position, scale, and rotation
 * of the object, as well as for rendering the object using a camera and shader program.
 *
 * @param <T> the type of the subclass extending {@link Renderable<T>} used for method chaining
 */
public abstract class Renderable<T extends Renderable<?>> {

  private final Vector3f position;
  private final Vector3f scaling;
  private final Quaternionf rotation;
  protected int order = Integer.MAX_VALUE;

  private final Matrix4f transformationMatrix = new Matrix4f();

  /**
   * Constructs a Renderable object with the specified position, scale, and rotation.
   *
   * @param position the position of the object
   * @param scale the scale of the object
   * @param rotation the rotation of the object
   */
  public Renderable(Vector3f position, Vector3f scale, Quaternionf rotation) {
    this.position = position;
    this.scaling = scale;
    this.rotation = rotation;
    this.calcTransformation();
  }

  /** Constructs a Renderable object with default position, rotation, scale. */
  public Renderable() {
    this(new Vector3f(), new Vector3f(1.0f), new Quaternionf());
  }

  /**
   * Renders the object using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  public abstract void render(Camera<?> camera);

  /**
   * Renders the object using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  public abstract void render(Camera<?> camera, ShaderProgram shader);

  /**
   * Gets the transformation matrix of the object.
   *
   * @return the transformation matrix
   */
  public Matrix4f transformationMatrix() {
    return this.transformationMatrix;
  }

  /** Called when the transformation of the object has changed. */
  protected void transformationChanged() {}

  /**
   * Sets the transformation of the object.
   *
   * @param position the new position of the object
   * @param rotation the new rotation of the object
   * @param scalation the new scale of the object
   * @return this object for method chaining
   */
  public T transformation(Vector3f position, Quaternionf rotation, Vector3f scalation) {
    if (position != null) this.position.set(position);
    if (rotation != null) this.rotation.set(rotation);
    if (scalation != null) this.scaling.set(scalation);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Gets the position of the object.
   *
   * @return the position
   */
  public Vector3f position() {
    return this.position;
  }

  /**
   * Sets the position of the object.
   *
   * @param position the new position
   * @return this object for method chaining
   */
  public T position(Vector3f position) {
    this.position.set(position);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Sets the position of the object.
   *
   * @param x the x-coordinate of the new position
   * @param y the y-coordinate of the new position
   * @param z the z-coordinate of the new position
   * @return this object for method chaining
   */
  public T position(float x, float y, float z) {
    this.position.set(x, y, z);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Translates the object by the specified vector.
   *
   * @param translation the translation vector
   * @return this object for method chaining
   */
  public T translate(Vector3f translation) {
    this.position.add(translation);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Translates the object by the specified coordinates.
   *
   * @param x the x-coordinate of the translation
   * @param y the y-coordinate of the translation
   * @param z the z-coordinate of the translation
   * @return this object for method chaining
   */
  public T translate(float x, float y, float z) {
    this.position.add(x, y, z);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Gets the scale of the object.
   *
   * @return the scale
   */
  public Vector3f scaling() {
    return this.scaling;
  }

  /**
   * Sets the scale of the object.
   *
   * @param scaling the new scale
   * @return this object for method chaining
   */
  public T scaling(Vector3f scaling) {
    this.scaling.set(scaling);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Scales the object by the specified vector.
   *
   * @param scale the scale vector
   * @return this object for method chaining
   */
  public T scale(Vector3f scale) {
    this.scaling.mul(scale);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Scales the object by the specified coordinates.
   *
   * @param x the x-coordinate of the scale
   * @param y the y-coordinate of the scale
   * @param z the z-coordinate of the scale
   * @return this object for method chaining
   */
  public T scale(float x, float y, float z) {
    this.scaling.mul(x, y, z);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Scales the object uniformly by the specified factor.
   *
   * @param s the scale factor
   * @return this object for method chaining
   */
  public T scale(float s) {
    this.scaling.mul(s);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Gets the rotation of the object.
   *
   * @return the rotation
   */
  public Quaternionf rotation() {
    return this.rotation;
  }

  /**
   * Sets the rotation of the object.
   *
   * @param rotation the new rotation
   * @return this object for method chaining
   */
  public T rotation(Quaternionf rotation) {
    this.rotation.set(rotation);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Rotates the object around the specified axis by the specified angle.
   *
   * @param x the x-coordinate of the axis
   * @param y the y-coordinate of the axis
   * @param z the z-coordinate of the axis
   * @param angle the angle of rotation in degrees
   * @return this object for method chaining
   */
  public T rotate(float x, float y, float z, float angle) {
    this.rotation.rotateAxis((float) Math.toRadians(angle), x, y, z);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Rotates the object around the specified axis by the specified angle.
   *
   * @param axis the axis of rotation
   * @param angle the angle of rotation in degrees
   * @return this object for method chaining
   */
  public T rotate(Vector3f axis, float angle) {
    this.rotation.rotateAxis((float) Math.toRadians(angle), axis);
    this.calcTransformation();
    return (T) this;
  }

  /**
   * Gets the transformation matrix of the object.
   *
   * @return the transformation matrix
   */
  public Matrix4f transformMatrix() {
    return this.transformationMatrix;
  }

  /** Calculates the transformation matrix based on the current position, rotation, and scale. */
  private void calcTransformation() {
    this.transformationMatrix
        .identity()
        .translationRotateScale(this.position, this.rotation, this.scaling);
    this.transformationChanged();
  }

  /**
   * Checks if the object should be rendered based on the specified camera frustum.
   *
   * @param frustum the camera frustum
   * @return true if the object should be rendered, false otherwise
   */
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  /**
   * Gets the order of the object for rendering.
   *
   * @return order number
   */
  public int order() {
    return this.order;
  }
}
