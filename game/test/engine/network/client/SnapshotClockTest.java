package engine.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import org.junit.jupiter.api.Test;

/** Tests for {@link SnapshotClock}. */
public class SnapshotClockTest {

  private static final long TICK_NANOS = 1_000_000_000L / Game.TICKS_PER_SECOND;
  private static final int TWENTY_SECONDS = 20 * Game.TICKS_PER_SECOND;

  /**
   * A connection is drawn two ticks behind the server from its first frame on and stays there while
   * every snapshot arrives on time.
   */
  @Test
  void drawsTwoTicksBehindTheServerOnACalmConnection() {
    SnapshotClock clock = new SnapshotClock();
    clock.observe(100, 100 * TICK_NANOS);
    assertEquals(100 - 2, clock.renderTick(100 * TICK_NANOS, 100), 0.001);

    double rendered = 0;
    for (int tick = 101; tick <= 100 + TWENTY_SECONDS; tick++) {
      clock.observe(tick, tick * TICK_NANOS);
      rendered = clock.renderTick(tick * TICK_NANOS, tick);
    }

    assertEquals(2, clock.delayTicks(), 0.001);
    assertEquals(100 + TWENTY_SECONDS - 2, rendered, 0.01);
  }

  /**
   * The delay covers the lateness that keeps coming back, here every twentieth snapshot arriving
   * three ticks late.
   */
  @Test
  void drawsFurtherBehindWhenSnapshotsKeepComingLate() {
    SnapshotClock clock = new SnapshotClock();
    int seen = 99;
    for (int now = 100; now <= 100 + 2 * TWENTY_SECONDS; now++) {
      int newest = now % 20 < 3 ? now - now % 20 - 1 : now;
      while (seen < newest) clock.observe(++seen, now * TICK_NANOS);
      clock.renderTick(now * TICK_NANOS, newest);
    }

    assertEquals(3 + 1, clock.delayTicks(), 0.3);
  }

  /**
   * Stalls longer than any delay could cover do not lengthen the delay, even when one comes every
   * second. Once snapshots arrive again, the render tick jumps to its place behind the server.
   */
  @Test
  void keepsItsDelayAfterStalls() {
    SnapshotClock clock = new SnapshotClock();
    int seen = 99;
    double rendered = 0;
    for (int now = 100; now <= 100 + TWENTY_SECONDS; now++) {
      // The first third of every second, no snapshot arrives.
      int newest = now % Game.TICKS_PER_SECOND < 20 ? now - now % Game.TICKS_PER_SECOND - 1 : now;
      while (seen < newest) clock.observe(++seen, now * TICK_NANOS);
      rendered = clock.renderTick(now * TICK_NANOS, newest);
    }

    assertEquals(2, clock.delayTicks(), 0.001);
    assertEquals(100 + TWENTY_SECONDS - 2, rendered, 0.01);
  }

  /**
   * After a stall of a fifth of a second, too short for a jump, the render tick has caught up with
   * its place behind the server within a second.
   */
  @Test
  void catchesUpWithinASecondAfterAShortStall() {
    SnapshotClock clock = new SnapshotClock();
    int now = 100;
    for (; now <= 100 + TWENTY_SECONDS; now++) {
      clock.observe(now, now * TICK_NANOS);
      clock.renderTick(now * TICK_NANOS, now);
    }
    int stalledAt = now - 1;
    for (int end = now + 12; now < end; now++) clock.renderTick(now * TICK_NANOS, stalledAt);

    for (int seen = stalledAt, end = now + Game.TICKS_PER_SECOND; now < end; now++) {
      while (seen < now) clock.observe(++seen, now * TICK_NANOS);
      clock.renderTick(now * TICK_NANOS, now);
    }

    assertEquals(2, clock.delayTicks(), 0.5);
  }

  /**
   * While snapshots take half a second longer than before, the render tick waits at the newest one.
   * It only moves forward, at most a quarter faster than real time.
   */
  @Test
  void neverRunsBackwardsWhenSnapshotsArriveLate() {
    SnapshotClock clock = new SnapshotClock();
    clock.observe(100, 100 * TICK_NANOS);
    double previous = clock.renderTick(100 * TICK_NANOS, 100);

    for (int now = 101; now <= 300; now++) {
      int newest = Math.max(100, now - 30);
      if (newest > 100) clock.observe(newest, now * TICK_NANOS);
      double rendered = clock.renderTick(now * TICK_NANOS, newest);
      double advanced = rendered - previous;
      assertTrue(advanced >= 0 && advanced <= 1.25 + 0.001, "advanced " + advanced);
      assertTrue(rendered <= newest, rendered + " lies past the newest snapshot " + newest);
      previous = rendered;
    }
  }
}
