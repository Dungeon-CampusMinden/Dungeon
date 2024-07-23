package de.fwatermann.dungine.ecs;

public abstract class System<T extends System<?>> {

  private int interval = 0;
  private final boolean isSync;
  private boolean paused = false;

  /**
   * Create a new System with specific interval and sync flag.
   *
   * @param interval Interval at which this system should be updated.
   * @param isSync Whether this system should be updated synchronously or asynchronously to the
   *     render loop.
   */
  public System(int interval, boolean isSync) {
    this.interval = interval;
    this.isSync = isSync;
  }

  /**
   * Create a new System with specific interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   *
   * @param interval Interval at which this system should be updated.
   */
  public System(int interval) {
    this(interval, false);
  }

  /**
   * Create a new System with default interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   */
  public System() {
    this(1, false);
  }

  public abstract void update();

  public int interval() {
    return this.interval;
  }

  public T interval(int interval) {
    this.interval = interval;
    return (T) this;
  }

  public final boolean sync() {
    return this.isSync;
  }

  public final boolean paused() {
    return this.paused;
  }

  public final T paused(boolean paused) {
    this.paused = paused;
    return (T) this;
  }
}
