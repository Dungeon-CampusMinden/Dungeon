package engine.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.Point;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link SnapshotBuffer}. */
public class SnapshotBufferTest {

  /** Outside the buffered ticks there is only one snapshot to draw. */
  @Test
  void advanceYieldsOneSnapshotOutsideTheBufferedTicks() {
    SnapshotBuffer buffer = new SnapshotBuffer();
    buffer.add(snapshot(10));
    buffer.add(snapshot(11));

    SnapshotBuffer.Span before = buffer.advance(9).orElseThrow();
    SnapshotBuffer.Span after = buffer.advance(14).orElseThrow();

    assertEquals(10, before.from().tick());
    assertTrue(before.to().isEmpty());
    assertEquals(11, after.from().tick());
    assertTrue(after.to().isEmpty());
  }

  private static SnapshotMessage snapshot(int tick) {
    return new SnapshotMessage(
        tick,
        List.of(EntityState.builder().entityId(1).position(new Point(tick, 0)).build()),
        new LevelState(Set.of()));
  }
}
