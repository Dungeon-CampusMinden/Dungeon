package core.platform.litiengine.render;

import core.utils.Point;
import java.util.Objects;

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
   * <p>This preserves the previous cursor semantics while moving the camera-center dependency away
   * from renderer offsets and toward the shared camera state.
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
      (float)
        (focusPosition.x() + ((screenPoint.x() - (screenWidth / 2.0)) / tilePx) + 0.5);
    float worldY =
      (float)
        (focusPosition.y() - ((screenPoint.y() - (screenHeight / 2.0)) / tilePx) - 0.5);

    return new Point(worldX, worldY);
  }
}
