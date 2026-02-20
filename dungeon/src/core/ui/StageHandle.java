package core.ui;

import java.util.Optional;

/**
 * Backend-agnostic handle to a UI stage.
 *
 * <p>This intentionally hides the concrete UI framework type (e.g. libGDX Stage).
 */
public interface StageHandle {

  /** Raw backend object (use sparingly; prefer unwrap). */
  Object raw();

  /** Try to unwrap the underlying backend object to a requested type. */
  <T> Optional<T> unwrap(Class<T> type);

  /** UI size access without leaking backend stage type. */
  float getWidth();

  float getHeight();

  /**
   * Add a UI actor/widget to the stage.
   *
   * <p>Uses Object to avoid leaking libGDX Actor into core API.
   */
  void addActor(Object actor);

  /**
   * Set keyboard focus to a UI actor/widget.
   *
   * <p>Uses Object to avoid leaking libGDX Actor into core API.
   */
  void setKeyboardFocus(Object actor);
}
