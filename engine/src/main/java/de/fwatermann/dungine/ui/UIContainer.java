package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.graphics.camera.Camera;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * The UIContainer class represents a container for UI elements. It extends the UIElement class and
 * provides methods to add, remove, and render child UI elements.
 */
public class UIContainer<T extends UIContainer<?>> extends UIElement<T> {

  private final List<UIElement<?>> elements = new ArrayList<>();

  /**
   * Constructs a UIContainer with the specified UI elements.
   *
   * @param elements the UI elements to add to this container
   */
  public UIContainer(UIElement<?>... elements) {
    Arrays.stream(elements).forEach(this::add);
  }

  /**
   * Constructs a UIContainer with the specified collection of UI elements.
   *
   * @param elements the collection of UI elements to add to this container
   */
  public UIContainer(Collection<UIElement<?>> elements) {
    elements.forEach(this::add);
  }

  /**
   * Renders the UIContainer and its child elements using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    // TODO: Stencil!
    this.elements.stream()
        .sorted((a, b) -> Float.compare(a.position.z, b.position.z))
        .forEach(element -> element.render(camera));
  }

  /**
   * Adds a UI element to this container.
   *
   * @param element the UI element to add
   */
  public void add(UIElement<?> element) {
    this.elements.add(element);
    element.parent = this;
  }

  /**
   * Removes a UI element from this container.
   *
   * @param element the UI element to remove
   */
  public void remove(UIElement<?> element) {
    this.elements.remove(element);
    element.parent = null;
  }

  /**
   * Returns a stream of all UI elements contained in this container.
   *
   * @return a stream of UI elements
   */
  public Stream<UIElement<?>> elements() {
    return this.elements.stream();
  }
}
