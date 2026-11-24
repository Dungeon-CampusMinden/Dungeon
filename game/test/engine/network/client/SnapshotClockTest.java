package engine.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import org.junit.jupiter.api.Test;

/** Tests for {@link SnapshotClock}. */
public class SnapshotClockTest {

  private static final long TICK_NANOS = 1_000_000_000L / Game.TICKS_PER_SECOND;
  private static final double DELAY_TICKS = 6;

  /** With snapshots arriving on time, the client draws the delay behind the server. */
  @Test
  void rendersTheDelayBehindTheServer() {
    SnapshotClock clock = new SnapshotClock(DELAY_TICKS);
    for (int tick = 100; tick <= 110; tick++) clock.observe(tick, tick * TICK_NANOS);

    assertEquals(110 - DELAY_TICKS, clock.renderTick(110 * TICK_NANOS), 0.001);
    assertEquals(120 - DELAY_TICKS, clock.renderTick(120 * TICK_NANOS), 0.001);
  }

  /**
   * Snapshots that arrive late pull the estimate back. The render tick still only moves forward,
   * and at most a tenth slower than real time.
   */
  @Test
  void neverRunsBackwardsWhenSnapshotsArriveLate() {
    SnapshotClock clock = new SnapshotClock(DELAY_TICKS);
    clock.observe(100, 100 * TICK_NANOS);
    double previous = clock.renderTick(100 * TICK_NANOS);

    for (int tick = 101; tick <= 200; tick++) {
      // Each snapshot took half a second longer than the first one.
      clock.observe(tick - 30, tick * TICK_NANOS);
      double rendered = clock.renderTick(tick * TICK_NANOS);
      double advanced = rendered - previous;
      assertTrue(advanced >= 0.9 - 0.001 && advanced <= 1.001, "advanced " + advanced);
      previous = rendered;
    }
  }
}
