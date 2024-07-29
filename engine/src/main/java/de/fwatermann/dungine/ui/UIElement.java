package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.graphics.camera.Camera;
import org.joml.Vector3f;

/**
 * The UIElement class represents an abstract base class for UI elements.
 * It provides common properties and methods for managing position, size,
 * and parent-child relationships of UI elements.
 *
 * @param <T> the type of the UIElement subclass
 */
public abstract class UIElement<T extends UIElement<?>> {

  protected UIContainer parent;
  protected Vector3f position = new Vector3f();
  protected Vector3f size = new Vector3f();
  protected boolean initialized = false;

  /**
   * Renders the UI element using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  protected abstract void render(Camera<?> camera);

  /**
   * Gets the parent container of this UI element.
   *
   * @return the parent container
   */
  public UIContainer parent() {
    return this.parent;
  }

  /**
   * Gets the position of this UI element.
   *
   * @return the position
   */
  public Vector3f position() {
    return this.position;
  }

  /**
   * Sets the position of this UI element.
   *
   * @param position the new position
   * @return this UIElement instance for method chaining
   */
  public T position(Vector3f position) {
    this.position = position;
    return (T) this;
  }

  /**
   * Gets the size of this UI element.
   *
   * @return the size
   */
  public Vector3f size() {
    return this.size;
  }

  /**
   * Sets the size of this UI element.
   *
   * @param size the new size
   * @return this UIElement instance for method chaining
   */
  public T size(Vector3f size) {
    this.size = size;
    return (T) this;
  }

  /**
   * Get the absolute position of this element.
   * @return the absolute position
   */
  public Vector3f absolutePosition() {
    return this.parent == null
        ? this.position
        : this.parent.absolutePosition().add(this.position, new Vector3f());
  }
}
