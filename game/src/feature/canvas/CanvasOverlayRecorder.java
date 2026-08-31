package feature.canvas;

import java.util.Objects;

/**
 * {@link CanvasListener} that keeps a {@link CanvasOverlay} in sync with a live {@link CanvasArea}.
 *
 * <p>Attach one to a canvas and every player change - adding, deleting, moving or editing a node -
 * is recorded in the overlay. Call {@link #flush()} to persist the overlay through {@link
 * CanvasStore}, which {@link CanvasUI} does whenever the dialog closes.
 */
public final class CanvasOverlayRecorder implements CanvasListener {

  private final String canvasId;
  private final CanvasOverlay overlay;
  private boolean dirty;

  /**
   * Creates a recorder for the given canvas.
   *
   * @param canvasId the canvas id used for persistence; must not be null
   * @param overlay the overlay to record into; must not be null
   */
  public CanvasOverlayRecorder(String canvasId, CanvasOverlay overlay) {
    this.canvasId = Objects.requireNonNull(canvasId, "canvasId");
    this.overlay = Objects.requireNonNull(overlay, "overlay");
  }

  /**
   * Returns the overlay this recorder writes to.
   *
   * @return the recorded overlay
   */
  public CanvasOverlay overlay() {
    return overlay;
  }

  @Override
  public void onNodeAdded(CanvasArea area, CanvasNode node) {
    overlay.put(node.toState());
    dirty = true;
  }

  @Override
  public void onNodeRemoved(CanvasArea area, CanvasNode node) {
    overlay.remove(node.toState());
    dirty = true;
  }

  @Override
  public void onNodeStateChanged(CanvasArea area, CanvasNode node) {
    overlay.put(node.toState());
    dirty = true;
  }

  /** Writes the overlay to the {@link CanvasStore} if anything changed since the last flush. */
  public void flush() {
    if (!dirty) {
      return;
    }
    CanvasStore.save(canvasId, overlay);
    dirty = false;
  }
}
