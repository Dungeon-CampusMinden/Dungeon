package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.event.input.MouseButtonEvent;

public interface IUIClickable {

  void click(int button, MouseButtonEvent.MouseButtonAction action);

}
