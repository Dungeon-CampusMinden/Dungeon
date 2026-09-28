package rooms.systemRecovery.modules.interpreter;

/**
 * Immutable context passed to terminal success and failure callbacks.
 *
 * @param state interpreter state that evaluated the submission
 * @param source complete source submitted by the player
 * @param playerId authoritative player ID, or {@code -1} for a non-player analysis
 * @param dialogId open dialog receiving feedback, or {@code null} for non-UI analysis
 * @param combinedSubmission whether this source completes multiple consecutive steps
 * @param acceptedSource complete accepted source across partial submissions, for solution records
 */
public record TerminalAttempt(
    int state,
    String source,
    int playerId,
    String dialogId,
    boolean combinedSubmission,
    String acceptedSource) {

  /** Creates an attempt without an associated dialog or combined-step marker. */
  public TerminalAttempt(int state, String source, int playerId, String dialogId) {
    this(state, source, playerId, dialogId, false, source);
  }

  /** Creates an attempt with a combined-step marker but no accepted source override. */
  public TerminalAttempt(
      int state, String source, int playerId, String dialogId, boolean combinedSubmission) {
    this(state, source, playerId, dialogId, combinedSubmission, source);
  }

  /** Creates an attempt without an associated dialog. */
  public TerminalAttempt(int state, String source, int playerId) {
    this(state, source, playerId, null, false, source);
  }

  /** Normalizes missing source text so tracking and callbacks never receive {@code null}. */
  public TerminalAttempt {
    source = source == null ? "" : source;
    acceptedSource = acceptedSource == null ? source : acceptedSource;
  }
}
