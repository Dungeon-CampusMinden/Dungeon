package de.fwatermann.dungine.graphics.camera;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a camera in a 3D space, allowing for movement and orientation adjustments. The
 * camera's view and projection matrices can be updated based on its position, orientation, and
 * perspective settings.
 */
public class CameraPerspective extends Camera<CameraPerspective> {

  private float fov;
  private float aspectRatio;
  private float near;
  private float far;

  private final CameraFrustum frustum;

  /**
   * Constructs a Camera with specified perspective settings.
   *
   * @param position The position of the camera.
   * @param front The direction the camera is facing.
   * @param up The up vector of the camera.
   * @param fov The field of view angle in degrees.
   * @param aspectRatio The aspect ratio (width / height) of the camera.
   * @param near The distance to the near clipping plane.
   * @param far The distance to the far clipping plane.
   * @param updateOnChange Whether the camera should update its view and projection matrices when
   */
  public CameraPerspective(
      Vector3f position,
      Vector3f front,
      Vector3f up,
      float fov,
      float aspectRatio,
      float near,
      float far,
      boolean updateOnChange) {
    super(position, front, up, updateOnChange);
    this.fov = fov;
    this.aspectRatio = aspectRatio;
    this.near = near;
    this.far = far;
    this.frustum = new CameraFrustum();
    this._update(true);
  }

  /**
   * Constructs a Camera with specified perspective settings.
   *
   * @param position The position of the camera.
   * @param front The direction the camera is facing.
   * @param up The up vector of the camera.
   * @param fov The field of view angle in degrees.
   * @param aspectRatio The aspect ratio (width / height) of the camera.
   * @param near The distance to the near clipping plane.
   * @param far The distance to the far clipping plane.
   */
  public CameraPerspective(
      Vector3f position,
      Vector3f front,
      Vector3f up,
      float fov,
      float aspectRatio,
      float near,
      float far) {
    this(position, front, up, fov, aspectRatio, near, far, false);
  }

  /**
   * Constructs a Camera with specified perspective settings.
   *
   * @param position The position of the camera.
   * @param fov The field of view angle in degrees.
   * @param aspectRatio The aspect ratio (width / height) of the camera.
   * @param near The distance to the near clipping plane.
   * @param far The distance to the far clipping plane.
   */
  public CameraPerspective(Vector3f position, float fov, float aspectRatio, float near, float far) {
    this(
        position,
        new Vector3f(0.0f, 0.0f, -1.0f),
        new Vector3f(0.0f, 1.0f, 0.0f),
        fov,
        aspectRatio,
        near,
        far);
  }

  /**
   * Constructs a Camera with specified perspective settings.
   *
   * @param fov The field of view angle in degrees.
   * @param aspectRatio The aspect ratio (width / height) of the camera.
   * @param near The distance to the near clipping plane.
   * @param far The distance to the far clipping plane.
   */
  public CameraPerspective(float fov, float aspectRatio, float near, float far) {
    this(new Vector3f(0.0f, 0.0f, 0.0f), fov, aspectRatio, near, far);
  }

  /**
   * Constructs a Camera with default perspective settings.
   *
   * <p>The default settings are as follows:
   *
   * <ul>
   *   <li>Position: (0.0, 0.0, 0.0)
   *   <li>Front: (0.0, 0.0, -1.0)
   *   <li>Up: (0.0, 1.0, 0.0)
   *   <li>Field of View: 80.0 degrees
   *   <li>Aspect Ratio: 16:9
   *   <li>Near Clipping Plane: 0.1
   *   <li>Far Clipping Plane: 100.0
   *   <li>Update on Change: false
   * </ul>
   */
  public CameraPerspective() {
    this(80.0f, 16.0f / 9.0f, 0.1f, 100.0f);
  }

  @Override
  protected Matrix4f calcProjectionMatrix(Matrix4f projectionMatrix) {
    return projectionMatrix.setPerspective(
        Math.toRadians(this.fov), this.aspectRatio, this.near, this.far);
  }

  @Override
  protected void onUpdate() {
    this.frustum.set(this.viewMatrix.mul(this.projectionMatrix, new Matrix4f()));
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
  public CameraPerspective fov(float fov) {
    this.fov = fov;
    this._update(false);
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
  public CameraPerspective aspectRatio(float aspectRatio) {
    this.aspectRatio = aspectRatio;
    this._update(false);
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
  public CameraPerspective nearPlane(float nearPlane) {
    this.near = nearPlane;
    this._update(false);
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
  public CameraPerspective farPlane(float farPlane) {
    this.far = farPlane;
    this._update(false);
    return this;
  }

  /**
   * Get the camera frustum.
   *
   * @return the camera frustum
   */
  public CameraFrustum frustum() {
    return this.frustum;
  }
}
