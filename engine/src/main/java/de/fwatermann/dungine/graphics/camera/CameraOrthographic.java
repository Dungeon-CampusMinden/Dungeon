package de.fwatermann.dungine.graphics.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The `CameraOrthographic` class represents an orthographic camera in a 3D graphics environment.
 * It extends the `Camera` class and provides methods to set and get the zoom level, as well as to calculate
 * the orthographic projection matrix based on the camera's viewport and zoom level.
 *
 * <p>This class supports method chaining for setting properties like zoom level.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * CameraViewport viewport = new CameraViewport(800, 600);
 * CameraOrthographic camera = new CameraOrthographic(viewport, 2.0f);
 * camera.zoom(1.5f);
 * }
 * </pre>
 */
public class CameraOrthographic extends Camera<CameraOrthographic> {

  private float zoom = 1.0f;

  /**
   * Constructs a new Camera instance.
   *
   * @param viewport The viewport of the camera.
   * @param zoom The zoom level of the camera.
   * @param position The initial position of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public CameraOrthographic(
      CameraViewport viewport, float zoom, Vector3f position, boolean updateOnChange) {
    super(position, viewport, updateOnChange);
    this.zoom = zoom;
    this.updateMatrices(true);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewport The viewport of the camera.
   * @param zoom The zoom level of the camera.
   * @param updateOnChange If true, the camera will automatically update its view matrix when its
   *     state changes.
   */
  public CameraOrthographic(CameraViewport viewport, float zoom, boolean updateOnChange) {
    this(viewport, zoom, new Vector3f(), updateOnChange);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewport The viewport of the camera.
   * @param zoom The zoom level of the camera.
   */
  public CameraOrthographic(CameraViewport viewport, float zoom) {
    this(viewport, zoom, true);
  }

  /**
   * Constructs a new Camera instance.
   *
   * @param viewport The viewport of the camera.
   */
  public CameraOrthographic(CameraViewport viewport) {
    this(viewport, 1.0f, true);
  }

  @Override
  protected Matrix4f calcProjectionMatrix(Matrix4f projectionMatrix) {
    float left = (this.viewport.width() / 2) - (this.viewport.width() / 2) * (1 / this.zoom);
    float right = (this.viewport.width() / 2) + (this.viewport.width() / 2) * (1 / this.zoom);
    float bottom = (this.viewport.height() / 2) - (this.viewport.height() / 2) * (1 / this.zoom);
    float top = (this.viewport.height() / 2) + (this.viewport.height() / 2) * (1 / this.zoom);
    return projectionMatrix.setOrtho(left, right, bottom, top, -1, 1);
  }

  @Override
  protected void onUpdate() {}

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
