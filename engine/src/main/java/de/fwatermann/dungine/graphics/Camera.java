package de.fwatermann.dungine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a camera in a 3D space, allowing for movement and orientation adjustments. The
 * camera's view and projection matrices can be updated based on its position, orientation, and
 * perspective settings.
 */
public class Camera {

  private Matrix4f viewMatrix = new Matrix4f();
  private Matrix4f projectionMatrix = new Matrix4f();
  private Vector3f up, position, right, front;
  private float yaw, pitch;
  private float fov, aspectRatio, near, far;

  /**
   * Constructs a Camera with specified perspective settings.
   *
   * @param fov The field of view angle in degrees.
   * @param aspectRatio The aspect ratio (width / height) of the camera.
   * @param near The distance to the near clipping plane.
   * @param far The distance to the far clipping plane.
   */
  public Camera(float fov, float aspectRatio, float near, float far) {
    this.fov = fov;
    this.aspectRatio = aspectRatio;
    this.near = near;
    this.far = far;

    this.front = new Vector3f(0.0f, 0.0f, 1.0f).normalize();
    this.right = this.front.cross(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f()).normalize();
    this.position = new Vector3f(0.0f, 0.0f, 0.0f);
    this.up = this.right.cross(this.front, new Vector3f()).normalize();

    this.update();
  }

