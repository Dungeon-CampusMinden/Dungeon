package de.fwatermann.dungine.graphics.camera;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * Abstract base class for a camera system in a 3D environment. This class provides the basic
 * functionality to manipulate the camera's position, orientation, and projection.
 *
 * @param <T> The type of the camera, used for fluent interface style method chaining.
 */
public abstract class Camera<T extends Camera<T>> {

  private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);

  /** The view matrix of the camera. */
  protected Matrix4f viewMatrix = new Matrix4f();

  /** The projection matrix of the camera. */
  protected Matrix4f projectionMatrix = new Matrix4f();

  /** The view-projection matrix of the camera. */
  protected Matrix4f viewProjectionMatrix = new Matrix4f();

  /** The inverse view-projection matrix of the camera. */
  protected Matrix4f viewProjectionMatrixInv = new Matrix4f();

  /** The front vector of the camera. */
  protected Vector3f front;

  /** The up vector of the camera. */
  protected Vector3f up;

  /** The right vector of the camera. */
  protected Vector3f right;

  /** The position of the camera in the world space. */
  protected Vector3f position;

  /** The viewport of the camera. */
  protected CameraViewport viewport;

  /** If true, the camera will automatically update its view matrix when its state changes. */
  protected boolean updateOnChange;

  /**
   * Constructs a new Camera instance.
   *
   * @param position The initial position of the camera.
   * @param viewport The viewport of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public Camera(Vector3f position, CameraViewport viewport, boolean updateOnChange) {
    this.position = position;
    this.viewport = viewport;
    this.front = new Vector3f(0, 0, -1);
    this.up = new Vector3f(0, 1, 0);
    this.right = this.front.cross(this.up, new Vector3f()).normalize();
    this.updateOnChange = updateOnChange;
    this.updateMatrices(false);
  }

  /**
   * Calculates the projection matrix. This method must be implemented by subclasses to define how
   * the camera projects the 3D world onto a 2D surface.
   *
   * @param projectionMatrix The projection matrix to be calculated. This matrix may contain the old
   *     projection matrix.
   * @return The projection matrix.
   */
  protected abstract Matrix4f calcProjectionMatrix(Matrix4f projectionMatrix);

  /**
   * Updates the camera's view and projection matrices. This method should be called whenever the
   * camera's state changes and needs to be reflected in the scene rendering.
   */
  public void update() {
    this.updateMatrices(true);
  }

  /**
   * Internal method to update the camera's view and projection matrices. This method is called by
   * the {@link #update()} method and should not be called directly from outside the
   * class/subclasses.
   *
   * @param force If true, the matrices will be updated regardless of the {@link #updateOnChange}
   *     setting.
   */
  protected void updateMatrices(boolean force) {
    if (!force && !this.updateOnChange) return;
    this.viewMatrix.setLookAt(
        this.position, this.position.add(this.front, new Vector3f()), this.up);
    this.projectionMatrix = this.calcProjectionMatrix(this.projectionMatrix);
    this.viewProjectionMatrix.set(this.projectionMatrix).mul(this.viewMatrix);
    this.viewProjectionMatrixInv.set(this.viewProjectionMatrix).invert();
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
    this.updateMatrices(false);
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
    this.updateMatrices(false);
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
    this.updateMatrices(false);
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
    this.updateMatrices(false);
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
    this.front.set(target).sub(this.position).normalize();
    this.right.set(this.front).cross(WORLD_UP).normalize();
    this.up.set(this.right).cross(this.front).normalize();

    this.updateMatrices(false);
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
    return this.lookAt(new Vector3f(x, y, z));
  }

  /**
   * Rotates the camera by a given quaternion. This method applies a rotation to the camera's
   * current orientation.
   *
   * @param rotation The quaternion representing the rotation to be applied.
   * @return The camera instance for method chaining.
   */
  public T rotate(Quaternionf rotation) {
    this.front.rotate(rotation).normalize();
    this.right.set(this.front).cross(WORLD_UP).normalize();
    this.up.set(this.right).cross(this.front).normalize();

    this.updateMatrices(false);
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
    axis.normalize();
    this.front.rotateAxis(angle, axis.x, axis.y, axis.z).normalize();
    this.right.set(this.front).cross(WORLD_UP).normalize();
    this.up.set(this.right).cross(this.front).normalize();
    this.updateMatrices(false);
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
    this.front.set(0, 0, -1).rotate(rotation).normalize();
    this.right.set(this.front).cross(WORLD_UP).normalize();
    this.up.set(this.right).cross(this.front).normalize();
    this.updateMatrices(false);
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
    axis.normalize();
    this.front.set(0, 0, -1).rotateAxis(angle, axis.x, axis.y, axis.z).normalize();
    this.right.set(this.front).cross(WORLD_UP).normalize();
    this.up.set(this.right).cross(this.front).normalize();
    this.updateMatrices(false);
    return (T) this;
  }

  /**
   * Returns the rotation of the camera as a quaternion. The quaternion represents the camera's
   * orientation in 3D space.
   *
   * @return The current rotation of the camera.
   */
  public Quaternionf rotation() {
    return new Quaternionf().rotationTo(new Vector3f(0, 0, -1), this.front);
  }

  /**
   * Rotates the camera around its right vector by a given angle. This method applies a pitch
   * rotation to the camera.
   *
   * @param angle The angle in radians to pitch.
   * @return The camera instance for method chaining.
   */
  public T pitch(float angle) {
    this.rotate(angle, this.right);
    return (T) this;
  }

  /**
   * Rotates the camera around its right vector by a given angle. This method applies a pitch
   * rotation to the camera.
   *
   * @param angle The angle in degrees to pitch.
   * @return The camera instance for method chaining.
   */
  public T pitchDeg(float angle) {
    return this.pitch(Math.toRadians(angle));
  }

  /**
   * Returns the current pitch angle of the camera. The pitch angle is calculated based on the
   * camera's orientation.
   *
   * @return The pitch angle in radians.
   */
  public float pitch() {
    return Math.asin(this.front.y);
  }

  /**
   * Rotates the camera around its up vector by a given angle. This method applies a yaw rotation to
   * the camera.
   *
   * @param angle The angle in radians to yaw.
   * @return The camera instance for method chaining.
   */
  public T yaw(float angle) {
    this.rotate(angle, WORLD_UP);
    return (T) this;
  }

  /**
   * Rotates the camera around its up vector by a given angle. This method applies a yaw rotation to
   * the camera.
   *
   * @param angle The angle in degrees to yaw.
   * @return The camera instance for method chaining.
   */
  public T yawDeg(float angle) {
    return this.yaw(Math.toRadians(angle));
  }

  /**
   * Returns the yaw angle of the camera. The yaw angle represents the rotation around the up
   * vector, affecting the left and right orientation.
   *
   * @return The yaw angle in radians.
   */
  public float yaw() {
    return Math.atan2(this.front.x, this.front.z);
  }

  /**
   * Rotates the camera around its front vector by a given angle. This method applies a roll
   * rotation to the camera, affecting its tilt.
   *
   * @param angle The angle in radians to roll.
   * @return The camera instance for method chaining.
   */
  public T roll(float angle) {
    this.rotate(angle, this.front);
    return (T) this;
  }

  /**
   * Rotates the camera around its front vector by a given angle. This method applies a roll
   * rotation to the camera, affecting its tilt.
   *
   * @param angle The angle in degrees to roll.
   * @return The camera instance for method chaining.
   */
  public T rollDeg(float angle) {
    return this.roll(Math.toRadians(angle));
  }

  /**
   * Returns the roll angle of the camera. The roll angle represents the rotation around the front
   * vector, affecting the camera's tilt.
   *
   * @return The roll angle in radians.
   */
  public float roll() {
    return Math.atan2(this.right.y, this.up.y);
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

  /**
   * Returns the current viewport of the camera.
   *
   * @return The current {@link CameraViewport} instance.
   */
  public CameraViewport viewport() {
    return this.viewport;
  }

  /**
   * Sets the viewport of the camera.
   *
   * @param viewport The new {@link CameraViewport} to be set.
   * @return The camera instance for method chaining.
   */
  public T viewport(CameraViewport viewport) {
    this.viewport = viewport;
    this.updateOnChange(false);
    return (T) this;
  }

  /**
   * Updates the viewport dimensions and offsets.
   *
   * @param width The new width of the viewport.
   * @param height The new height of the viewport.
   * @param offsetX The new X offset of the viewport.
   * @param offsetY The new Y offset of the viewport.
   * @return The camera instance for method chaining.
   */
  public T updateViewport(int width, int height, int offsetX, int offsetY) {
    this.viewport.set(width, height, offsetX, offsetY);
    this.updateOnChange(false);
    return (T) this;
  }

  /**
   * Projects 3D world coordinates to 2D screen coordinates.
   *
   * @param x The X coordinate in the world space.
   * @param y The Y coordinate in the world space.
   * @param z The Z coordinate in the world space.
   * @return A new {@link Vector3f} instance representing the projected 2D screen coordinates.
   */
  public Vector3f project(float x, float y, float z) {
    return this.viewProjectionMatrix.project(
        x,
        y,
        z,
        new int[] {
          (int) Math.floor(this.viewport.offsetX()),
          (int) Math.floor(this.viewport.offsetY()),
          (int) Math.floor(this.viewport.width()),
          (int) Math.floor(this.viewport.height())
        },
        new Vector3f());
  }

  /**
   * Projects 3D world coordinates to 2D screen coordinates.
   *
   * @param worldCoords The {@link Vector3f} instance representing the world coordinates.
   * @return A new {@link Vector3f} instance representing the projected 2D screen coordinates.
   */
  public Vector3f project(Vector3f worldCoords) {
    return this.project(worldCoords.x, worldCoords.y, worldCoords.z);
  }

  /**
   * Unprojects 2D screen coordinates to 3D world coordinates.
   *
   * @param x The X coordinate on the screen.
   * @param y The Y coordinate on the screen.
   * @return A new {@link Vector3f} instance representing the unprojected 3D world coordinates.
   */
  public Vector3f unproject(int x, int y) {
    return this.viewProjectionMatrixInv.unprojectInv(
        x,
        y,
        0,
        new int[] {
          (int) this.viewport.offsetX(),
          (int) this.viewport.offsetY(),
          (int) this.viewport.width(),
          (int) this.viewport.height()
        },
        new Vector3f());
  }

  /**
   * Unprojects 2D screen coordinates to 3D world coordinates.
   *
   * @param screenCoords The {@link Vector2i} instance representing the screen coordinates.
   * @return A new {@link Vector3f} instance representing the unprojected 3D world coordinates.
   */
  public Vector3f unproject(Vector2i screenCoords) {
    return this.unproject(screenCoords.x, screenCoords.y);
  }

  /**
   * Calculates the direction of a ray from the camera through the specified screen coordinates.
   *
   * @param x The x-coordinate on the screen.
   * @param y The y-coordinate on the screen.
   * @return A new {@link Vector3f} instance representing the direction of the ray.
   */
  public Vector3f raycast(int x, int y) {
    return this.projectionMatrix.frustumRayDir(
        x / this.viewport.width(), y / this.viewport.height(), new Vector3f());
  }

  /**
   * Calculates the direction of a ray from the camera through the specified screen coordinates.
   *
   * @param screenCoords The {@link Vector2i} instance representing the screen coordinates.
   * @return A new {@link Vector3f} instance representing the direction of the ray.
   */
  public Vector3f raycast(Vector2i screenCoords) {
    return this.raycast(screenCoords.x, screenCoords.y);
  }
}
