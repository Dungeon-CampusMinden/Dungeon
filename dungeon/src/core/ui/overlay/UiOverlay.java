package core.ui.overlay;

import java.awt.Graphics2D;

/**
 * Interface for overlay-based UI elements that render on top of the game scene.
 *
 * <p>UiOverlay defines the contract for UI components that are rendered as overlays, independent of
 * the main stage hierarchy. Overlays are typically used for HUDs, menus, dialogs, and other
 * floating UI elements that need to appear above game content.
 *
 * <p>Key properties:
 *
 * <ul>
 *   <li>Rendering: Custom drawing logic via {@link #render(Graphics2D)}
 *   <li>Position: X and Y coordinates for placement
 *   <li>Size: Width and height dimensions
 *   <li>Visibility: Boolean flag to show/hide the overlay
 *   <li>Hit testing: Support for point-in-overlay detection
 * </ul>
 */
public interface UiOverlay {

  /**
   * Renders this overlay on the given graphics context.
   *
   * <p>Implementations should draw all visual content for the overlay using the provided Graphics2D
   * object.
   *
   * @param g the Graphics2D context to render on
   */
  void render(Graphics2D g);

  /**
   * Gets the x-coordinate of this overlay.
   *
   * @return the x position in pixels
   */
  int x();

  /**
   * Sets the x-coordinate of this overlay.
   *
   * @param x the x position in pixels
   */
  void x(int x);

  /**
   * Gets the y-coordinate of this overlay.
   *
   * @return the y position in pixels
   */
  int y();

  /**
   * Sets the y-coordinate of this overlay.
   *
   * @param y the y position in pixels
   */
  void y(int y);

  /**
   * Gets the width of this overlay.
   *
   * @return the width in pixels
   */
  int width();

  /**
   * Sets the width of this overlay.
   *
   * @param width the width in pixels
   */
  void width(int width);

  /**
   * Gets the height of this overlay.
   *
   * @return the height in pixels
   */
  int height();

  /**
   * Sets the height of this overlay.
   *
   * @param height the height in pixels
   */
  void height(int height);

  /**
   * Checks whether this overlay is currently visible.
   *
   * @return true if the overlay is visible, false if hidden
   */
  boolean visible();

  /**
   * Sets the visibility of this overlay.
   *
   * @param visible true to show the overlay, false to hide it
   */
  void visible(boolean visible);

  /**
   * Checks whether the given pixel coordinates are contained within this overlay's bounds.
   *
   * <p>This method performs an inclusive boundary check: coordinates exactly on the edges
   * (including the right and bottom boundaries) are considered contained.
   *
   * @param px the x pixel coordinate to check
   * @param py the y pixel coordinate to check
   * @return true if (px, py) is within the overlay's rectangular bounds, false otherwise
   */
  default boolean contains(int px, int py) {
    return px >= x() && px <= x() + width() && py >= y() && py <= y() + height();
  }
}
