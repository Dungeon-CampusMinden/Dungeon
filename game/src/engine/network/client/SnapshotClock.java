package engine.network.client;

import engine.Game;

/**
 * The moment a multiplayer client draws, measured in server ticks.
 *
 * <p>A client draws the world a little behind the newest snapshot, so that it usually knows a
 * snapshot on either side of the drawn moment and can draw the way between them. {@link #observe}
 * learns from each snapshot how server ticks relate to the local clock. {@link #renderTick} follows
 * the tick that lies the delay behind the server.
 *
 * <p>The render tick never runs backwards. It runs at most {@link #MAX_RATE_CHANGE} faster or
 * slower than real time to reach its target, so a change of the estimate or of the delay shows as a
 * slightly different speed and not as a jump.
 */
public final class SnapshotClock {

  private static final double TICKS_PER_NANO = Game.TICKS_PER_SECOND / 1e9;

  /**
   * Share of a measurement that enters the estimate; evens out the jitter of single packets. The
   * first measurements count more, so the estimate starts as their mean.
   */
  private static final double OFFSET_GAIN = 0.02;

  /** Share by which the render tick runs faster or slower than real time at most. */
  private static final double MAX_RATE_CHANGE = 0.1;

  /** The render tick jumps forward instead of catching up from this many ticks behind on. */
  private static final double JUMP_FORWARD_TICKS = Game.TICKS_PER_SECOND;

  private double delayTicks;
  // Server tick minus local time in ticks, as estimated from the snapshots.
  private double offset;
  private int observations;
  private double renderTick;
  private long renderedAtNanos;
  private boolean rendered;

  /**
   * Creates a clock.
   *
   * @param delayTicks how many ticks behind the server the client draws
   */
  public SnapshotClock(double delayTicks) {
    this.delayTicks = delayTicks;
  }

  /**
   * Learns from a snapshot when the server reached a tick.
   *
   * @param serverTick the tick of the snapshot
   * @param receivedAtNanos the {@link System#nanoTime()} at which the snapshot arrived
   */
  public void observe(int serverTick, long receivedAtNanos) {
    double measured = serverTick - receivedAtNanos * TICKS_PER_NANO;
    if (observations < Integer.MAX_VALUE) observations++;
    offset += (measured - offset) * Math.max(OFFSET_GAIN, 1.0 / observations);
  }

  /**
   * Advances the render tick to the given time.
   *
   * @param nowNanos the current {@link System#nanoTime()}
   * @return the server tick to draw; before the first snapshot was observed, a tick before all
   *     others
   */
  public double renderTick(long nowNanos) {
    if (observations == 0) return Double.NEGATIVE_INFINITY;
    double target = nowNanos * TICKS_PER_NANO + offset - delayTicks;
    if (!rendered || target - renderTick > JUMP_FORWARD_TICKS) {
      renderTick = target;
    } else {
      double realTime = Math.max(0, nowNanos - renderedAtNanos) * TICKS_PER_NANO;
      double behind = target - (renderTick + realTime);
      double most = realTime * MAX_RATE_CHANGE;
      renderTick += realTime + Math.clamp(behind, -most, most);
    }
    rendered = true;
    renderedAtNanos = nowNanos;
    return renderTick;
  }

  /**
   * Gets how many ticks behind the server the client draws.
   *
   * @return the delay in ticks
   */
  public double delayTicks() {
    return delayTicks;
  }
}
