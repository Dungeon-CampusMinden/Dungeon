package de.fwatermann.dungine.ecs;

import de.fwatermann.dungine.state.GameState;

public abstract class System<T extends System<?>> {

  private int interval = 0;
  private GameState attachedGameState;

  /**
   * Create a new System with default interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   */
  public System() {}

  /**
   * Create a new System with specific interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   *
   * @param interval Interval at which this system should be updated.
   */
  public System(int interval) {
    this.interval = interval;
  }

  public abstract void update();

  public int interval() {
    return this.interval;
  }

  public T interval(int interval) {
    this.interval = interval;
    return (T) this;
  }

  public GameState attachedGameState() {
    return this.attachedGameState;
  }

  public T attachedGameState(GameState state) {
    this.attachedGameState = state;
    return (T) this;
  }

}
