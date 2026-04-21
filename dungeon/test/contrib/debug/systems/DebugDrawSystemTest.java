package contrib.debug.systems;

import static org.junit.jupiter.api.Assertions.assertTrue;

import core.game.render.RenderContext;
import core.utils.Point;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for debug draw queue lifecycle behavior. */
class DebugDrawSystemTest {

  private static final String[] QUEUE_FIELDS = {
    "WORLD_RECTANGLES",
    "WORLD_FILLS",
    "SCREEN_TEXTS",
    "SCREEN_MARKERS",
    "WORLD_LINES",
    "WORLD_CIRCLE_OUTLINES",
    "WORLD_CIRCLE_FILLS",
    "SCREEN_RECTANGLES",
  };

  @AfterEach
  void cleanup() {
    RenderContext.clear();
    DebugDrawSystem.clearQueuedDrawCalls();
  }

  @Test
  void clearQueuedDrawCallsClearsAllQueues() throws Exception {
    queueAllDrawCalls();

    DebugDrawSystem.clearQueuedDrawCalls();

    assertAllQueuesEmpty();
  }

  @Test
  void renderWithoutRenderContextClearsAllQueues() throws Exception {
    RenderContext.clear();
    queueAllDrawCalls();

    new DebugDrawSystem().render(0f);

    assertAllQueuesEmpty();
  }

  @Test
  void stopDoesNotPauseDebugDrawSystem() {
    DebugDrawSystem system = new DebugDrawSystem();

    system.stop();

    assertTrue(system.isRunning());
  }

  private static void queueAllDrawCalls() {
    Point origin = new Point(0f, 0f);
    Point one = new Point(1f, 1f);

    DebugDrawSystem.drawRectangleOutline(0f, 0f, 1f, 1f, Color.WHITE);
    DebugDrawSystem.fillWorldRectangle(0f, 0f, 1f, 1f, Color.WHITE);
    DebugDrawSystem.drawText("debug", origin, Color.WHITE);
    DebugDrawSystem.drawScreenMarker(origin, 4, Color.WHITE, Color.BLACK);
    DebugDrawSystem.drawWorldLine(origin, one, Color.WHITE);
    DebugDrawSystem.drawWorldCircleOutline(origin, 1f, Color.WHITE);
    DebugDrawSystem.drawWorldCircleFill(origin, 1f, Color.WHITE);
    DebugDrawSystem.drawScreenRectangle(origin, 8, 8, Color.WHITE, Color.BLACK);
  }

  private static void assertAllQueuesEmpty() throws Exception {
    for (String fieldName : QUEUE_FIELDS) {
      Field field = DebugDrawSystem.class.getDeclaredField(fieldName);
      field.setAccessible(true);

      List<?> queue = (List<?>) field.get(null);
      assertTrue(queue.isEmpty(), fieldName + " should be empty");
    }
  }
}
