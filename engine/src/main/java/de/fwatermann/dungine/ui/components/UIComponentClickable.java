package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction3P;

public class UIComponentClickable extends UIComponent<UIComponentClickable> {

  private IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick;

  public UIComponentClickable() {
    this.onClick = (element, button, action) -> {};
  }

  public UIComponentClickable(IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick) {
    this.onClick = onClick;
  }

  public IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick() {
    return this.onClick;
  }

  public UIComponentClickable onClick(IVoidFunction3P<UIElement<?>, Integer, MouseButtonEvent.MouseButtonAction> onClick) {
    this.onClick = onClick;
    return this;
  }
}
