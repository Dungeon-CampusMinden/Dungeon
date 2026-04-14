package core.ui.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Central registry for managing UI overlay instances.
 *
 * <p>UiOverlayRegistry maintains a global list of active overlays and provides synchronized
 * methods for registering, unregistering, and rendering overlays. It serves as a single point
 * of control for overlay lifecycle and rendering order.
 *
 * <p>Key features:
 * <ul>
 *   <li>Thread-safe overlay registration and removal
 *   <li>Preventing duplicate overlay entries
 *   <li>Z-order management through front-to-back ordering
 *   <li>Batch rendering of visible overlays
 * </ul>
 *
 * <p>All public methods are synchronized to ensure thread safety. Overlays are rendered in the
 * order they appear in the registry, with later entries appearing on top.
 *
 * <p>This class is not instantiable; all methods are static.
 */
public final class UiOverlayRegistry {

  private static final List<UiOverlay> OVERLAYS = new ArrayList<>();

  private UiOverlayRegistry() {}

  /**
   * Registers an overlay in the registry.
   *
   * <p>The overlay is added to the list of managed overlays. If the overlay is already
   * registered or is null, no action is taken.
   *
   * @param overlay the overlay to register (null is safely ignored)
   */
  public static synchronized void add(UiOverlay overlay) {
    if (overlay != null && !OVERLAYS.contains(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  /**
   * Unregisters an overlay from the registry.
   *
   * <p>Removes the overlay from the list of managed overlays. If the overlay is not registered,
   * no action is taken.
   *
   * @param overlay the overlay to unregister
   */
  public static synchronized void remove(UiOverlay overlay) {
    OVERLAYS.remove(overlay);
  }

  /**
   * Checks whether an overlay is currently registered.
   *
   * @param overlay the overlay to check
   * @return true if the overlay is registered, false otherwise
   */
  public static synchronized boolean contains(UiOverlay overlay) {
    return OVERLAYS.contains(overlay);
  }

  /**
   * Moves an overlay to the front of the rendering order.
   *
   * <p>This operation repositions the overlay to be rendered last (on top) of all other overlays.
   * If the overlay is not registered, no action is taken. Null inputs are safely ignored.
   *
   * @param overlay the overlay to bring to front (null is safely ignored)
   */
  public static synchronized void toFront(UiOverlay overlay) {
    if (overlay == null) {
      return;
    }
    if (OVERLAYS.remove(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  /**
   * Renders all visible overlays on the given graphics context.
   *
   * <p>Iterates through all registered overlays in order and renders each visible overlay.
   * Hidden overlays are skipped. Overlays are rendered back-to-front based on their position
   * in the registry.
   *
   * @param g the Graphics2D context to render on
   */
  public static synchronized void renderAll(Graphics2D g) {
    for (UiOverlay overlay : List.copyOf(OVERLAYS)) {
      if (overlay.visible()) {
        overlay.render(g);
      }
    }
  }
}
