package core.camera;

import core.utils.Point;
import java.util.Objects;
import java.util.Optional;

/** Shared camera view data for the LITIENGINE backend. */
public final class LitiengineCameraViews {
  private static final View DEFAULT_VIEW = new View(0, 0, 0, 32);

  private LitiengineCameraViews() {}

  public record View(double offsetX, double offsetY, int levelHeight, int tilePx) {}

  private static volatile View CURRENT = DEFAULT_VIEW;

  public static View get() {
    return CURRENT;
  }

  public static void set(double offsetX, double offsetY, int levelHeight, int tilePx) {
    CURRENT = new View(offsetX, offsetY, levelHeight, tilePx);
  }

  public static Optional<View> activeView() {
    View view = get();
    if (view == null || view.tilePx() <= 0) {
      return Optional.empty();
    }
    return Optional.of(view);
  }

  /** Resets the shared camera view to its default state. */
  public static void reset() {
    CURRENT = DEFAULT_VIEW;
  }

  /**
   * Converts a screen-space pixel position to a world-space position using the shared camera focus.
   *
   * <p>The shared focus represents the effective camera center in world units. The current view
   * still provides the active render scale via {@link View#tilePx()}.
   *
   * @param screenPoint screen-space cursor position in pixels
   * @param focusPosition effective shared camera focus in world units
   * @param screenWidth current screen width in pixels
   * @param screenHeight current screen height in pixels
   * @return corresponding world-space cursor position
   */
  public static Point screenToWorld(
    Point screenPoint, Point focusPosition, int screenWidth, int screenHeight) {
    Objects.requireNonNull(screenPoint, "screenPoint");
    Objects.requireNonNull(focusPosition, "focusPosition");

    View view = get();
    int tilePx = Math.max(1, view.tilePx());

    float worldX =
      (float) (focusPosition.x() + ((screenPoint.x() - (screenWidth / 2.0)) / tilePx) + 0.5);
    float worldY =
      (float) (focusPosition.y() - ((screenPoint.y() - (screenHeight / 2.0)) / tilePx) - 0.5);

    return new Point(worldX, worldY);
  }

  /**
   * Converts a world-space tile position to the corresponding screen-space tile origin.
   *
   * <p>The returned point corresponds to the top-left screen position of the tile cell that
   * contains the given world point.
   *
   * @param worldPoint world-space point in tile/world units
   * @return corresponding screen-space tile origin in pixels
   */
  public static Point worldToScreen(Point worldPoint) {
    Objects.requireNonNull(worldPoint, "worldPoint");

    View view = get();
    int tilePx = Math.max(1, view.tilePx());
    int levelHeight = view.levelHeight();

    float screenX = (float) (worldPoint.x() * tilePx + view.offsetX());
    float screenY =
      levelHeight > 0
        ? (float) (((levelHeight - 1) - worldPoint.y()) * tilePx + view.offsetY())
        : (float) (worldPoint.y() * tilePx + view.offsetY());

    return new Point(screenX, screenY);
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

  /**
   * Converts a world-space length to screen-space pixels using the current tile scale.
   *
   * @param worldLength length in world units
   * @return corresponding pixel length, at least 1
   */
  public static int worldLengthToScreen(float worldLength) {
    return Math.max(1, Math.round(worldLength * Math.max(1, get().tilePx())));
  }
}
