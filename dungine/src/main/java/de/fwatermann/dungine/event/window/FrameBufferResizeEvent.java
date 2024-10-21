package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Event;

/**
 * The `FrameBufferResizeEvent` class represents an event that is triggered when the frame buffer is
 * resized. It contains the new width and height of the frame buffer.
 */
public class FrameBufferResizeEvent extends Event {

  private final int width;
  private final int height;

  /**
   * Constructs a new `FrameBufferResizeEvent` with the specified width and height.
   *
   * @param width the new width of the frame buffer
   * @param height the new height of the frame buffer
   */
  public FrameBufferResizeEvent(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns the new width of the frame buffer.
   *
   * @return the new width of the frame buffer
   */
  public int width() {
    return this.width;
  }

  /**
   * Returns the new height of the frame buffer.
   *
   * @return the new height of the frame buffer
   */
  public int height() {
    return this.height;
  }
}
