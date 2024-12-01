package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Event;
import de.fwatermann.dungine.window.GameWindow;

/**
 * Represents a window focus changed event. This class extends the Event class and is used to signal
 * that the focus state of a game window has changed.
 */
public class WindowFocusChangedEvent extends Event {

  /** The focus state of the game window. */
  public final boolean focused;

  /** The game window whose focus state has changed. */
  public final GameWindow window;

  /**
   * Constructs a new WindowFocusChangedEvent.
   *
   * @param focused the focus state of the game window
   * @param window the game window whose focus state has changed
   */
  public WindowFocusChangedEvent(boolean focused, GameWindow window) {
    this.focused = focused;
    this.window = window;
  }
}
