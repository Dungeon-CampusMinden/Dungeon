package de.fwatermann.dungine.utils;


/**
 * The `FrameCounter` class is used to keep track of the number of frames per second (FPS) in an application.
 * It provides methods to update the frame count and retrieve the current FPS.
 */
public class FrameCounter {

  private long counter = 0;
  private long currentFPS = 0;
  private long lastReset = 0;
  private long updateInterval = 1000;

  /**
   * Constructs a new `FrameCounter` with the default update interval of 1000 milliseconds.
   */
  public FrameCounter() {}

  /**
   * Updates the frame counter. If the update interval has passed, it calculates the current FPS,
   * resets the counter, and updates the last reset time.
   */
  public void update() {
    this.counter ++;
    if(System.currentTimeMillis() > this.lastReset + this.updateInterval) {
      this.currentFPS = this.counter * (1000 / this.updateInterval);
      this.counter = 0;
      this.lastReset = System.currentTimeMillis();
    }
  }

  /**
   * Returns the current frames per second (FPS).
   *
   * @return the current FPS
   */
  public long currentFPS() {
    return this.currentFPS;
  }

  /**
   * Returns the update interval in milliseconds.
   *
   * @return the update interval
   */
  public long updateInterval() {
    return this.updateInterval;
  }

  /**
   * Sets the update interval and returns the `FrameCounter` instance.
   *
   * @param updateInterval the update interval in milliseconds
   * @return the `FrameCounter` instance
   */
  public FrameCounter updateInterval(long updateInterval) {
    this.updateInterval = updateInterval == 0 ? 1 : updateInterval;
    return this;
  }
}
