package de.fwatermann.dungine.event.input;

import de.fwatermann.dungine.event.Event;

/**
 * Represents a mouse scroll event. This class provides additional information about a mouse scroll
 * event.
 */
public class MouseScrollEvent extends Event {

  /** The x-coordinate of the mouse scroll event. */
  public final int x;

  /** The y-coordinate of the mouse scroll event. */
  public final int y;

  /**
   * Constructs a new MouseScrollEvent with the specified x and y coordinates.
   *
   * @param x the x-coordinate of the mouse scroll event
   * @param y the y-coordinate of the mouse scroll event
   */
  public MouseScrollEvent(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
