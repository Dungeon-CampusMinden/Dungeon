package feature.canvas;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client side persistence for {@link CanvasOverlay}s.
 *
 * <p>Overlays are stored per canvas id and outlive the dialog they belong to, which is what makes
 * local player changes survive closing and reopening a canvas.
 *
 * <p>The default implementation keeps overlays in memory for the lifetime of the process. The
 * indirection through this class exists so a save game backed implementation can be plugged in
 * later via {@link #backend(Backend)} without touching any canvas code.
 */
public final class CanvasStore {

  /** Pluggable persistence backend for canvas overlays. */
  public interface Backend {

    /**
     * Loads the overlay of a canvas.
     *
     * @param canvasId the canvas id
     * @return the stored overlay, or a fresh empty one when nothing was stored yet
     */
    CanvasOverlay load(String canvasId);

    /**
     * Stores the overlay of a canvas.
     *
     * @param canvasId the canvas id
     * @param overlay the overlay to store
     */
    void save(String canvasId, CanvasOverlay overlay);

    /**
     * Discards the stored overlay of a canvas.
     *
     * @param canvasId the canvas id
     */
    void clear(String canvasId);
  }

  private static volatile Backend backend = new InMemoryBackend();

  private CanvasStore() {}

  /**
   * Replaces the persistence backend.
   *
   * @param newBackend the backend to use; must not be null
   */
  public static void backend(Backend newBackend) {
    backend = Objects.requireNonNull(newBackend, "backend");
  }

  /**
   * Loads the overlay of a canvas.
   *
   * @param canvasId the canvas id; must not be null
   * @return the stored overlay, or a fresh empty one
   */
  public static CanvasOverlay load(String canvasId) {
    Objects.requireNonNull(canvasId, "canvasId");
    return backend.load(canvasId);
  }

  /**
   * Stores the overlay of a canvas.
   *
   * @param canvasId the canvas id; must not be null
   * @param overlay the overlay to store; must not be null
   */
  public static void save(String canvasId, CanvasOverlay overlay) {
    Objects.requireNonNull(canvasId, "canvasId");
    Objects.requireNonNull(overlay, "overlay");
    backend.save(canvasId, overlay);
  }

  /**
   * Discards all local changes stored for a canvas.
   *
   * @param canvasId the canvas id; must not be null
   */
  public static void clear(String canvasId) {
    Objects.requireNonNull(canvasId, "canvasId");
    backend.clear(canvasId);
  }

  /** In-memory backend that keeps overlays for the lifetime of the process. */
  public static final class InMemoryBackend implements Backend {
    private final Map<String, CanvasSnapshot> stored = new ConcurrentHashMap<>();

    @Override
    public CanvasOverlay load(String canvasId) {
      CanvasSnapshot snapshot = stored.get(canvasId);
      return snapshot == null ? new CanvasOverlay() : CanvasOverlay.fromSnapshot(snapshot);
    }

    @Override
    public void save(String canvasId, CanvasOverlay overlay) {
      if (overlay.isEmpty()) {
        stored.remove(canvasId);
        return;
      }
      stored.put(canvasId, overlay.toSnapshot());
    }

    @Override
    public void clear(String canvasId) {
      stored.remove(canvasId);
    }

    /**
     * Returns the canvas ids that currently have stored changes.
     *
     * @return the stored canvas ids
     */
    public List<String> storedCanvasIds() {
      return List.copyOf(stored.keySet());
    }
  }
}
