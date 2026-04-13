package core.ui.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Global registry for custom LITIENGINE screen overlays.
 *
 * <p>Rendered by {@code EcsRenderScreen} after the ECS render phase.
 */
public final class UiOverlayRegistry {

  private static final List<UiOverlay> OVERLAYS = new ArrayList<>();

  private UiOverlayRegistry() {}

  public static synchronized void add(UiOverlay overlay) {
    if (overlay != null && !OVERLAYS.contains(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  public static synchronized void remove(UiOverlay overlay) {
    OVERLAYS.remove(overlay);
  }

  public static synchronized boolean contains(UiOverlay overlay) {
    return OVERLAYS.contains(overlay);
  }

  public static synchronized void toFront(UiOverlay overlay) {
    if (overlay == null) {
      return;
    }
    if (OVERLAYS.remove(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  public static synchronized void renderAll(Graphics2D g) {
    for (UiOverlay overlay : List.copyOf(OVERLAYS)) {
      if (overlay.visible()) {
        overlay.render(g);
      }
    }
  }
}
