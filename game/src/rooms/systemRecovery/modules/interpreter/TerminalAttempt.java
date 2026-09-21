package rooms.systemRecovery.modules.interpreter;

/**
 * Immutable context passed to terminal success and failure callbacks.
 *
 * @param state interpreter state that evaluated the submission
 * @param source complete source submitted by the player
 * @param playerId authoritative player ID, or {@code -1} for a non-player analysis
 * @param dialogId open dialog receiving feedback, or {@code null} for non-UI analysis
 */
public record TerminalAttempt(int state, String source, int playerId, String dialogId) {

  /** Creates an attempt without an associated dialog. */
  public TerminalAttempt(int state, String source, int playerId) {
    this(state, source, playerId, null);
  }

  /** Normalizes missing source text so tracking and callbacks never receive {@code null}. */
  public TerminalAttempt {
    source = source == null ? "" : source;
  }
}
