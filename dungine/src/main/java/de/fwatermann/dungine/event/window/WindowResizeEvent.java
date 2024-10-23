package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Cancelable;
import de.fwatermann.dungine.event.Event;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;

/**
 * Represents a window resize event. This class implements the Cancelable interface and is used to
 * signal that a game window has been resized.
 */
public class WindowResizeEvent extends Event implements Cancelable {

  /** The cancel state of the window resize event. */
  private boolean canceled = false;

  /** The initial size of the game window before the resize. */
  public final Vector2i from;

  /** The final size of the game window after the resize. */
  public final Vector2i to;

  /** The game window that was resized. */
  public final GameWindow window;

  /**
   * Constructs a new WindowResizeEvent.
   *
   * @param from the initial size of the game window before the resize
   * @param to the final size of the game window after the resize
   * @param window the game window that was resized
   */
  public WindowResizeEvent(Vector2i from, Vector2i to, GameWindow window) {
    this.from = from;
    this.to = to;
    this.window = window;
  }

  /**
   * Checks if the window resize event has been canceled.
   *
   * @return the cancel state of the window resize event
   */
  @Override
  public boolean isCanceled() {
    return this.canceled;
  }

  /**
   * Sets the cancel state of the window resize event.
   *
   * @param canceled the cancel state to set
   */
  @Override
  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }
}
