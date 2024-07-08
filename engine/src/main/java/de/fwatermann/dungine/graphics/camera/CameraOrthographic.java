package de.fwatermann.dungine.graphics.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CameraOrthographic extends Camera<CameraOrthographic> {

  private float viewportWidth;
  private float viewportHeight;
  private float zoom = 1.0f;

  /**
   * Constructs a new Camera instance.
   *
   * @param viewportWidth The width of the camera's viewport.
   * @param viewportHeight The height of the camera's viewport.
   * @param zoom The zoom level of the camera.
   * @param position The initial position of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public CameraOrthographic(
      float viewportWidth,
      float viewportHeight,
      float zoom,
      Vector3f position,
      boolean updateOnChange) {
    super(position, updateOnChange);
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
    this.zoom = zoom;
    this.updateMatrices(true);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewportWidth The width of the camera's viewport.
   * @param viewportHeight The height of the camera's viewport.
   * @param zoom The zoom level of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public CameraOrthographic(
      float viewportWidth, float viewportHeight, float zoom, boolean updateOnChange) {
    this(
        viewportWidth,
        viewportHeight,
        zoom,
        new Vector3f(),
        updateOnChange);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewportWidth The width of the camera's viewport.
   * @param viewportHeight The height of the camera's viewport.
   * @param zoom The zoom level of the camera.
   */
  public CameraOrthographic(float viewportWidth, float viewportHeight, float zoom) {
    this(viewportWidth, viewportHeight, zoom, true);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewportWidth The width of the camera's viewport.
   * @param viewportHeight The height of the camera's viewport.
   */
  public CameraOrthographic(float viewportWidth, float viewportHeight) {
    this(viewportWidth, viewportHeight, 1.0f, true);
  }

  @Override
  protected Matrix4f calcProjectionMatrix(Matrix4f projectionMatrix) {
    return projectionMatrix.setOrtho(
        this.zoom * -this.viewportWidth / 2,
        this.zoom * this.viewportWidth / 2,
        this.zoom * -this.viewportHeight / 2,
        this.zoom * this.viewportHeight / 2,
        -1,
        1);
  }

  @Override
  protected void onUpdate() {}

  /**
   * Returns the current width of the viewport.
   *
   * @return The width of the viewport.
   */
  public float viewportWidth() {
    return this.viewportWidth;
  }

  /**
   * Sets the width of the viewport and returns the updated camera instance. This method allows for
   * chaining.
   *
   * @param viewportWidth The new width to set for the viewport.
   * @return The updated OrthographicCamera instance.
   */
  public CameraOrthographic viewportWidth(float viewportWidth) {
    this.viewportWidth = viewportWidth;
    this.updateMatrices(false);
    return this;
  }

  /**
   * Returns the current height of the viewport.
   *
   * @return The height of the viewport.
   */
  public float viewportHeight() {
    return this.viewportHeight;
  }

  /**
   * Sets the height of the viewport and returns the updated camera instance. This method allows for
   * chaining.
   *
   * @param viewportHeight The new height to set for the viewport.
   * @return The updated OrthographicCamera instance.
   */
  public CameraOrthographic viewportHeight(float viewportHeight) {
    this.viewportHeight = viewportHeight;
    this.updateMatrices(false);
    return this;
  }

  /**
   * Returns the current zoom level of the camera.
   *
   * @return The zoom level.
   */
  public float zoom() {
    return this.zoom;
  }

  /**
   * Sets the zoom level of the camera and returns the updated camera instance. This method allows
   * for chaining.
   *
   * @param zoom The new zoom level to set.
   * @return The updated OrthographicCamera instance.
   */
  public CameraOrthographic zoom(float zoom) {
    this.zoom = zoom;
    this.updateMatrices(false);
    return this;
  }
}
