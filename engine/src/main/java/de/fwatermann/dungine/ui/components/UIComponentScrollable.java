package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction3P;

public class UIComponentScrollable extends UIComponent<UIComponentScrollable> {

  private IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll;

  public IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll() {
    return this.onScroll;
  }

  public UIComponentScrollable onScroll(
      IVoidFunction3P<UIElement<?>, Integer, Integer> onScroll) {
    this.onScroll = onScroll;
    return this;
  }
}
