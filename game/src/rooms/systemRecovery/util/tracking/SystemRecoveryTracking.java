package rooms.systemRecovery.util.tracking;

import engine.Entity;
import engine.tracking.Tracking;

/**
 * Low-level server-side adapter for the engine tracking API.
 *
 * <p>Gameplay code should use {@link SystemRecoveryPuzzleEvents}; this class deliberately contains
 * no puzzle wiring and exists only to isolate the engine tracking dependency.
 */
final class SystemRecoveryTracking {
  private SystemRecoveryTracking() {}

  /** Records that a puzzle became available. */
  static void started(SystemRecoveryPuzzle puzzle) {
    Tracking.puzzleStarted(puzzle.id());
  }

  /** Records a completed puzzle once the authoritative room state has changed. */
  static void solved(SystemRecoveryPuzzle puzzle) {
    started(puzzle);
    Tracking.puzzleSolved(puzzle.id());
  }

  /** Records a server-evaluated attempt made by a concrete player entity. */
  static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    if (player == null) {
      started(puzzle);
      return;
    }
    attempt(puzzle, objectId, answerKind, rawAnswer, correct, player.id());
  }

  /** Records a server-evaluated attempt made by a player ID, when one is available. */
  static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      int playerId) {
    started(puzzle);
    if (playerId < 0) return;
    Tracking.participantForEntity(playerId)
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    puzzle.id(),
                    objectId,
                    answerKind,
                    rawAnswer == null ? "" : rawAnswer,
                    correct,
                    participantId));
  }

  /** Records a terminal answer using the state that was active when it was submitted. */
  static void terminalAttempt(int state, String source, boolean correct, int playerId) {
    SystemRecoveryPuzzle puzzle = SystemRecoveryPuzzle.fromTerminalState(state);
    attempt(puzzle, "terminal-state-" + state, "terminal-source", source, correct, playerId);
  }
}
