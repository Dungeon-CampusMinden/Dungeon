package engine.game;

import engine.Game;
import java.util.function.LongSupplier;

/**
 * Turns the time of rendered frames into whole world ticks.
 *
 * <p>A frame runs as many ticks as its time covers; the rest carries over to the next frame, so the
 * world advances at {@link Game#TICKS_PER_SECOND} whatever the frame rate is.
 *
 * <p>Catching up is limited, so that it cannot build up: a frame runs at most {@link
 * #MAX_TICKS_PER_FRAME} ticks, and no further tick once its ticks took as long as a tick lasts.
 * Time beyond that is dropped. After a stall, below 12 FPS, or when the simulation itself is slower
 * than real time, the world runs slower instead of costing ever more frames.
 */
final class TickAccumulator {

  /** The most ticks one frame runs. */
  static final int MAX_TICKS_PER_FRAME = 5;

  private static final long TICK_NANOS = 1_000_000_000L / Game.TICKS_PER_SECOND;

  private final LongSupplier nanoTime;
  private float seconds;
  private int ticksThisFrame;
  private long frameStartNanos;

  /** Creates an accumulator that measures the cost of ticks with the system clock. */
  TickAccumulator() {
    this(System::nanoTime);
  }

  /**
   * Creates an accumulator.
   *
   * @param nanoTime the clock that measures how long the ticks of a frame take
   */
  TickAccumulator(LongSupplier nanoTime) {
    this.nanoTime = nanoTime;
  }

  /**
   * Starts a frame.
   *
   * @param delta seconds since the last frame
   */
  void beginFrame(float delta) {
    seconds += delta;
    ticksThisFrame = 0;
    frameStartNanos = nanoTime.getAsLong();
  }

  /**
   * Checks whether the current frame has to run another world tick, and counts that tick as run.
   * Call it in a loop until it returns false.
   *
   * @return true if a tick is due
   */
  boolean nextTick() {
    if (seconds < Game.TICK_SECONDS) return false;
    boolean caughtUpEnough =
        ticksThisFrame >= MAX_TICKS_PER_FRAME
            || (ticksThisFrame > 0 && nanoTime.getAsLong() - frameStartNanos >= TICK_NANOS);
    if (caughtUpEnough) {
      seconds %= Game.TICK_SECONDS;
      return false;
    }
    seconds -= Game.TICK_SECONDS;
    ticksThisFrame++;
    return true;
  }

  /**
   * Gets how far the current frame lies between the last tick and the next one.
   *
   * @return the share of a tick that has passed since the last tick, from 0 to below 1
   */
  float alpha() {
    return seconds / Game.TICK_SECONDS;
  }
}
