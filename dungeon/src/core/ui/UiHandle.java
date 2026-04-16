package core.ui;

import java.util.Optional;

/**
 * Interface for managing and interacting with a UI element handle.
 *
 * <p>The {@code UiHandle} interface defines a contract for controlling the lifecycle,
 * visibility, attachment, and positioning of a user interface element within a stage.
 *
 * <p>It provides abstraction over specific UI framework implementations while enabling
 * type-safe access to the underlying framework-specific objects.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Managing the visibility state of the UI element.</li>
 *   <li>Attaching the UI element to a stage.</li>
 *   <li>Controlling z-order priority and bringing the element to the front.</li>
 *   <li>Positioning the UI element relative to a stage.</li>
 *   <li>Providing type-safe unwrapping to access framework-specific implementations.</li>
 * </ul>
 */
public interface UiHandle {

  /**
   * Removes this UI element from the stage.
   */
  void remove();

  /**
   * Returns the z-order index of this UI element.
   *
   * @return the z-index value
   */
  int getZIndex();

  /**
   * Checks if this UI element is currently visible.
   *
   * @return true if visible, false otherwise
   */
  boolean isVisible();

  /**
   * Sets the visibility of this UI element.
   *
   * @param visible true to show, false to hide
   */
  void setVisible(boolean visible);

  /**
   * Checks if this UI element is attached to a stage.
   *
   * @return true if attached, false otherwise
   */
  boolean isAttached();

  /**
   * Attaches this UI element to the specified stage.
   *
   * @param stageHandle the stage to attach to
   */
  void attachTo(StageHandle stageHandle);

  /**
   * Brings this UI element to the front, increasing its z-order priority.
   */
  void toFront();

  /**
   * Centers this UI element on the specified stage.
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
