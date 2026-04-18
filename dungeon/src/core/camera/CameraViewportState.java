package core.camera;

import core.utils.Point;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared viewport/projection state for the active camera rendering path.
 *
 * <p>This state stores screen offsets and tile scaling that are needed to convert between
 * world-space and screen-space coordinates.
 */
public final class CameraViewportState {
  private static final Viewport DEFAULT_VIEWPORT = new Viewport(0, 0, 0, 32);

  private CameraViewportState() {}

  /**
   * Represents the shared viewport/projection state for camera rendering.
   *
   * @param offsetX screen offset for X coordinate in pixels
   * @param offsetY screen offset for Y coordinate in pixels
   * @param levelHeight height of the current level in tiles
   * @param tilePx size of a tile in pixels
   */
  public record Viewport(double offsetX, double offsetY, int levelHeight, int tilePx) {}

  private static volatile Viewport CURRENT = DEFAULT_VIEWPORT;

  /**
   * Gets the current viewport state.
   *
   * @return the current viewport configuration
   */
  public static Viewport get() {
    return CURRENT;
  }

  /**
   * Sets the current viewport state.
   *
   * @param offsetX screen offset for X coordinate in pixels
   * @param offsetY screen offset for Y coordinate in pixels
   * @param levelHeight height of the current level in tiles
   * @param tilePx size of a tile in pixels
   */
  public static void set(double offsetX, double offsetY, int levelHeight, int tilePx) {
    CURRENT = new Viewport(offsetX, offsetY, levelHeight, tilePx);
  }

  /**
   * Gets the current active viewport if it is valid.
   *
   * <p>A viewport is considered valid if it has been set and has a positive tile pixel size.
   *
   * @return an Optional containing the current viewport if valid, otherwise empty
   */
  public static Optional<Viewport> activeViewport() {
    Viewport viewport = get();
    if (viewport == null || viewport.tilePx() <= 0) {
      return Optional.empty();
    }
    return Optional.of(viewport);
  }

  /** Resets the shared viewport state to its default state. */
  public static void reset() {
    CURRENT = DEFAULT_VIEWPORT;
  }

  /**
   * Converts a screen-space pixel position to a world-space position using the current viewport.
   *
   * @param screenPoint screen-space cursor position in pixels
   * @param focusPosition effective shared camera focus in world units; kept for source
   *     compatibility
   * @return corresponding world-space cursor position
   */
  public static Point screenToWorld(Point screenPoint, Point focusPosition) {
    Objects.requireNonNull(screenPoint, "screenPoint");
    Objects.requireNonNull(focusPosition, "focusPosition");

    Viewport viewport = get();
    int tilePx = Math.max(1, viewport.tilePx());
    double screenTileY = (screenPoint.y() - viewport.offsetY()) / tilePx;

    float worldX = (float) ((screenPoint.x() - viewport.offsetX()) / tilePx);
    float worldY =
      viewport.levelHeight() > 0
        ? (float) (viewport.levelHeight() - screenTileY)
        : (float) screenTileY;

    return new Point(worldX, worldY);
  }

  /**
   * Converts a world-space tile position to the corresponding screen-space tile origin.
   *
   * @param worldPoint world-space point in tile/world units
   * @return corresponding screen-space tile origin in pixels
   */
  public static Point worldToScreen(Point worldPoint) {
    Objects.requireNonNull(worldPoint, "worldPoint");

    Viewport viewport = get();
    int tilePx = Math.max(1, viewport.tilePx());
    int levelHeight = viewport.levelHeight();

    float screenX = (float) (worldPoint.x() * tilePx + viewport.offsetX());
    float screenY =
      levelHeight > 0
        ? (float) (((levelHeight - 1) - worldPoint.y()) * tilePx + viewport.offsetY())
        : (float) (worldPoint.y() * tilePx + viewport.offsetY());

    return new Point(screenX, screenY);
  }

  /**
   * Converts a world-space length to screen-space pixels using the current tile scale.
   *
   * @param worldLength length in world units
   * @return corresponding pixel length, at least 1
   */
  public static int worldLengthToScreen(float worldLength) {
    return Math.max(1, Math.round(worldLength * Math.max(1, get().tilePx())));
  }

  /**
   * Converts a world-space tile position to the corresponding screen-space tile center.
   *
   * @param worldPoint world-space point in tile/world units
   * @return corresponding screen-space tile center in pixels
   */
  public static Point worldCenterToScreen(Point worldPoint) {
    Point topLeft = worldToScreen(worldPoint);
    int tilePx = Math.max(1, get().tilePx());
    float halfTile = tilePx * 0.5f;
    return new Point(topLeft.x() + halfTile, topLeft.y() + halfTile);
  }

}
