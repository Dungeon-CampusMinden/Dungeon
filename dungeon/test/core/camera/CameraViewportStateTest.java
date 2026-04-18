package core.camera;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.utils.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Regression tests for shared camera viewport coordinate conversion. */
class CameraViewportStateTest {

  private static final float EPSILON = 0.001f;

  @AfterEach
  void tearDown() {
    CameraViewportState.reset();
  }

  @Test
  void screenToWorldMapsScreenCenterToTileCenter() {
    CameraViewportState.set(688, 242, 10, 32);

    Point world = CameraViewportState.screenToWorld(new Point(800, 450), new Point(3, 3));

    assertEquals(3.5f, world.x(), EPSILON);
    assertEquals(3.5f, world.y(), EPSILON);
  }

  @Test
  void screenToWorldKeepsWorldYStableForHorizontalCursorMovement() {
    CameraViewportState.set(688, 242, 10, 32);

    Point left = CameraViewportState.screenToWorld(new Point(640, 450), new Point(3, 3));
    Point right = CameraViewportState.screenToWorld(new Point(960, 450), new Point(3, 3));

    assertEquals(3.5f, left.y(), EPSILON);
    assertEquals(3.5f, right.y(), EPSILON);
  }
}
