package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction3P;

/**
 * A UI component that adds scroll functionality to a UI element.
 * The UIComponentScrollable class allows setting and getting a function
 * that will be executed when the UI element is scrolled.
 */
public class UIComponentScrollable extends UIComponent<UIComponentScrollable> {

  /** The function to be executed when the UI element is scrolled. */
  private IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll;

  /**
   * Creates a new UIComponentScrollable.
   */
  public UIComponentScrollable() {}

  /**
   * Returns the function to be executed when the UI element is scrolled.
   *
   * @return the function to be executed when the UI element is scrolled
   */
  public IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll() {
    return this.onScroll;
  }

  /**
   * Sets the function to be executed when the UI element is scrolled.
   *
   * @param onScroll the function to be executed when the UI element is scrolled
   * @return this UIComponentScrollable instance for method chaining
   */
  public UIComponentScrollable onScroll(
      IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll) {
    this.onScroll = onScroll;
    return this;
  }
}
