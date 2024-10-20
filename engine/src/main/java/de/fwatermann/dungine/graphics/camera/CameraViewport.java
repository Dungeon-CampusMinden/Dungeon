package de.fwatermann.dungine.graphics.camera;

/**
 * The `CameraViewport` class represents the viewport of a camera in a 3D graphics environment.
 * It provides methods to set and get the dimensions and offsets of the viewport.
 *
 * <p>This class supports method chaining for setting properties like width, height, offsetX, and offsetY.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * CameraViewport viewport = new CameraViewport(800, 600, 0, 0);
 * viewport.width(1024).height(768).offsetX(10).offsetY(20);
 * }
 * </pre>
 */
public class CameraViewport {

  private float width, height, offsetX, offsetY;

  /**
   * Constructs a `CameraViewport` with the specified dimensions and offsets.
   *
   * @param width the width of the viewport
   * @param height the height of the viewport
   * @param offsetX the x-offset of the viewport
   * @param offsetY the y-offset of the viewport
   */
  public CameraViewport(float width, float height, float offsetX, float offsetY) {
    this.width = width;
    this.height = height;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  /**
   * Sets the dimensions and offsets of the viewport.
   *
   * @param width the width of the viewport
   * @param height the height of the viewport
   * @param offsetX the x-offset of the viewport
   * @param offsetY the y-offset of the viewport
   */
  public void set(float width, float height, float offsetX, float offsetY) {
    this.width = width;
    this.height = height;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  /**
   * Gets the width of the viewport.
   *
   * @return the width of the viewport
   */
  public float width() {
    return this.width;
  }

  /**
   * Sets the width of the viewport.
   *
   * @param width the width of the viewport
   * @return the updated `CameraViewport` instance
   */
  public CameraViewport width(float width) {
    this.width = width;
    return this;
  }

  /**
   * Gets the height of the viewport.
   *
   * @return the height of the viewport
   */
  public float height() {
    return this.height;
  }

  /**
   * Sets the height of the viewport.
   *
   * @param height the height of the viewport
   * @return the updated `CameraViewport` instance
   */
  public CameraViewport height(float height) {
    this.height = height;
    return this;
  }

  /**
   * Gets the x-offset of the viewport.
   *
   * @return the x-offset of the viewport
   */
  public float offsetX() {
    return this.offsetX;
  }

  /**
   * Sets the x-offset of the viewport.
   *
   * @param offsetX the x-offset of the viewport
   * @return the updated `CameraViewport` instance
   */
  public CameraViewport offsetX(float offsetX) {
    this.offsetX = offsetX;
    return this;
  }

  /**
   * Gets the y-offset of the viewport.
   *
   * @return the y-offset of the viewport
   */
  public float offsetY() {
    return this.offsetY;
  }

  /**
   * Sets the y-offset of the viewport.
   *
   * @param offsetY the y-offset of the viewport
   * @return the updated `CameraViewport` instance
   */
  public CameraViewport offsetY(float offsetY) {
    this.offsetY = offsetY;
    return this;
  }
}
