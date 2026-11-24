package engine.network.client;

import engine.Game;
import java.util.Arrays;

/**
 * The moment a multiplayer client draws, measured in server ticks.
 *
 * <p>A client draws the world a little behind the newest snapshot, so that it usually knows a
 * snapshot on either side of the drawn moment and can draw the way between them. {@link #observe}
 * learns from each snapshot how server ticks relate to the local clock: the estimated server tick
 * is the tick a snapshot would carry if it arrived now as fast as the fastest of the last seconds.
 * The time snapshots travel is not part of it. {@link #renderTick} follows the tick that lies the
 * delay behind this estimate.
 *
 * <p>The delay follows the connection. Each time a newer snapshot arrives, the clock notes how far
 * the newest one lay behind the estimate until then: the lateness the delay had to bridge. The
 * delay covers {@link #COVERED_SHARE} of these cases of the last seconds, plus a reserve. On a
 * local network that is a few ticks, on a connection with jitter or loss more. A new connection
 * starts with the shortest delay, which grows with the first late snapshots. The rare stall beyond
 * it is not covered: the picture then waits for the next snapshot, instead of every frame being
 * drawn later.
 *
 * <p>The render tick never runs backwards and never passes the newest snapshot. It closes the
 * distance to its target bit by bit and runs at most a tenth slower or a quarter faster than real
 * time for that, so a change of the delay shows as a slightly different speed and not as a jump.
 * Only far behind its target it jumps forward.
 */
public final class SnapshotClock {

  private static final double TICKS_PER_NANO = Game.TICKS_PER_SECOND / 1e9;

  /**
   * The estimate keeps to the fastest snapshot of the current and the previous period. That is
   * short enough to follow a connection that became slower, and long enough to hold a fast snapshot
   * through a burst of late ones.
   */
  private static final long ESTIMATE_PERIOD_NANOS = 1_000_000_000L;

  /** Share by which the render tick runs slower than real time at most, to gain reserve quickly. */
  private static final double MAX_SLOWDOWN = 0.1;

  /**
   * Share by which the render tick runs faster than real time at most. After a short stall it is a
   * stall's length behind its target; at this rate it is back within a second, and a moving entity
   * is still drawn at close to its speed.
   */
  private static final double MAX_SPEEDUP = 0.25;

  /** Share of the distance to its target the render tick closes per tick of real time. */
  private static final double CLOSED_PER_TICK = 0.1;

  /** The render tick jumps forward instead of catching up from this many ticks behind on. */
  private static final double JUMP_FORWARD_TICKS = Game.TICKS_PER_SECOND / 4.0;

  /** The shortest and the longest delay, in ticks. */
  private static final double MIN_DELAY_TICKS = 2;

  private static final double MAX_DELAY_TICKS = 12;

  /** Ticks the delay keeps on top of the lateness it covers. */
  private static final double RESERVE_TICKS = 1;

  /** Share of the arrivals whose lateness the delay covers. */
  private static final double COVERED_SHARE = 0.99;

  /** The lateness is counted over the current and the previous period. */
  private static final long PERIOD_NANOS = 10_000_000_000L;

  private static final int STEPS_PER_TICK = 4;

  // Arrivals by lateness in steps of a quarter tick, for the current and the previous period.
  private final int[][] arrivals = new int[2][(int) (MAX_DELAY_TICKS * STEPS_PER_TICK) + 1];
  // The newest snapshot the previous frame had.
  private int newestTick;
  // The periods count from the first rendered frame.
  private long periodsStartNanos;
  private long period;
  // Server tick minus local time in ticks of the fastest snapshot, for the current and the
  // previous period.
  private final double[] fastest = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
  private long estimatePeriod;
  // Server tick minus local time in ticks, as estimated from the snapshots.
  private double offset;
  private boolean observed;
  private double renderTick;
  // The estimated server tick at the time of the render tick.
  private double serverTick;
  private long renderedAtNanos;
  private boolean rendered;

