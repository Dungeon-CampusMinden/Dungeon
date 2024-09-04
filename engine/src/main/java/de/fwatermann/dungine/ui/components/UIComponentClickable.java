package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;

public class UIComponentClickable extends UIComponent<UIComponentClickable> {

  private IVoidFunction1P<UIElement<?>> onClick;

  public UIComponentClickable() {
    this.onClick = (element) -> {};
  }

  public UIComponentClickable(IVoidFunction1P<UIElement<?>> onClick) {
    this.onClick = onClick;
  }

  public IVoidFunction1P<UIElement<?>> onClick() {
    return this.onClick;
  }

  public UIComponentClickable onClick(IVoidFunction1P<UIElement<?>> onClick) {
    this.onClick = onClick;
    return this;
  }
}
