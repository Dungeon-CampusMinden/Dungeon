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

  /**
   * Records that a puzzle became available.
   *
   * @param puzzle puzzle whose place became active
   */
  static void started(SystemRecoveryPuzzle puzzle) {
    Tracking.puzzleStarted(puzzle.id());
  }

  /**
   * Records a completed puzzle once the authoritative room state has changed.
   *
   * @param puzzle puzzle that has completed
   */
  static void solved(SystemRecoveryPuzzle puzzle) {
    Tracking.puzzleSolved(puzzle.id());
  }

  /**
   * Records a server-evaluated attempt made by a concrete player entity.
   *
   * @param puzzle riddle that owns the interaction
   * @param objectId stable identifier of the interacted object
   * @param answerKind stable semantic label for the answer
   * @param rawAnswer submitted answer payload
   * @param correct whether the answer was accepted
   * @param player player entity that performed the interaction
   */
  static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    if (player == null) {
      return;
    }
    attempt(puzzle, objectId, answerKind, rawAnswer, correct, player.id());
  }

  /**
   * Records a server-evaluated attempt made by a player ID, when one is available.
   *
   * @param puzzle riddle that owns the interaction
   * @param objectId stable identifier of the interacted object
   * @param answerKind stable semantic label for the answer
   * @param rawAnswer submitted answer payload
   * @param correct whether the answer was accepted
   * @param playerId authoritative player ID, or a negative value when unavailable
   */
  static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      int playerId) {
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

  /**
   * Records a terminal answer using the state that was active when it was submitted.
   *
   * @param state terminal state that received the source
   * @param source complete submitted source
   * @param correct whether the source was accepted
   * @param playerId authoritative player ID, or a negative value when unavailable
   */
  static void terminalAttempt(int state, String source, boolean correct, int playerId) {
    SystemRecoveryPuzzle puzzle = SystemRecoveryPuzzle.fromTerminalState(state);
    attempt(puzzle, "terminal-state-" + state, "terminal-source", source, correct, playerId);
  }

  /**
   * Records one accepted use of a shared telephone hint.
   *
   * @param puzzle puzzle whose hint was requested
   * @param hintId stable hint identifier
   * @param player player who accepted the hint
   */
  static void hintUsed(SystemRecoveryPuzzle puzzle, String hintId, Entity player) {
    if (player == null) return;
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(puzzle.id(), hintId, participantId));
  }
}