  /**
   * Learns from a snapshot when the server reached a tick.
   *
   * <p>A snapshot that arrived fast tells how far the server is; one that arrived late only tells
   * that it took long. So the estimate keeps to the fastest snapshot of the last seconds.
   *
   * @param serverTick the tick of the snapshot
   * @param receivedAtNanos the {@link System#nanoTime()} at which the snapshot arrived
   */
  public void observe(int serverTick, long receivedAtNanos) {
    double measured = serverTick - receivedAtNanos * TICKS_PER_NANO;
    long now = Math.floorDiv(receivedAtNanos, ESTIMATE_PERIOD_NANOS);
    if (!observed || now > estimatePeriod) {
      if (!observed || now - estimatePeriod > 1) Arrays.fill(fastest, Double.NEGATIVE_INFINITY);
      fastest[(int) Math.floorMod(now, 2)] = Double.NEGATIVE_INFINITY;
      estimatePeriod = now;
    }
    int current = (int) Math.floorMod(estimatePeriod, 2);
    fastest[current] = Math.max(fastest[current], measured);
    offset = Math.max(fastest[0], fastest[1]);
    observed = true;
  }

  /**
   * Advances the render tick to the given time.
   *
   * @param nowNanos the current {@link System#nanoTime()}
   * @param newestTick the tick of the newest snapshot the client has
   * @return the server tick to draw
   */
  public double renderTick(long nowNanos, int newestTick) {
    serverTick = nowNanos * TICKS_PER_NANO + offset;
    if (!rendered) periodsStartNanos = nowNanos;
    startPeriod(nowNanos);
    if (rendered && newestTick != this.newestTick) {
      // Up to the previous frame the client only had the snapshot before this one.
      countLateness(renderedAtNanos * TICKS_PER_NANO + offset - this.newestTick);
    }
    this.newestTick = newestTick;

    double target = serverTick - targetDelayTicks();
    double next;
    if (!rendered || target - renderTick > JUMP_FORWARD_TICKS) {
      next = target;
    } else {
      double realTime = Math.max(0, nowNanos - renderedAtNanos) * TICKS_PER_NANO;
      double closed = 1 - Math.pow(1 - CLOSED_PER_TICK, realTime);
      double behind = (target - (renderTick + realTime)) * closed;
      next =
          renderTick
              + realTime
              + Math.clamp(behind, -realTime * MAX_SLOWDOWN, realTime * MAX_SPEEDUP);
    }
    // Beyond the newest snapshot there is nothing to draw, so the tick waits for the next one. A
    // server that ticks slower than real time for a while then cannot be outrun.
    next = Math.min(next, newestTick);
    renderTick = rendered ? Math.max(renderTick, next) : next;
    rendered = true;
    renderedAtNanos = nowNanos;
    return renderTick;
  }

  /**
   * Gets how many ticks behind the estimated server tick the client drew last. The time snapshots
   * travel is not part of it.
   *
   * @return the delay in ticks
   */
  public double delayTicks() {
    return serverTick - renderTick;
  }

  // Forgets the arrivals of the period before the previous one.
  private void startPeriod(long nowNanos) {
    long now = (nowNanos - periodsStartNanos) / PERIOD_NANOS;
    if (now == period) return;
    if (now - period > 1) Arrays.fill(arrivals[(int) ((now + 1) % 2)], 0);
    Arrays.fill(arrivals[(int) (now % 2)], 0);
    period = now;
  }

  private void countLateness(double lateness) {
    // A stall longer than any delay could cover says nothing about the delay the connection needs.
    if (lateness >= MAX_DELAY_TICKS - RESERVE_TICKS) return;
    int[] counts = arrivals[(int) (period % 2)];
    counts[(int) Math.clamp(lateness * STEPS_PER_TICK, 0, counts.length - 1)]++;
  }

  // The delay the render tick heads for: what the connection needs at the moment.
  private double targetDelayTicks() {
    return Math.clamp(coveredLateness() + RESERVE_TICKS, MIN_DELAY_TICKS, MAX_DELAY_TICKS);
  }

  // The lateness that COVERED_SHARE of the counted arrivals stayed below.
  private double coveredLateness() {
    int total = 0;
    for (int step = 0; step < arrivals[0].length; step++) {
      total += arrivals[0][step] + arrivals[1][step];
    }
    double needed = total * COVERED_SHARE;
    int counted = 0;
    for (int step = 0; step < arrivals[0].length; step++) {
      counted += arrivals[0][step] + arrivals[1][step];
      if (counted >= needed) return (step + 1.0) / STEPS_PER_TICK;
    }
    return MAX_DELAY_TICKS;
  }
}
