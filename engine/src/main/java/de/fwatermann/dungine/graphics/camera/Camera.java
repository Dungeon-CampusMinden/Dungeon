package de.fwatermann.dungine.graphics.camera;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Abstract base class for a camera system in a 3D environment. This class provides the basic
 * functionality to manipulate the camera's position, orientation, and projection.
 *
 * @param <T> The type of the camera, used for fluent interface style method chaining.
 */
public abstract class Camera<T extends Camera<T>> {

  protected Matrix4f viewMatrix = new Matrix4f();
  protected Matrix4f projectionMatrix = new Matrix4f();
  protected Vector3f up, position, right, front;
  protected Quaternionf rotation = new Quaternionf();

  protected boolean updateOnChange = false;

  /**
   * Constructs a new Camera instance.
   *
   * @param position The initial position of the camera.
   * @param front The initial front vector of the camera, indicating the direction it is facing.
   * @param up The initial up vector of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public Camera(Vector3f position, Vector3f front, Vector3f up, boolean updateOnChange) {
    this.position = position;
    this.front = front;
    this.up = up;
    this.right = this.front.cross(this.up, new Vector3f()).normalize();
    this.updateOnChange = updateOnChange;
  }

  /**
   * Calculates the projection matrix. This method must be implemented by subclasses to define how
   * the camera projects the 3D world onto a 2D surface.
   *
   * @return The projection matrix.
   */
  protected abstract Matrix4f calcProjectionMatrix(Matrix4f projectionMatrix);

  /**
   * Updates the camera's view and projection matrices. This method should be called whenever the
   * camera's state changes and needs to be reflected in the scene rendering.
   */
  public void update() {
    this._update(true);
  }

  /**
   * Internal method to update the camera's view and projection matrices. This method is called by
   * the {@link #update()} method and should not be called directly from outside the
   * class/subclasses.
   *
   * @param force If true, the matrices will be updated regardless of the {@link #updateOnChange}
   *     setting.
   */
  protected final void _update(boolean force) {
    if (!force && !this.updateOnChange) return;
    this.front = new Vector3f(0, 0, -1).rotate(this.rotation);
    this.up = new Vector3f(0, 1, 0).rotate(this.rotation);
    this.right = this.front.cross(this.up, new Vector3f()).normalize();
    this.viewMatrix.setLookAt(
        this.position, this.position.add(this.front, new Vector3f()), this.up);
    this.projectionMatrix = this.calcProjectionMatrix(this.projectionMatrix);
    this.onUpdate();
  }

  /**
   * Called by internal update methods to allow subclasses to perform additional updates when the
   * camera state changes.
   */
  protected abstract void onUpdate();

  /**
   * Returns the projection matrix of the camera. The projection matrix is used to project 3D
   * coordinates into 2D screen coordinates.
   *
   * @return The current projection matrix.
   */
  public Matrix4f projectionMatrix() {
    return this.projectionMatrix;
  }

  /**
   * Returns the view matrix of the camera. The view matrix is used to transform vertices from world
   * space to camera space.
   *
   * @return The current view matrix.
   */
  public Matrix4f viewMatrix() {
    return this.viewMatrix;
  }

  /**
   * Returns the position of the camera in the world space.
   *
   * @return A new {@link Vector3f} instance representing the current position of the camera.
   */
  public Vector3f position() {
    return new Vector3f(this.position);
  }

  /**
   * Returns the front vector of the camera. The front vector indicates the direction the camera is
   * facing.
   *
   * @return A new {@link Vector3f} instance representing the front vector of the camera.
   */
  public Vector3f front() {
    return new Vector3f(this.front);
  }

  /**
   * Returns the up vector of the camera. The up vector indicates the upward direction relative to
   * the camera's orientation.
   *
   * @return A new {@link Vector3f} instance representing the up vector of the camera.
   */
  public Vector3f up() {
    return new Vector3f(this.up);
  }

  /**
   * Returns the right vector of the camera. The right vector is perpendicular to both the camera's
   * front and up vectors, indicating the rightward direction.
   *
   * @return A new {@link Vector3f} instance representing the right vector of the camera.
   */
  public Vector3f right() {
    return new Vector3f(this.right);
  }

  /**
   * Sets the position of the camera.
   *
   * @param position The new position of the camera.
   * @return The camera instance for method chaining.
   */
  public T position(Vector3f position) {
    this.position.set(position);
    this._update(false);
    return (T) this;
  }

  /**
   * Sets the position of the camera using individual components.
   *
   * @param x The X component of the new position.
   * @param y The Y component of the new position.
   * @param z The Z component of the new position.
   * @return The camera instance for method chaining.
   */
  public T position(float x, float y, float z) {
    this.position.set(x, y, z);
    this._update(false);
    return (T) this;
  }

  /**
   * Moves the camera by a relative amount.
   *
   * @param relative The vector by which to move the camera.
   * @return The camera instance for method chaining.
   */
  public T move(Vector3f relative) {
    this.position.add(relative);
    this._update(false);
    return (T) this;
  }

  /**
   * Moves the camera by a specified amount in each direction. This method updates the camera's
   * position based on the given x, y, and z offsets.
   *
   * @param x The offset in the x-direction.
   * @param y The offset in the y-direction.
   * @param z The offset in the z-direction.
   * @return The camera instance for method chaining.
   */
  public T move(float x, float y, float z) {
    this.position.add(x, y, z);
    this._update(false);
    return (T) this;
  }

  /**
   * Orients the camera to look at a specified target point in space. This method updates the
   * camera's front vector to point towards the given target.
   *
   * @param target The target position the camera should look at.
   * @return The camera instance for method chaining.
   */
  public T lookAt(Vector3f target) {
    this.rotation.rotateTo(this.front, target.sub(this.position, new Vector3f()));
    this._update(false);
    return (T) this;
  }

