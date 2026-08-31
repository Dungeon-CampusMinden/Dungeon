package feature.canvas;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

/**
 * Mutable configuration for a {@link CanvasArea}.
 *
 * <p>All values have sensible defaults, so a canvas can be created without touching this class at
 * all. Setters are fluent, which makes the options easy to configure inline from {@link
 * CanvasMaker.Builder#options(java.util.function.Consumer)}:
 *
 * <pre>{@code
 * CanvasMaker.builder("demo")
 *     .options(o -> o.zoom(0.25f, 4f).grid(32f, true).snapToGrid(true))
 *     .build();
 * }</pre>
 */
public final class CanvasOptions {

  /** Default minimum zoom factor. */
  public static final float DEFAULT_MIN_ZOOM = 0.25f;

  /** Default maximum zoom factor. */
  public static final float DEFAULT_MAX_ZOOM = 4f;

  /** Default zoom factor applied when the canvas is opened. */
  public static final float DEFAULT_ZOOM = 1f;

  /** Default grid cell size in world units. */
  public static final float DEFAULT_GRID_SIZE = 32f;

  private float minZoom = DEFAULT_MIN_ZOOM;
  private float maxZoom = DEFAULT_MAX_ZOOM;
  private float initialZoom = DEFAULT_ZOOM;
  private float zoomSpeed = 0.12f;

  private boolean gridEnabled = true;
  private float gridSize = DEFAULT_GRID_SIZE;
  private boolean snapToGrid = false;

  private int panButton = Input.Buttons.MIDDLE;
  private boolean panWithSpace = true;

  private Color backgroundColor = new Color(1f, 1f, 1f, 1f);
  private Color gridColor = new Color(0.82f, 0.82f, 0.82f, 1f);
  private Color selectionColor = new Color(0.36f, 0.63f, 0.88f, 1f);
  private Color dropTargetColor = new Color(0.16f, 1f, 0f, 1f);

  private boolean selectionEnabled = true;
  private boolean multiSelectEnabled = true;
  private boolean rubberBandEnabled = true;
  private boolean keyboardShortcutsEnabled = true;
  private float nudgeStep = 1f;

  private boolean pruneStaleOverrides = true;
  private boolean pruneStaleTombstones = false;

  /** Creates a new options instance with all defaults applied. */
  public CanvasOptions() {}

  /**
   * Sets the allowed zoom range.
   *
   * @param min the minimum zoom factor; must be greater than zero
   * @param max the maximum zoom factor; must be greater than or equal to {@code min}
   * @return this instance for chaining
   */
  public CanvasOptions zoom(float min, float max) {
    if (min <= 0f) {
      throw new IllegalArgumentException("minZoom must be > 0, was " + min);
    }
    if (max < min) {
      throw new IllegalArgumentException("maxZoom must be >= minZoom");
    }
    this.minZoom = min;
    this.maxZoom = max;
    this.initialZoom = clampZoom(this.initialZoom);
    return this;
  }

  /**
   * Sets the zoom factor the canvas starts with.
   *
   * @param zoom the initial zoom factor; clamped into the configured zoom range
   * @return this instance for chaining
   */
  public CanvasOptions initialZoom(float zoom) {
    this.initialZoom = clampZoom(zoom);
    return this;
  }

  /**
   * Sets how strongly one mouse wheel step changes the zoom factor.
   *
   * @param speed the relative zoom change per wheel step; must be greater than zero
   * @return this instance for chaining
   */
  public CanvasOptions zoomSpeed(float speed) {
    if (speed <= 0f) {
      throw new IllegalArgumentException("zoomSpeed must be > 0, was " + speed);
    }
    this.zoomSpeed = speed;
    return this;
  }

  /**
   * Configures the background grid.
   *
   * @param size the grid cell size in world units; must be greater than zero
   * @param enabled whether the grid is rendered
   * @return this instance for chaining
   */
  public CanvasOptions grid(float size, boolean enabled) {
    if (size <= 0f) {
      throw new IllegalArgumentException("gridSize must be > 0, was " + size);
    }
    this.gridSize = size;
    this.gridEnabled = enabled;
    return this;
  }

