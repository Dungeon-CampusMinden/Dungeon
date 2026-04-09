package core.camera;

import core.utils.Point;
import core.utils.Rectangle;
import java.util.Objects;
import java.util.Optional;

/**
 * Backend-neutral camera math shared by engine-specific camera implementations.
 *
 * <p>This helper intentionally only models pure camera behavior:
 *
 * <ul>
 *   <li>resolving the current focus target
 *   <li>softly approaching the focus target
 *   <li>calculating world bounds from viewport + zoom
 *   <li>performing a simple visibility test for world points
 * </ul>
 *
 * <p>Concrete backends remain responsible for applying the resulting position to an actual camera
 * object.
 */
public final class CameraMath {
  public static final Point ORIGIN = new Point(0, 0);

  private static final float SNAP_EPSILON = 0.01f;

  private CameraMath() {}

  /**
   * Resolves the current focus target for the camera.
   *
   * <p>Priority:
   *
   * <ol>
   *   <li>tracked entity point
   *   <li>level start point
   *   <li>world origin
   * </ol>
   *
   * @param trackedPoint focus point of a tracked entity
   * @param levelStartPoint fallback start point of the level
   * @return resolved camera focus point
   */
  public static Point resolveFocus(
    final Optional<Point> trackedPoint, final Optional<Point> levelStartPoint) {
    Objects.requireNonNull(trackedPoint, "trackedPoint");
    Objects.requireNonNull(levelStartPoint, "levelStartPoint");
    return trackedPoint.orElseGet(() -> levelStartPoint.orElse(ORIGIN));
  }

  /**
   * Advances the camera position toward the focus point using linear interpolation.
   *
   * <p>If no current position exists yet, the focus point is taken immediately. This preserves the
   * old first-frame behavior of {@code GdxCameraSystem}.
   *
   * @param currentPosition current camera position, may be {@code null} on first update
   * @param focusPoint target focus point
   * @param focusLerp interpolation factor in range {@code [0, 1]}
   * @return next camera position
   */
  public static Point stepTowardsFocus(
    final Point currentPosition, final Point focusPoint, final float focusLerp) {
    Objects.requireNonNull(focusPoint, "focusPoint");

    final float clampedLerp = Math.clamp(focusLerp, 0f, 1f);

    if (currentPosition == null) {
      return focusPoint;
    }

    final float newX =
      currentPosition.x() * (1 - clampedLerp) + focusPoint.x() * clampedLerp;
    final float newY =
      currentPosition.y() * (1 - clampedLerp) + focusPoint.y() * clampedLerp;

    final Point next = new Point(newX, newY);
    if (next.distance(focusPoint) <= SNAP_EPSILON) {
      return focusPoint;
    }
    return next;
  }

  /**
   * Calculates the world-space bounds currently covered by a camera.
   *
   * @param cameraCenter camera center position
   * @param viewportWidth viewport width in world units
   * @param viewportHeight viewport height in world units
   * @param zoom zoom factor
   * @return visible world bounds
   */
  public static Rectangle worldBounds(
    final Point cameraCenter,
    final float viewportWidth,
    final float viewportHeight,
    final float zoom) {
    Objects.requireNonNull(cameraCenter, "cameraCenter");

    final float safeViewportWidth = Math.max(0f, viewportWidth);
    final float safeViewportHeight = Math.max(0f, viewportHeight);
    final float safeZoom = Math.max(0f, zoom);

    final float worldWidth = safeViewportWidth * safeZoom;
    final float worldHeight = safeViewportHeight * safeZoom;
    final float posX = cameraCenter.x() - (worldWidth / 2f);
    final float posY = cameraCenter.y() - (worldHeight / 2f);

    return new Rectangle(worldWidth, worldHeight, posX, posY);
  }

  /**
   * Checks if a point is visible inside an axis-aligned camera view.
   *
   * <p>The view is expanded by the given margin to preserve the old
   * {@code GdxCameraSystem.isPointInFrustum(...)} semantics, which used a small bounding box around
   * the point instead of testing the point alone.
   *
   * @param point world point to test
   * @param cameraCenter camera center position
   * @param viewportWidth viewport width in world units
   * @param viewportHeight viewport height in world units
   * @param zoom zoom factor
   * @param margin extra visibility margin in world units
   * @return {@code true} if the point is visible, otherwise {@code false}
   */
  public static boolean isPointVisible(
    final Point point,
    final Point cameraCenter,
    final float viewportWidth,
    final float viewportHeight,
    final float zoom,
    final float margin) {
    Objects.requireNonNull(point, "point");

    final Rectangle visibleBounds =
      worldBounds(cameraCenter, viewportWidth, viewportHeight, zoom)
        .expand(Math.max(0f, margin));

    return visibleBounds.contains(point);
  }
}