  /**
   * Orients the camera to look at a specified point in space, defined by x, y, and z coordinates.
   * This method updates the camera's front vector to point towards the given coordinates.
   *
   * @param x The x-coordinate of the target point.
   * @param y The y-coordinate of the target point.
   * @param z The z-coordinate of the target point.
   * @return The camera instance for method chaining.
   */
  public T lookAt(float x, float y, float z) {
    this.rotation.rotateTo(this.front, new Vector3f(x, y, z).sub(this.position, new Vector3f()));
    return (T) this;
  }

  /**
   * Rotates the camera by a given quaternion. This method applies a rotation to the camera's
   * current orientation.
   *
   * @param rotation The quaternion representing the rotation to be applied.
   * @return The camera instance for method chaining.
   */
  public T rotate(Quaternionf rotation) {
    this.rotation.mul(rotation);
    this._update(false);
    return (T) this;
  }

  /**
   * Rotates the camera around a specified axis by a given angle. This method applies a rotation
   * around an arbitrary axis.
   *
   * @param angle The angle in radians to rotate.
   * @param axis The axis to rotate around.
   * @return The camera instance for method chaining.
   */
  public T rotate(float angle, Vector3f axis) {
    Vector3f norm = axis.normalize(new Vector3f());
    this.rotation.rotateAxis(angle, norm);
    this._update(false);
    return (T) this;
  }

  /**
   * Sets the rotation of the camera to a given quaternion. This method sets the camera's
   * orientation to the given quaternion.
   *
   * @param rotation The quaternion representing the new rotation of the camera.
   * @return The camera instance for method chaining.
   */
  public T rotation(Quaternionf rotation) {
    this.rotation.set(rotation);
    this._update(false);
    return (T) this;
  }

  /**
   * Rotates the camera around an arbitrary axis by a given angle. This method applies a rotation
   * around an arbitrary axis.
   *
   * @param angle The angle in radians to rotate.
   * @param axis The axis to rotate around.
   * @return The camera instance for method chaining.
   */
  public T rotation(float angle, Vector3f axis) {
    Vector3f norm = axis.normalize(new Vector3f());
    this.rotation.setAngleAxis(angle, norm.x, norm.y, norm.z);
    this._update(false);
    return (T) this;
  }

  /**
   * Returns the rotation of the camera as a quaternion. The quaternion represents the camera's
   * orientation in space.
   *
   * @return A new {@link Quaternionf} instance representing the rotation of the camera.
   */
  public Quaternionf rotation() {
    return new Quaternionf(this.rotation);
  }

  /**
   * Rotates the camera around its right vector by a given angle. This method applies a pitch
   * rotation to the camera.
   *
   * @param angle The angle in radians to pitch.
   * @return The camera instance for method chaining.
   */
  public T pitch(float angle) {
    this.rotation.rotateAxis(angle, this.right);
    this._update(false);
    return (T) this;
  }

  /**
   * Returns the current pitch angle of the camera. The pitch angle is calculated based on the
   * camera's orientation.
   *
   * @return The pitch angle in radians.
   */
  public float pitch() {
    return this.rotation.getEulerAnglesXYZ(new Vector3f()).x;
  }

  /**
   * Rotates the camera around its up vector by a given angle. This method applies a yaw rotation to
   * the camera.
   *
   * @param angle The angle in radians to yaw.
   * @return The camera instance for method chaining.
   */
  public T yaw(float angle) {
    this.rotation.rotateAxis(angle, this.up);
    this._update(false);
    return (T) this;
  }

  /**
   * Returns the yaw angle of the camera. The yaw angle represents the rotation around the up
   * vector, affecting the left and right orientation.
   *
   * @return The yaw angle in radians.
   */
  public float yaw() {
    return this.rotation.getEulerAnglesXYZ(new Vector3f()).y;
  }

  /**
   * Rotates the camera around its front vector by a given angle. This method applies a roll
   * rotation to the camera, affecting its tilt.
   *
   * @param angle The angle in radians to roll.
   * @return The camera instance for method chaining.
   */
  public T roll(float angle) {
    this.rotation.rotateAxis(angle, this.front);
    this._update(false);
    return (T) this;
  }

  /**
   * Returns the roll angle of the camera. The roll angle represents the rotation around the front
   * vector, affecting the camera's tilt.
   *
   * @return The roll angle in radians.
   */
  public float roll() {
    return this.rotation.getEulerAnglesXYZ(new Vector3f()).z;
  }

  /**
   * Returns whether the camera will automatically update its view matrix when its state changes.
   *
   * <p>Note: If this value is set to true, the camera will update its matrices on every change.
   * This may be computationally expensive, especially if the camera is updated frequently. It is
   * recommended to set this value to false and manually call the {@link #update()} method when
   * necessary (e.g. at the end/beginning of a frame).
   *
   * @return true if the camera will update on change, false otherwise.
   */
  public boolean updateOnChange() {
    return this.updateOnChange;
  }

  /**
   * Sets whether the camera should automatically update its view matrix when its state changes.
   *
   * <p>Note: If this value is set to true, the camera will update its matrices on every change.
   * This may be computationally expensive, especially if the camera is updated frequently. It is
   * recommended to set this value to false and manually call the {@link #update()} method when
   * necessary (e.g. at the end/beginning of a frame).
   *
   * @param updateOnChange true to enable automatic updates, false to disable.
   * @return The camera instance for method chaining.
   */
  public T updateOnChange(boolean updateOnChange) {
    this.updateOnChange = updateOnChange;
    return (T) this;
  }
}
