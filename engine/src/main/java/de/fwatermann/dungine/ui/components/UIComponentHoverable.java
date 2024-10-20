package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;

/**
 * A UI component that adds hover functionality to a UI element.
 * The UIComponentHoverable class allows setting and getting functions
 * that will be executed when the UI element is hovered over or when the hover ends.
 */
public class UIComponentHoverable extends UIComponent<UIComponentHoverable> {

  /** The function to be executed when the UI element is hovered over. */
  private IVoidFunction1P<UIElement<?>> onEnter;

  /** The function to be executed when the hover ends. */
  private IVoidFunction1P<UIElement<?>> onLeave;

  /**
   * Creates a new UIComponentHoverable.
   */
  public UIComponentHoverable() {}

  /**
   * Returns the function to be executed when the UI element is hovered over.
   *
   * @return the function to be executed when the UI element is hovered over
   */
  public IVoidFunction1P<UIElement<?>> onEnter() {
    return this.onEnter;
  }

  /**
   * Sets the function to be executed when the UI element is hovered over.
   *
   * @param onEnter the function to be executed when the UI element is hovered over
   * @return this UIComponentHoverable instance for method chaining
   */
  public UIComponentHoverable onEnter(IVoidFunction1P<UIElement<?>> onEnter) {
    this.onEnter = onEnter;
    return this;
  }

  /**
   * Returns the function to be executed when the hover ends.
   *
   * @return the function to be executed when the hover ends
   */
  public IVoidFunction1P<UIElement<?>> onLeave() {
    return this.onLeave;
  }

  /**
   * Sets the function to be executed when the hover ends.
   *
   * @param onLeave the function to be executed when the hover ends
   * @return this UIComponentHoverable instance for method chaining
   */
  public UIComponentHoverable onLeave(IVoidFunction1P<UIElement<?>> onLeave) {
    this.onLeave = onLeave;
    return this;
  }
}
