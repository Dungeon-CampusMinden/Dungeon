package core.ui;

import java.util.Optional;

/**
 * Handle interface for managing UI node lifecycle, visibility, and positioning.
 *
 * <p>UiNodeHandle provides a unified interface for controlling UI nodes (visual elements)
 * within the UI system. It abstracts away the specific UI framework implementation and provides
 * common operations for managing node state and presentation.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Lifecycle management (attachment, removal)
 *   <li>Visibility control
 *   <li>Z-ordering (layering)
 *   <li>Positioning and centering
 *   <li>Type-safe access to underlying framework objects
 * </ul>
 *
 * <p>Different implementations may have varying behavior for certain operations (e.g., headless
 * implementations may use no-ops).
 */
public interface UiNodeHandle {

  /**
   * Removes this UI node from the stage.
   *
   * <p>After removal, the node should no longer be rendered or receive input events.
   * The node's visibility may be set to false as part of the removal process.
   */
  void remove();

  /**
   * Gets the z-index (layer depth) of this UI node.
   *
   * <p>Higher z-index values indicate nodes rendered on top. The exact range and meaning
   * of z-index values is implementation-dependent.
   *
   * @return the z-index of this node
   */
  int getZIndex();

  /**
   * Checks whether this UI node is currently visible.
   *
   * @return true if the node is visible, false if hidden
   */
  boolean isVisible();

  /**
   * Sets the visibility of this UI node.
   *
   * <p>Invisible nodes are not rendered but typically remain in the stage hierarchy.
   *
   * @param visible true to show the node, false to hide it
   */
  void setVisible(boolean visible);

  /**
   * Checks whether this UI node is currently attached to a stage.
   *
   * @return true if the node is attached to a stage, false otherwise
   */
  boolean isAttached();

  /**
   * Attaches this UI node to the specified stage.
   *
   * <p>After attachment, the node will be rendered and receive input events according
   * to the stage's event handling.
   *
   * @param stageHandle the stage to attach to
   */
  void attachTo(StageHandle stageHandle);

  /**
   * Moves this UI node to the front of the z-order.
   *
   * <p>After this operation, the node will be rendered on top of all other nodes
   * in the stage.
   */
  void toFront();

  /**
   * Centers this UI node on the specified stage.
   *
   * <p>Positions the node such that it is centered both horizontally and vertically
   * within the stage. The node's dimensions are considered in the centering calculation.
   *
   * @param stageHandle the stage to center on
   */
  void centerOn(StageHandle stageHandle);

  /**
   * Unwraps this handle to a specific framework type in a type-safe manner.
   *
   * <p>This method allows access to the underlying framework-specific implementation
   * when needed.
   *
   * @param <T> the target type
   * @param type the target class to unwrap to
   * @return an Optional containing the unwrapped object if it matches the type, or empty if not
   */
  <T> Optional<T> unwrap(Class<T> type);
}
