package de.fwatermann.dungine.ui.components;

import de.fwatermann.dungine.ui.UIComponent;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.utils.functions.IVoidFunction1Parameter;

public class UIComponentHoverable extends UIComponent<UIComponentHoverable> {

  private IVoidFunction1Parameter<UIElement<?>> onEnter;
  private IVoidFunction1Parameter<UIElement<?>> onLeave;

  public IVoidFunction1Parameter<UIElement<?>> onEnter() {
    return this.onEnter;
  }

  public UIComponentHoverable onEnter(IVoidFunction1Parameter<UIElement<?>> onEnter) {
    this.onEnter = onEnter;
    return this;
  }

  public IVoidFunction1Parameter<UIElement<?>> onLeave() {
    return this.onLeave;
  }

  public UIComponentHoverable onLeave(IVoidFunction1Parameter<UIElement<?>> onLeave) {
    this.onLeave = onLeave;
    return this;
  }
}
