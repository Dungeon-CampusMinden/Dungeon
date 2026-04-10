package core.platform;

import core.Entity;
import core.camera.CameraMath;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;

/**
 * Backend-specific camera access for gameplay/debug helpers.
 *
 * <p>This interface intentionally stays small. For now it exposes zoom and the current
 * focus position / camera center in world units.
 */
public interface CameraAdapter {

  /**
   * Returns whether the active backend supports zoom control.
   *
   * @return {@code true} if zoom can be read/written, otherwise {@code false}
   */
  default boolean supportsZoom() {
    return false;
  }

  /**
   * Returns the current zoom factor.
   *
   * @return current zoom factor; default is {@code 1f}
   */
  default float zoom() {
    return 1f;
  }

  /**
   * Updates the current zoom factor.
   *
   * @param zoom new zoom factor
   */
  default void zoom(float zoom) {
    // no-op by default
  }

  /**
   * Returns whether the active backend exposes its current focus position.
   *
   * @return {@code true} if the focus position can be read/written
   */
  default boolean supportsFocusPosition() {
    return false;
  }

  /**
   * Returns the current focus position / camera center in world units.
   *
   * @return current focus position; default is the origin
   */
  default Point focusPosition() {
    return new Point(0, 0);
  }

  /**
   * Updates the current focus position / camera center.
   *
   * @param focusPosition new focus position
   */
  default void focusPosition(Point focusPosition) {
    // no-op by default
  }

  /**
   * Returns whether the active backend can resolve a camera follow target from the current game
   * state.
   *
   * @return {@code true} if follow-target resolution is supported
   */
  default boolean supportsFollowTargetResolution() {
    return false;
  }

  /**
   * Resolves the current camera follow target in world units.
   *
   * <p>Backends that do not support this may simply fall back to the current focus position.
   *
   * @return resolved camera follow target
   */
  default Point resolveFollowTarget() {
    return focusPosition();
  }

  default boolean supportsViewportMetrics() {
    return false;
  }

  default float viewportWidth() {
    return 0f;
  }

  default float viewportHeight() {
    return 0f;
  }

  default Rectangle worldBounds() {
    return CameraMath.worldBounds(focusPosition(), viewportWidth(), viewportHeight(), zoom());
  }

  default boolean isPointVisible(Point point) {
    return isPointVisible(point, 1f);
  }

  default boolean isPointVisible(Point point, float margin) {
    return CameraMath.isPointVisible(
      point, focusPosition(), viewportWidth(), viewportHeight(), zoom(), margin);
  }

  default boolean isEntityHovered(Entity entity) {
    final float HOVER_RADIUS = 0.5f;

    if (entity == null) {
      return false;
    }

    Point mousePoint = Platform.cursor().world();

    return entity
      .fetch(PositionComponent.class)
      .map(
        positionComponent ->
          entity
            .fetch(DrawComponent.class)
            .map(
              dc -> {
                float width = dc.getWidth();
                float height = dc.getHeight();
                Point bottomLeft = positionComponent.position();

                return bottomLeft.x() <= mousePoint.x()
                  && mousePoint.x() <= bottomLeft.x() + width
                  && bottomLeft.y() <= mousePoint.y()
                  && mousePoint.y() <= bottomLeft.y() + height;
              })
            .orElseGet(() -> positionComponent.position().distance(mousePoint) < HOVER_RADIUS))
      .orElse(false);
  }
}
