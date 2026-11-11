package engine.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Game;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/** Tests how {@link TickAccumulator} turns frame time into world ticks. */
class TickAccumulatorTest {

  /** A clock that stands still: ticks cost no time. */
  private final TickAccumulator accumulator = new TickAccumulator(() -> 0L);

  private static int ticksOfFrame(TickAccumulator accumulator, float delta) {
    accumulator.beginFrame(delta);
    int ticks = 0;
    while (accumulator.nextTick()) {
      ticks++;
    }
    return ticks;
  }

  @Test
  void shortFramesAddUpToATick() {
    assertEquals(0, ticksOfFrame(accumulator, Game.TICK_SECONDS * 0.4f));
    assertEquals(0.4f, accumulator.alpha(), 0.001f);
    assertEquals(0, ticksOfFrame(accumulator, Game.TICK_SECONDS * 0.4f));
    assertEquals(1, ticksOfFrame(accumulator, Game.TICK_SECONDS * 0.4f));
    assertEquals(0.2f, accumulator.alpha(), 0.001f);
  }

  @Test
  void longFrameRunsSeveralTicks() {
    assertEquals(2, ticksOfFrame(accumulator, Game.TICK_SECONDS * 2.5f));
    assertEquals(0.5f, accumulator.alpha(), 0.001f);
  }

  @Test
  void stallRunsTheMaximumAndDropsTheRest() {
    assertEquals(TickAccumulator.MAX_TICKS_PER_FRAME, ticksOfFrame(accumulator, 2f));
    assertEquals(0, ticksOfFrame(accumulator, 0f));
  }

  @Test
  void slowTicksDoNotPileUp() {
    // Every tick costs longer than a tick lasts, so catching up could never succeed.
    AtomicLong nanos = new AtomicLong();
    TickAccumulator slow = new TickAccumulator(() -> nanos.getAndAdd(20_000_000L));

    assertEquals(1, ticksOfFrame(slow, Game.TICK_SECONDS * 3.5f));
    assertEquals(0, ticksOfFrame(slow, 0f));
  }
}
