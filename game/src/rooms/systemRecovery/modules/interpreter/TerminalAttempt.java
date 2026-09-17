package rooms.systemRecovery.modules.interpreter;

/**
 * Immutable context passed to terminal success and failure callbacks.
 *
 * @param state interpreter state that evaluated the submission
 * @param source complete source submitted by the player
 * @param playerId authoritative player ID, or {@code -1} for a non-player analysis
 */
public record TerminalAttempt(int state, String source, int playerId) {

  /** Normalizes missing source text so tracking and callbacks never receive {@code null}. */
  public TerminalAttempt {
    source = source == null ? "" : source;
  }
}