  /**
   * Updates the camera's view and projection matrices based on its current position and
   * orientation.
   */
  public void update() {
    this.front.x =
        (float) (Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));
    this.front.y = (float) Math.sin(Math.toRadians(this.pitch));
    this.front.z =
        (float) (Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));

    this.front = this.front.normalize();
    this.right = this.front.cross(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f()).normalize();
    this.up = this.right.cross(this.front, new Vector3f()).normalize();

    this.viewMatrix =
        new Matrix4f()
            .lookAt(this.position, this.position.add(this.front, new Vector3f()), this.up);
    this.projectionMatrix =
        new Matrix4f()
            .perspective((float) Math.toRadians(this.fov), this.aspectRatio, this.near, this.far);
  }

  private float clampAngle(float angle) {
    return ((int) (angle * 100) % 36000) / 100.0f;
  }

  /**
   * Get the view matrix of the camera.
   *
   * @return the view matrix
   */
  public Matrix4f viewMatrix() {
    return this.viewMatrix;
  }

  /**
   * Set the view matrix of the camera.
   *
   * @param viewMatrix the new view matrix
   * @return this camera
   */
  public Camera viewMatrix(Matrix4f viewMatrix) {
    this.viewMatrix = viewMatrix;
    return this;
  }

  /**
   * Get the projection matrix of the camera.
   *
   * @return the projection matrix
   */
  public Matrix4f projectionMatrix() {
    return this.projectionMatrix;
  }

  /**
   * Set the projection matrix of the camera.
   *
   * @param projectionMatrix the new projection matrix
   * @return this camera
   */
  public Camera projectionMatrix(Matrix4f projectionMatrix) {
    this.projectionMatrix = projectionMatrix;
    return this;
  }

  /**
   * Get the up vector of the camera.
   *
   * @return the up vector
   */
  public Vector3f up() {
    return this.up;
  }

  /**
   * Set the up vector of the camera.
   *
   * @param up the new up vector
   * @return this camera
   */
  public Camera up(Vector3f up) {
    this.up = up;
    this.update();
    return this;
  }

  /**
   * Get the position of the camera.
   *
   * @return the position
   */
  public Vector3f position() {
    return this.position;
  }

  /**
   * Set the position of the camera.
   *
   * @param position the new position
   * @return this camera
   */
  public Camera position(Vector3f position) {
    this.position = position;
    this.update();
    return this;
  }

  /**
   * Move the camera by a relative amount.
   *
   * @param relative the relative amount to move the camera
   * @return this camera
   */
  public Camera move(Vector3f relative) {
    this.position.add(relative);
    this.update();
    return this;
  }

  /**
   * Move the camera by a relative amount.
   *
   * @param x the amount to move the camera along the x-axis
   * @param y the amount to move the camera along the y-axis
   * @param z the amount to move the camera along the z-axis
   * @return this camera
   */
  public Camera move(float x, float y, float z) {
    this.position.add(x, y, z);
    this.update();
    return this;
  }

  /**
   * Get the right vector of the camera.
   *
   * @return the right vector
   */
  public Vector3f right() {
    return this.right;
  }

  /**
   * Set the right vector of the camera.
   *
   * @param right the new right vector
   * @return this camera
   */
  public Camera right(Vector3f right) {
    this.right = right;
    this.update();
    return this;
  }

  /**
   * Rotate the camera to look at a target.
   *
   * @param target the target to rotate the camera towards
   * @return this camera
   */
  public Camera lookAt(Vector3f target) {
    this.front = target.sub(this.position, new Vector3f()).normalize();
    this.yaw = (float) Math.toDegrees(Math.atan2(this.front.z, this.front.x));
    this.pitch = (float) Math.toDegrees(Math.asin(this.front.y));
    this.update();
    return this;
  }

  /**
   * Get the front vector of the camera.
   *
   * @return the front vector
   */
  public Vector3f front() {
    return this.front;
  }

  /**
   * Set the front vector of the camera.
   *
   * @param front the new front vector
   * @return this camera
   */
  public Camera front(Vector3f front) {
    this.front = front;
    this.update();
    return this;
  }

  /**
   * Get the yaw angle of the camera.
   *
   * @return the yaw angle
   */
  public float yaw() {
    return this.yaw;
  }

  /**
   * Set the yaw angle of the camera.
   *
   * @param yaw the new yaw angle
   * @return this camera
   */
  public Camera yaw(float yaw) {
    this.yaw = yaw;
    this.update();
    return this;
  }

  /**
   * Get the pitch angle of the camera.
   *
   * @return the pitch angle
   */
  public float pitch() {
    return this.pitch;
  }

  /**
   * Set the pitch angle of the camera.
   *
   * @param pitch the new pitch angle
   * @return this camera
   */
  public Camera pitch(float pitch) {
    this.pitch = pitch;
    this.update();
    return this;
  }

  /**
   * Get the field of view angle of the camera.
   *
   * @return the field of view angle
   */
  public float fov() {
    return this.fov;
  }

  /**
   * Set the field of view angle of the camera.
   *
   * @param fov the new field of view angle
   * @return this camera
   */
  public Camera fov(float fov) {
    this.fov = fov;
    this.update();
    return this;
  }

  /**
   * Get the aspect ratio of the camera.
   *
   * @return the aspect ratio
   */
  public float aspectRatio() {
    return this.aspectRatio;
  }

  /**
   * Set the aspect ratio of the camera.
   *
   * @param aspectRatio the new aspect ratio
   * @return this camera
   */
  public Camera aspectRatio(float aspectRatio) {
    this.aspectRatio = aspectRatio;
    this.update();
    return this;
  }

  /**
   * Get the distance to the near clipping plane of the camera.
   *
   * @return the distance to the near clipping plane
   */
  public float nearPlane() {
    return this.near;
  }

  /**
   * Set the distance to the near clipping plane of the camera.
   *
   * @param nearPlane the new distance to the near clipping plane
   * @return this camera
   */
  public Camera nearPlane(float nearPlane) {
    this.near = nearPlane;
    this.update();
    return this;
  }

  /**
   * Get the distance to the far clipping plane of the camera.
   *
   * @return the distance to the far clipping plane
   */
  public float farPlane() {
    return this.far;
  }

  /**
   * Set the distance to the far clipping plane of the camera.
   *
   * @param farPlane the new distance to the far clipping plane
   * @return this camera
   */
  public Camera farPlane(float farPlane) {
    this.far = farPlane;
    this.update();
    return this;
  }
}
