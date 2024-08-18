package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.ui.IUIClickable;
import de.fwatermann.dungine.utils.IVoidFunction;

public class UITextClickable extends UIText implements IUIClickable {

  private IVoidFunction onClick = null;

  public UITextClickable(Font font, String text, IVoidFunction onClick) {
    super(font, text);
  }

  public UITextClickable(Font font, String text, int fontSize, IVoidFunction onClick) {
    super(font, text, fontSize);
  }

  public UITextClickable(Font font, String text) {
    super(font, text);
  }

  public UITextClickable(Font font, String text, int fontSize) {
    super(font, text, fontSize);
  }

  @Override
  public void click(int button, MouseButtonEvent.MouseButtonAction action) {
    if(this.onClick != null) this.onClick.run();
  }

  public IVoidFunction getOnClick() {
    return this.onClick;
  }

  public UITextClickable setOnClick(IVoidFunction onClick) {
    this.onClick = onClick;
    return this;
  }
}
