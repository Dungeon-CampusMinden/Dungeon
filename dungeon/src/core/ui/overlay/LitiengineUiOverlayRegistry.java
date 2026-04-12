package core.ui.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Global registry for custom LITIENGINE screen overlays.
 *
 * <p>Rendered by {@code EcsRenderScreen} after the ECS render phase.
 */
public final class LitiengineUiOverlayRegistry {

  private static final List<LitiengineUiOverlay> OVERLAYS = new ArrayList<>();

  private LitiengineUiOverlayRegistry() {}

  public static synchronized void add(LitiengineUiOverlay overlay) {
    if (overlay != null && !OVERLAYS.contains(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  public static synchronized void remove(LitiengineUiOverlay overlay) {
    OVERLAYS.remove(overlay);
  }

  public static synchronized boolean contains(LitiengineUiOverlay overlay) {
    return OVERLAYS.contains(overlay);
  }

  public static synchronized void toFront(LitiengineUiOverlay overlay) {
    if (overlay == null) {
      return;
    }
    if (OVERLAYS.remove(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  public static synchronized void renderAll(Graphics2D g) {
    for (LitiengineUiOverlay overlay : List.copyOf(OVERLAYS)) {
      if (overlay.visible()) {
        overlay.render(g);
      }
    }
  }
}
