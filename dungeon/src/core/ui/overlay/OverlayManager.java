package core.ui.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of {@link UiOverlay} objects and provides methods to manipulate and render them.
 *
 * <p>This class is designed as a utility for handling UI overlays that need to be rendered on top
 * of the game scene. It maintains a globally accessible, synchronized list of overlays, allowing
 * for adding, removing, checking, reordering, and rendering these elements.
 *
 * <p>The {@code OverlayManager} class is thread-safe due to the use of synchronized methods.
 * However, it is a final class with a private constructor, preventing instantiation and extension.
 */
public final class OverlayManager {

  private static final List<UiOverlay> OVERLAYS = new ArrayList<>();

  private OverlayManager() {}

  /**
   * Adds the specified overlay to the collection if it is not already present.
   * The method ensures thread safety by using synchronization.
   *
   * @param overlay the {@link UiOverlay} instance to add; must not be null.
   *                If the overlay is already present in the collection, it will not be added again.
   */
  public static synchronized void add(UiOverlay overlay) {
    if (overlay != null && !OVERLAYS.contains(overlay)) {
      OVERLAYS.add(overlay);
    }
  }

  /**
   * Removes the specified overlay from the collection if it is present.
   * The method ensures thread safety by using synchronization.
   *
   * @param overlay the {@link UiOverlay} instance to remove; must not be null.
   *                If the overlay is not in the collection, no action will be taken.
   */
  public static synchronized void remove(UiOverlay overlay) {
    OVERLAYS.remove(overlay);
  }

  /**
   * Checks whether the given {@link UiOverlay} instance is present in the collection of overlays.
   * The method ensures thread safety by using synchronization.
   *
   * @param overlay the {@link UiOverlay} instance to check for; must not be null.
   * @return true if the overlay is present in the collection, false otherwise.
   */
  public static synchronized boolean contains(UiOverlay overlay) {
    return OVERLAYS.contains(overlay);
  }

  /**
   * Moves the specified {@link UiOverlay} instance to the front of the overlay collection.
   *
   * <p>If the overlay is already present in the collection, it will be removed and re-added
   * to the end of the list, ensuring it is rendered above other overlays. If the overlay
   * is not present, no action will be taken.
   *
   * @param overlay the {@link UiOverlay} instance to move to the front; must not be null.
   *                If the argument is null, the method does nothing.
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
   * Renders all visible {@link UiOverlay} instances from the overlay collection onto the provided
   * {@link Graphics2D} context.
   *
   * <p>This method iterates over a copy of the overlay collection to ensure thread safety
   * during iteration. Only overlays that are visible (determined by {@link UiOverlay#visible()})
   * will be rendered by invoking their {@link UiOverlay#render(Graphics2D)} method.
   *
   * @param g the {@link Graphics2D} context to render the overlays onto; must not be null
   */
  public static synchronized void renderAll(Graphics2D g) {
    for (UiOverlay overlay : List.copyOf(OVERLAYS)) {
      if (overlay.visible()) {
        overlay.render(g);
      }
    }
  }
}
