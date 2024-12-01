package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction3P;

/**
 * A UI component that adds click functionality to a UI element. The UIComponentClickable class
 * allows setting and getting a function that will be executed when the UI element is clicked.
 */
public class UIComponentClickable extends UIComponent<UIComponentClickable> {

  /** The function to be executed when the UI element is clicked. */
  private IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick;

  /**
   * Constructs a UIComponentClickable with a default click function. The default function does
   * nothing.
   */
  public UIComponentClickable() {
    this.onClick = (element, button, action) -> {};
  }

  /**
   * Constructs a UIComponentClickable with the specified click function.
   *
   * @param onClick the function to be executed when the UI element is clicked
   */
  public UIComponentClickable(
      IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick) {
    this.onClick = onClick;
  }

  /**
   * Returns the function to be executed when the UI element is clicked.
   *
   * @return the function to be executed when the UI element is clicked
   */
  public IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick() {
    return this.onClick;
  }

  /**
   * Sets the function to be executed when the UI element is clicked.
   *
   * @param onClick the function to be executed when the UI element is clicked
   * @return this UIComponentClickable instance for method chaining
   */
  public UIComponentClickable onClick(
      IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick) {
    this.onClick = onClick;
    return this;
  }
}
