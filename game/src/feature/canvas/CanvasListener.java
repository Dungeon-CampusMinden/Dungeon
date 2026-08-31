package feature.canvas;

import java.util.Set;

/**
 * Callback interface for observing structural changes on a {@link CanvasArea}.
 *
 * <p>All methods have empty default implementations, so implementors only override what they care
 * about.
 */
public interface CanvasListener {

  /**
   * Called after a node was added to the canvas.
   *
   * @param area the canvas the node was added to
   * @param node the added node
   */
  default void onNodeAdded(CanvasArea area, CanvasNode node) {}

  /**
   * Called after a node was removed from the canvas.
   *
   * @param area the canvas the node was removed from
   * @param node the removed node
   */
  default void onNodeRemoved(CanvasArea area, CanvasNode node) {}

  /**
   * Called after the selection changed.
   *
   * @param area the canvas whose selection changed
   * @param selection the currently selected nodes
   */
  default void onSelectionChanged(CanvasArea area, Set<CanvasNode> selection) {}

  /**
   * Called after persistent state of a node changed, for example after it was moved.
   *
   * @param area the canvas the node belongs to
   * @param node the changed node
   */
  default void onNodeStateChanged(CanvasArea area, CanvasNode node) {}
}
