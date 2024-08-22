package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction3Parameters;

public class UIComponentScrollable extends UIComponent<UIComponentScrollable> {

  private IVoidFunction3Parameters<UIElement<?>, Integer, Integer> onScroll;

  public IVoidFunction3Parameters<UIElement<?>, Integer, Integer> onScroll() {
    return this.onScroll;
  }

  public UIComponentScrollable onScroll(
      IVoidFunction3Parameters<UIElement<?>, Integer, Integer> onScroll) {
    this.onScroll = onScroll;
    return this;
  }
}