  /**
   * Sets whether dropped nodes snap to the grid.
   *
   * @param snap true to snap node positions to the grid on drop
   * @return this instance for chaining
   */
  public CanvasOptions snapToGrid(boolean snap) {
    this.snapToGrid = snap;
    return this;
  }

  /**
   * Sets the mouse button used to pan the canvas.
   *
   * @param button a {@link com.badlogic.gdx.Input.Buttons} constant
   * @return this instance for chaining
   */
  public CanvasOptions panButton(int button) {
    this.panButton = button;
    return this;
  }

  /**
   * Sets whether holding space additionally enables panning with the left mouse button.
   *
   * @param enabled true to allow space + left drag panning
   * @return this instance for chaining
   */
  public CanvasOptions panWithSpace(boolean enabled) {
    this.panWithSpace = enabled;
    return this;
  }

  /**
   * Sets the canvas background color.
   *
   * @param color the background color; must not be null
   * @return this instance for chaining
   */
  public CanvasOptions backgroundColor(Color color) {
    this.backgroundColor = new Color(color);
    return this;
  }

  /**
   * Sets the grid line color.
   *
   * @param color the grid color; must not be null
   * @return this instance for chaining
   */
  public CanvasOptions gridColor(Color color) {
    this.gridColor = new Color(color);
    return this;
  }

  /**
   * Sets the color used for selection outlines and the rubber band.
   *
   * @param color the selection color; must not be null
   * @return this instance for chaining
   */
  public CanvasOptions selectionColor(Color color) {
    this.selectionColor = new Color(color);
    return this;
  }

  /**
   * Sets the color used to highlight the node a dragged node would be dropped onto.
   *
   * @param color the drop target color; must not be null
   * @return this instance for chaining
   */
  public CanvasOptions dropTargetColor(Color color) {
    this.dropTargetColor = new Color(color);
    return this;
  }

  /**
   * Enables or disables node selection entirely.
   *
   * @param enabled true to allow selecting nodes
   * @return this instance for chaining
   */
  public CanvasOptions selectionEnabled(boolean enabled) {
    this.selectionEnabled = enabled;
    return this;
  }

  /**
   * Enables or disables selecting more than one node at a time.
   *
   * @param enabled true to allow ctrl/shift multi-select
   * @return this instance for chaining
   */
  public CanvasOptions multiSelectEnabled(boolean enabled) {
    this.multiSelectEnabled = enabled;
    return this;
  }

  /**
   * Enables or disables rubber band selection by dragging on empty canvas.
   *
   * @param enabled true to allow rubber band selection
   * @return this instance for chaining
   */
  public CanvasOptions rubberBandEnabled(boolean enabled) {
    this.rubberBandEnabled = enabled;
    return this;
  }

  /**
   * Enables or disables the built-in keyboard shortcuts (delete and arrow key nudging).
   *
   * @param enabled true to enable keyboard shortcuts
   * @return this instance for chaining
   */
  public CanvasOptions keyboardShortcutsEnabled(boolean enabled) {
    this.keyboardShortcutsEnabled = enabled;
    return this;
  }

  /**
   * Sets how far one arrow key press moves the selected nodes.
   *
   * @param step the step size in world units; must be greater than zero
   * @return this instance for chaining
   */
  public CanvasOptions nudgeStep(float step) {
    if (step <= 0f) {
      throw new IllegalArgumentException("nudgeStep must be > 0, was " + step);
    }
    this.nudgeStep = step;
    return this;
  }

  /**
   * Sets whether overlay overrides for node ids that no longer exist in the server defaults are
   * discarded when merging.
   *
   * @param prune true to discard stale overrides
   * @return this instance for chaining
   * @see CanvasMerger
   */
  public CanvasOptions pruneStaleOverrides(boolean prune) {
    this.pruneStaleOverrides = prune;
    return this;
  }

