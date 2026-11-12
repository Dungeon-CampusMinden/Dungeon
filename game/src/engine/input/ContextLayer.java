package engine.input;

/**
 * Fixed vertical order of the input contexts, from bottom to top.
 *
 * <p>Inside a layer, later contexts lie above earlier ones, which matches the visible order of UIs.
 */
public enum ContextLayer {
  /** Gameplay or main menu. */
  BASE,
  /** Contexts brought along by rooms. */
  ROOM,
  /** The client's mirror of a server-side control lock. */
  SERVER_LOCK,
  /** Toggling the level editor. */
  EDITOR_TOGGLE,
  /** The active level editor. */
  EDITOR,
  /** Popups of the level editor. */
  EDITOR_POPUP,
  /** Dialogs, inventory and pause menu. */
  UI,
  /** Host controls of the audience mode. */
  AUDIENCE,
  /** Debug shortcuts. */
  DEBUG,
  /** A focused text field. */
  TEXT,
  /** Actions that work everywhere. */
  SYSTEM
}
