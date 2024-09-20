package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.ui.layout.UIElementLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
  protected List<UIComponent<?>> components = new ArrayList<>();
  protected UIElementLayout layout = new UIElementLayout();

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

  /**
   * Get the layout object of this element.
   * @return the layout object
   */
  public UIElementLayout layout() {
    return this.layout;
  }

  /**
   * Attaches a component to this UI element.
   *
   * @param component the component to attach
   */
  public void attachComponent(UIComponent<?> component) {
    this.components.add(component);
  }

  /**
   * Detaches a component from this UI element.
   *
   * @param component the component to detach
   */
  public void detachComponent(UIComponent<?> component) {
    this.components.remove(component);
  }

  /**
   * Gets a stream of all components attached to this UI element.
   *
   * @return a stream of components
   */
  public Stream<UIComponent<?>> components() {
    return this.components.stream();
  }

  /**
   * Gets a stream of components of the specified types attached to this UI element.
   *
   * @param clazz the classes of the components to filter by
   * @return a stream of components of the specified types
   */
  @SafeVarargs
  public final Stream<UIComponent<?>> components(Class<? extends UIComponent<?>>... clazz) {
    return this.components.stream()
        .filter(
            c -> {
              for (Class<? extends UIComponent<?>> cClazz : clazz) {
                if (cClazz.isInstance(c)) {
                  return true;
                }
              }
              return false;
            });
  }

  /**
   * Gets the first component of the specified type attached to this UI element.
   *
   * @param <C> the type of the component
   * @param clazz the class of the component to get
   * @return the first component of the specified type, or null if none found
   */
  public final <C extends UIComponent<?>> Optional<C> component(Class<C> clazz) {
    return Optional.ofNullable((C) this.components.stream().filter(clazz::isInstance).findFirst().orElse(null));
  }

  /**
   * Checks if a component of the specified type is attached to this UI element.
   *
   * @param clazz the class of the component to check for
   * @return true if a component of the specified type is attached, false otherwise
   */
  public boolean hasComponent(Class<? extends UIComponent<?>> clazz) {
    return this.components.stream().anyMatch(clazz::isInstance);
  }
}
