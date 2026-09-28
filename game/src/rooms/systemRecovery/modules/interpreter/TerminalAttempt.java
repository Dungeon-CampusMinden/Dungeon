package rooms.systemRecovery.modules.interpreter;

/**
 * Immutable context passed to terminal success and failure callbacks.
 *
 * @param state interpreter state that evaluated the submission
 * @param source complete source submitted by the player
 * @param playerId authoritative player ID, or {@code -1} for a non-player analysis
 * @param dialogId open dialog receiving feedback, or {@code null} for non-UI analysis
 * @param combinedSubmission whether this source completes multiple consecutive steps
 */
public record TerminalAttempt(
    int state, String source, int playerId, String dialogId, boolean combinedSubmission) {

  /** Creates an attempt without an associated dialog or combined-step marker. */
  public TerminalAttempt(int state, String source, int playerId, String dialogId) {
    this(state, source, playerId, dialogId, false);
  }

  /** Creates an attempt without an associated dialog. */
  public TerminalAttempt(int state, String source, int playerId) {
    this(state, source, playerId, null, false);
  }

  /** Normalizes missing source text so tracking and callbacks never receive {@code null}. */
  public TerminalAttempt {
    source = source == null ? "" : source;
  }
}
