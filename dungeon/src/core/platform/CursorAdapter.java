package core.platform;

import core.utils.Point;

/**
 * Platform adapter interface for cursor/mouse input.
 *
 * <p>CursorAdapter provides access to cursor position information in both screen and world
 * coordinate systems. It abstracts away platform-specific input handling, allowing the engine
 * to query cursor position independently of the underlying framework.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Reporting screen-space cursor coordinates (pixel-based)
 *   <li>Reporting world-space cursor coordinates (game-world coordinates)
 * </ul>
 */
public interface CursorAdapter {

  /**
   * Gets the cursor x-coordinate in screen space (pixels).
   *
   * @return the x-position of the cursor on the screen
   */
  int screenX();

  /**
   * Gets the cursor y-coordinate in screen space (pixels).
   *
   * @return the y-position of the cursor on the screen
   */
  int screenY();

  /**
   * Gets the cursor position in world space coordinates.
   *
   * <p>This method performs any necessary coordinate transformations (e.g., applying camera
   * offset and zoom) to convert the screen-space cursor position to world space.
   *
   * @return the cursor position in the world coordinates as a Point
   */
  Point world();
}
