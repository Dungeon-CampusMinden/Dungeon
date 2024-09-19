package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Event;

public class FrameBufferResizeEvent extends Event {

  private final int width;
  private final int height;

  public FrameBufferResizeEvent(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int width() {
    return this.width;
  }

  public int height() {
    return this.height;
  }
}
