package feature.canvas;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client side, in-memory persistence for the local changes a player made to a canvas.
 *
 * <p>Changes are stored per canvas id and outlive the dialog they belong to, which is what makes a
 * player's arrangement survive closing and reopening a canvas. They are deliberately <em>not</em>
 * written to disk: restarting the game resets every canvas back to the server defaults.
 *
 * @see CanvasSnapshot#changesOf(CanvasSnapshot, List, CanvasSnapshot, CanvasOptions)
 * @see CanvasSnapshot#mergeWith(CanvasSnapshot, CanvasOptions)
 */
public final class CanvasStore {

  private static final Map<String, CanvasSnapshot> STORED = new ConcurrentHashMap<>();

  private CanvasStore() {}

  /**
   * Loads the local changes of a canvas.
   *
   * @param canvasId the canvas id; must not be null
   * @return the stored changes, or an empty snapshot when nothing was stored yet
   */
  public static CanvasSnapshot load(String canvasId) {
    Objects.requireNonNull(canvasId, "canvasId");
    return STORED.getOrDefault(canvasId, CanvasSnapshot.empty());
  }

  /**
   * Stores the local changes of a canvas.
   *
   * <p>An empty snapshot removes the entry instead of storing it, so a canvas the player reset by
   * hand does not keep an empty record around.
   *
   * @param canvasId the canvas id; must not be null
   * @param changes the changes to store; must not be null
   */
  public static void save(String canvasId, CanvasSnapshot changes) {
    Objects.requireNonNull(canvasId, "canvasId");
    Objects.requireNonNull(changes, "changes");
    if (changes.size() == 0) {
      STORED.remove(canvasId);
      return;
    }
    STORED.put(canvasId, changes);
  }

  /**
   * Discards all local changes stored for a canvas.
   *
   * @param canvasId the canvas id; must not be null
   */
  public static void clear(String canvasId) {
    Objects.requireNonNull(canvasId, "canvasId");
    STORED.remove(canvasId);
  }

  /** Discards the local changes of every canvas. */
  public static void clearAll() {
    STORED.clear();
  }

  /**
   * Returns the canvas ids that currently have stored changes.
   *
   * @return the stored canvas ids
   */
  public static List<String> storedCanvasIds() {
    return List.copyOf(STORED.keySet());
  }
}