  /**
   * Sets whether tombstones for node ids that are currently absent from the server defaults are
   * discarded when merging.
   *
   * <p>Defaults to {@code false} so a default node that temporarily disappears and later comes back
   * stays deleted, matching the player's intent.
   *
   * @param prune true to discard stale tombstones
   * @return this instance for chaining
   * @see CanvasMerger
   */
  public CanvasOptions pruneStaleTombstones(boolean prune) {
    this.pruneStaleTombstones = prune;
    return this;
  }

  /**
   * Clamps the given zoom factor into the configured zoom range.
   *
   * @param zoom the zoom factor to clamp
   * @return the clamped zoom factor
   */
  public float clampZoom(float zoom) {
    return Math.max(minZoom, Math.min(maxZoom, zoom));
  }

  /**
   * Returns the minimum zoom factor.
   *
   * @return the minimum zoom factor
   */
  public float minZoom() {
    return minZoom;
  }

  /**
   * Returns the maximum zoom factor.
   *
   * @return the maximum zoom factor
   */
  public float maxZoom() {
    return maxZoom;
  }

  /**
   * Returns the zoom factor applied when the canvas is opened.
   *
   * @return the initial zoom factor
   */
  public float initialZoom() {
    return initialZoom;
  }

  /**
   * Returns the relative zoom change per mouse wheel step.
   *
   * @return the zoom speed
   */
  public float zoomSpeed() {
    return zoomSpeed;
  }

  /**
   * Returns whether the background grid is rendered.
   *
   * @return true if the grid is enabled
   */
  public boolean gridEnabled() {
    return gridEnabled;
  }

  /**
   * Returns the grid cell size in world units.
   *
   * @return the grid size
   */
  public float gridSize() {
    return gridSize;
  }

  /**
   * Returns whether dropped nodes snap to the grid.
   *
   * @return true if snapping is enabled
   */
  public boolean snapToGrid() {
    return snapToGrid;
  }

  /**
   * Returns the mouse button used for panning.
   *
   * @return the pan mouse button
   */
  public int panButton() {
    return panButton;
  }

  /**
   * Returns whether space + left drag pans the canvas.
   *
   * @return true if space panning is enabled
   */
  public boolean panWithSpace() {
    return panWithSpace;
  }

  /**
   * Returns the canvas background color.
   *
   * @return the background color
   */
  public Color backgroundColor() {
    return backgroundColor;
  }

  /**
   * Returns the grid line color.
   *
   * @return the grid color
   */
  public Color gridColor() {
    return gridColor;
  }

  /**
   * Returns the selection outline color.
   *
   * @return the selection color
   */
  public Color selectionColor() {
    return selectionColor;
  }

  /**
   * Returns the drop target highlight color.
   *
   * @return the drop target color
   */
  public Color dropTargetColor() {
    return dropTargetColor;
  }

  /**
   * Returns whether nodes can be selected.
   *
   * @return true if selection is enabled
   */
  public boolean selectionEnabled() {
    return selectionEnabled;
  }

  /**
   * Returns whether more than one node can be selected at a time.
   *
   * @return true if multi-select is enabled
   */
  public boolean multiSelectEnabled() {
    return multiSelectEnabled;
  }

  /**
   * Returns whether rubber band selection is enabled.
   *
   * @return true if rubber band selection is enabled
   */
  public boolean rubberBandEnabled() {
    return rubberBandEnabled;
  }

  /**
   * Returns whether the built-in keyboard shortcuts are enabled.
   *
   * @return true if keyboard shortcuts are enabled
   */
  public boolean keyboardShortcutsEnabled() {
    return keyboardShortcutsEnabled;
  }

  /**
   * Returns how far one arrow key press moves the selection.
   *
   * @return the nudge step in world units
   */
  public float nudgeStep() {
    return nudgeStep;
  }

  /**
   * Returns whether stale overlay overrides are pruned on merge.
   *
   * @return true if stale overrides are pruned
   */
  public boolean pruneStaleOverrides() {
    return pruneStaleOverrides;
  }

  /**
   * Returns whether stale tombstones are pruned on merge.
   *
   * @return true if stale tombstones are pruned
   */
  public boolean pruneStaleTombstones() {
    return pruneStaleTombstones;
  }
}
