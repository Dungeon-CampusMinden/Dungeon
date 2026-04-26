package core.ui;

import java.util.Optional;

/**
 * Handle interface for accessing and managing a UI stage.
 *
 * <p>StageHandle provides an abstraction layer for interacting with the underlying UI stage,
 * regardless of the specific UI framework or implementation being used. It allows for
 * framework-agnostic UI operations such as adding actors, managing focus, and querying stage
 * properties.
 *
 * <p>Key capabilities:
 *
 * <ul>
 *   <li>Accessing the raw underlying stage object
 *   <li>Type-safe unwrapping of framework-specific stage implementations
 *   <li>Querying stage dimensions and properties
 *   <li>Adding and managing actors on the stage
 *   <li>Managing keyboard focus
 *   <li>Querying mouse input state
 * </ul>
 */
public interface StageHandle {

  /**
   * Returns the raw underlying stage object.
   *
   * <p>This provides access to the framework-specific stage implementation for cases where direct
   * access is necessary.
   *
   * @return the underlying stage object, framework-specific type
   */
  Object raw();

  /**
   * Unwraps the stage handle to a specific type in a type-safe manner.
   *
   * <p>This method attempts to cast the underlying stage to the specified type and returns an
   * Optional containing it if successful, or an empty Optional if the underlying object is not an
   * instance of the requested type.
   *
   * @param <T> the target type
   * @param type the target class to unwrap to
   * @return an Optional containing the stage cast to type T, or empty if casting is not possible
   */
  <T> Optional<T> unwrap(Class<T> type);

  /**
   * Gets the width of the stage.
   *
   * @return the stage width in pixels (or framework units)
   */
  float getWidth();

  /**
   * Gets the height of the stage.
   *
   * @return the stage height in pixels (or framework units)
   */
  float getHeight();

  /**
   * Adds an actor to the stage.
   *
   * <p>The actor will be rendered and receive input events according to the stage's event handling
   * and rendering order.
   *
   * @param actor the actor to add (framework-specific actor type)
   */
  void addActor(Object actor);

  /**
   * Sets keyboard focus to the specified actor.
   *
   * <p>The actor will receive keyboard input events while it has focus. Only one actor can have
   * keyboard focus at a time.
   *
   * @param actor the actor to receive keyboard focus
   */
  void setKeyboardFocus(Object actor);

  /**
   * Gets the current mouse x-coordinate.
   *
   * @return the x-coordinate of the mouse cursor in stage coordinates
   */
  int mouseX();

  /**
   * Gets the current mouse y-coordinate.
   *
   * @return the y-coordinate of the mouse cursor in stage coordinates
   */
  int mouseY();
}
