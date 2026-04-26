package core.systems;

import static org.junit.jupiter.api.Assertions.*;

import core.camera.CameraMath;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for backend-neutral camera math extracted from the old GDX camera system. */
public class CameraSystemTest {

  private static final Point TRACKED_POINT = new Point(3, 3);
  private static final Point START_POINT = new Point(7, 7);
  private static final Point ORIGIN = new Point(0, 0);

  private static final float VIEWPORT_WIDTH = 10f;
  private static final float VIEWPORT_HEIGHT = 10f;
  private static final float ZOOM = 1f;
  private static final float FOCUS_LERP = 0.1f;

  /**
   * Tests camera focus calculation when a tracked entity is present. Verifies that the camera steps
   * towards the tracked point over time using linear interpolation.
   */
  @Test
  public void executeWithEntity() {
    Point focus = CameraMath.resolveFocus(Optional.of(TRACKED_POINT), Optional.of(START_POINT));
    Point actual = CameraMath.stepTowardsFocus(null, focus, FOCUS_LERP);

    assertEquals(TRACKED_POINT.x(), actual.x(), 0.001);
    assertEquals(TRACKED_POINT.y(), actual.y(), 0.001);
  }

  /**
   * Tests camera focus calculation when no tracked entity is present. Verifies that the camera uses
   * the start point as the focus when no entity is being tracked.
   */
  @Test
  public void executeWithoutEntity() {
    Point focus = CameraMath.resolveFocus(Optional.empty(), Optional.of(START_POINT));
    Point actual = CameraMath.stepTowardsFocus(null, focus, FOCUS_LERP);

    assertEquals(START_POINT.x(), actual.x(), 0.001);
    assertEquals(START_POINT.y(), actual.y(), 0.001);
  }

  /**
   * Tests camera focus calculation when neither a tracked entity nor a level start point are
   * available. Verifies that the camera defaults to the origin (0, 0) when no reference points
   * exist.
   */
  @Test
  public void executeWithoutLevel() {
    Point focus = CameraMath.resolveFocus(Optional.empty(), Optional.empty());
    Point actual = CameraMath.stepTowardsFocus(null, focus, FOCUS_LERP);

    assertEquals(ORIGIN.x(), actual.x(), 0.001);
    assertEquals(ORIGIN.y(), actual.y(), 0.001);
  }

  /**
   * Tests that a point within the camera's viewport is correctly identified as visible. Verifies
   * that points inside the frustum area are recognized by the visibility check.
   */
  @Test
  public void isPointInFrustumWithVisiblePoint() {
    assertTrue(
        CameraMath.isPointVisible(
            new Point(1.0f, 1.0f), ORIGIN, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, ZOOM, 1.0f));
  }

  /**
   * Tests that a point outside the camera's viewport is correctly identified as invisible. Verifies
   * that points outside the frustum area are not recognized by the visibility check.
   */
  @Test
  public void isPointInFrustumWithInvisiblePoint() {
    assertFalse(
        CameraMath.isPointVisible(
            new Point(100.0f, 100.0f), ORIGIN, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, ZOOM, 1.0f));
  }
}
