package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction1Parameter;

public class UIComponentClickable extends UIComponent<UIComponentClickable> {

  private IVoidFunction1Parameter<UIElement<?>> onClick;

  public IVoidFunction1Parameter<UIElement<?>> onClick() {
    return this.onClick;
  }

  public UIComponentClickable onClick(IVoidFunction1Parameter<UIElement<?>> onClick) {
    this.onClick = onClick;
    return this;
  }
}
