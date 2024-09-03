package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;

public class UIComponentHoverable extends UIComponent<UIComponentHoverable> {

  private IVoidFunction1P<UIElement<?>> onEnter;
  private IVoidFunction1P<UIElement<?>> onLeave;

  public IVoidFunction1P<UIElement<?>> onEnter() {
    return this.onEnter;
  }

  public UIComponentHoverable onEnter(IVoidFunction1P<UIElement<?>> onEnter) {
    this.onEnter = onEnter;
    return this;
  }

  public IVoidFunction1P<UIElement<?>> onLeave() {
    return this.onLeave;
  }

  public UIComponentHoverable onLeave(IVoidFunction1P<UIElement<?>> onLeave) {
    this.onLeave = onLeave;
    return this;
  }
}
