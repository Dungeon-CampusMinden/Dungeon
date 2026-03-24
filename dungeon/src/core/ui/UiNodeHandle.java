package core.ui;

import java.util.Optional;

/**
 * Engine-agnostic handle for a UI node/dialog.
 */
public interface UiNodeHandle {

  /**
   * Removes the wrapped UI node from its parent/stage if applicable.
   */
  void remove();

  /**
   * Returns the z-index used for ordering topmost dialogs.
   */
  int getZIndex();

  /**
   * Returns whether the wrapped dialog is currently visible.
   */
  boolean isVisible();

  /**
   * Updates the visibility of the wrapped dialog.
   *
   * @param visible true if the dialog should be visible
   */
  void setVisible(boolean visible);

  /**
   * Returns whether the wrapped dialog is currently attached to a stage/scene.
   *
   * <p>This intentionally hides the concrete backend stage type.
   */
  boolean isAttached();

  /**
   * Attaches this dialog to the given stage.
   *
   * @param stageHandle backend-agnostic stage handle
   */
  void attachTo(StageHandle stageHandle);

  /**
   * Moves this dialog to the front if supported by the backend.
   */
  void toFront();

  /**
   * Centers this dialog on the given stage.
   *
   * @param stageHandle backend-agnostic stage handle
   */
  void centerOn(StageHandle stageHandle);

  /**
   * Tries to unwrap the backend-specific UI object.
   *
   * @param type expected backend type
   * @param <T> backend type
   * @return optional containing the backend object if compatible
   */
  <T> Optional<T> unwrap(Class<T> type);
}
