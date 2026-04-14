package core.platform;

import core.Entity;
import core.camera.CameraMath;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;

/**
 * Platform adapter interface for camera control and viewport operations.
 *
 * <p>CameraAdapter defines an abstraction for camera-related functionality, including zoom control,
 * focus positioning, viewport metrics, and visibility calculations. It provides default no-op
 * implementations for all methods, allowing implementations to selectively support specific features.
 *
 * <p>Key capability categories:
 * <ul>
 *   <li>Zoom: Control and query zoom level
 *   <li>Focus Position: Set and get a camera focus point
 *   <li>Follow Target: Resolve follow target for dynamic camera control
 *   <li>Viewport Metrics: Query viewport dimensions and world bounds
 *   <li>Visibility: Check if points and entities are visible in the viewport
 *   <li>Hover Detection: Determine if entities are under the mouse cursor
 * </ul>
 *
 * <p>Implementations indicate which features they support via the various {@code supports*()}
 * methods, allowing consumers to gracefully handle partial implementations.
 */
public interface CameraAdapter {

  /**
   * Checks whether this adapter supports zoom control.
   *
   * @return true if zoom functionality is supported, false otherwise
   */
  default boolean supportsZoom() {
    return false;
  }

  /**
   * Gets the current zoom level.
   *
   * <p>The default zoom level is 1.0. Values greater than 1.0 represent magnification;
   * values less than 1.0 represent reduction.
   *
   * @return the current zoom level
   */
  default float zoom() {
    return 1f;
  }

  /**
   * Sets the zoom level.
   *
   * <p>This is a no-op by default. Implementations that support zoom should override this method.
   *
   * @param zoom the zoom level to set (typically 1.0 for normal, > 1.0 for magnified, < 1.0 for reduced)
   */
  default void zoom(float zoom) {
    // no-op by default
  }

  /**
   * Checks whether this adapter supports setting a focus position.
   *
   * @return true if focus position control is supported, false otherwise
   */
  default boolean supportsFocusPosition() {
    return false;
  }

  /**
   * Gets the current camera focus position (center of the camera viewport).
   *
   * <p>The default focus position is (0, 0).
   *
   * @return the current focus position in world coordinates
   */
  default Point focusPosition() {
    return new Point(0, 0);
  }

  /**
   * Sets the camera focus position to center on.
   *
   * <p>This is a no-op by default. Implementations that support focus positioning should override this method.
   *
   * @param focusPosition the world position to focus the camera on
   */
  default void focusPosition(Point focusPosition) {
    // no-op by default
  }

  /**
   * Checks whether this adapter supports resolving dynamic follow targets.
   *
   * @return true if follow target resolution is supported, false otherwise
   */
  default boolean supportsFollowTargetResolution() {
    return false;
  }

  /**
   * Resolves the follow target position, used for dynamic camera tracking.
   *
   * <p>This method can be used to implement camera following behavior by returning the position
   * that the camera should follow (e.g., player position). Default implementation returns the
   * current focus position.
   *
   * @return the position to follow, or the current focus position if not implemented
   */
  default Point resolveFollowTarget() {
    return focusPosition();
  }

  /**
   * Checks whether this adapter supports querying viewport metrics.
   *
   * @return true if viewport metric queries are supported, false otherwise
   */
  default boolean supportsViewportMetrics() {
    return false;
  }

  /**
   * Gets the width of the camera viewport in world coordinates.
   *
   * <p>Default viewport width is 0.
   *
   * @return the viewport width
   */
  default float viewportWidth() {
    return 0f;
  }

  /**
   * Gets the height of the camera viewport in world coordinates.
   *
   * <p>Default viewport height is 0.
   *
   * @return the viewport height
   */
  default float viewportHeight() {
    return 0f;
  }

  /**
   * Calculates the world bounds currently visible by the camera.
   *
   * <p>This method computes a rectangle representing the visible world area based on the current
   * focus position, viewport dimensions, and zoom level.
   *
   * @return a Rectangle defining the visible world bounds
   */
  default Rectangle worldBounds() {
    return CameraMath.worldBounds(focusPosition(), viewportWidth(), viewportHeight(), zoom());
  }

  /**
   * Checks whether a point in world coordinates is visible in the camera viewport.
   *
   * <p>This method uses a default margin of 1.0.
   *
   * @param point the world position to check
   * @return true if the point is visible, false otherwise
   */
  default boolean isPointVisible(Point point) {
    return isPointVisible(point, 1f);
  }

  /**
   * Checks whether a point in world coordinates is visible in the camera viewport with margin.
   *
   * <p>The margin parameter expands the visibility check: a positive margin extends the visible
   * area beyond the viewport boundaries.
   *
   * @param point the world position to check
   * @param margin the margin to extend the viewport bounds by
   * @return true if the point is visible (within the margin), false otherwise
   */
  default boolean isPointVisible(Point point, float margin) {
    return CameraMath.isPointVisible(
      point, focusPosition(), viewportWidth(), viewportHeight(), zoom(), margin);
  }

  /**
   * Checks whether an entity is currently under the mouse cursor (hovered).
   *
   * <p>This method performs hover detection by checking if the mouse position falls within the
   * entity's draw bounds (if available) or within a hover radius of the entity's position.
   *
   * <p>Requirements:
   * <ul>
   *   <li>Entity must have a PositionComponent
   *   <li>If an entity has a DrawComponent, its bounds are used for hover detection
   *   <li>Otherwise, a default hover radius of 0.5 world units is used
   * </ul>
   *
   * @param entity the entity to check for hover (null is safely handled)
   * @return true if the entity is under the mouse cursor, false otherwise
   */
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
