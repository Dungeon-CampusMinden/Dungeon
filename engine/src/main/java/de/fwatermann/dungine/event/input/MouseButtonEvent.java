package de.fwatermann.dungine.event.input;

import de.fwatermann.dungine.event.Event;

/**
 * Represents a mouse button event. This class provides additional information about a mouse button
 * event.
 */
public class MouseButtonEvent extends Event {

  /** The key code of the mouse button involved in the event. */
  public final int button;

  /** The action performed on the mouse button. */
  public final MouseButtonAction action;

  /**
   * Constructs a new MouseButtonEvent with the specified key code and action.
   *
   * @param button the key code of the mouse button involved in the event
   * @param action the action performed on the mouse button
   */
  public MouseButtonEvent(int button, MouseButtonAction action) {
    this.button = button;
    this.action = action;
  }

  /** Enum representing the possible actions that can be performed on a mouse button. */
  public enum MouseButtonAction {
    /** Represents the pressing of a mouse button. */
    PRESS,

    /** Represents the releasing of a mouse button. */
    RELEASE,

    /** Represents the repeated pressing of a mouse button. */
    REPEAT
  }
}
