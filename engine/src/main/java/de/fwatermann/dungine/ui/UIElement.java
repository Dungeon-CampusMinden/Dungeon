package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.graphics.camera.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The UIElement class represents an abstract base class for UI elements. It provides common
 * properties and methods for managing position, size, and parent-child relationships of UI
 * elements.
 *
 * @param <T> the type of the UIElement subclass
 */
public abstract class UIElement<T extends UIElement<?>> {

  protected UIContainer<?> parent;
  protected Vector3f position = new Vector3f();
  protected Vector3f size = new Vector3f();
  protected Quaternionf rotation = new Quaternionf();
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
  public UIContainer<?> parent() {
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
   * Gets the rotation of this UI element.
   *
   * @return the rotation as a Quaternionf
   */
  public Quaternionf rotation() {
    return this.rotation;
  }

  /**
   * Sets the rotation of this UI element.
   *
   * @param rotation the new rotation as a Quaternionf
   * @return this UIElement instance for method chaining
   */
  public T rotation(Quaternionf rotation) {
    this.rotation = rotation;
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified rotation.
   *
   * @param rotation the rotation to apply as a Quaternionf
   * @return this UIElement instance for method chaining
   */
  public T rotate(Quaternionf rotation) {
    this.rotation.mul(rotation);
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified angle around the given axis.
   *
   * @param angle the angle to rotate by
   * @param axis the axis to rotate around as a Vector3f
   * @return this UIElement instance for method chaining
   */
  public T rotate(float angle, Vector3f axis) {
    this.rotation.rotateAxis(angle, axis);
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified angle around the given axis.
   *
   * @param angle the angle to rotate by
   * @param x the x component of the axis
   * @param y the y component of the axis
   * @param z the z component of the axis
   * @return this UIElement instance for method chaining
   */
  public T rotate(float angle, float x, float y, float z) {
    this.rotation.rotateAxis(angle, x, y, z);
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified angle around the x-axis.
   *
   * @param angle the angle to rotate by
   * @return this UIElement instance for method chaining
   */
  public T rotateX(float angle) {
    this.rotation.rotateX(angle);
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified angle around the y-axis.
   *
   * @param angle the angle to rotate by
   * @return this UIElement instance for method chaining
   */
  public T rotateY(float angle) {
    this.rotation.rotateY(angle);
    return (T) this;
  }

  /**
   * Rotates this UI element by the specified angle around the z-axis.
   *
   * @param angle the angle to rotate by
   * @return this UIElement instance for method chaining
   */
  public T rotateZ(float angle) {
    this.rotation.rotateZ(angle);
    return (T) this;
  }

  /**
   * Get the absolute position of this element.
   *
   * @return the absolute position
   */
  public Vector3f absolutePosition() {
    return this.parent == null
        ? this.position
        : this.parent.absolutePosition().add(this.position, new Vector3f());
  }
}
