package rooms.systemRecovery.util.tracking;

import engine.Entity;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;

/**
 * Single integration boundary for System Recovery progress events.
 *
 * <p>Riddle controllers emit semantic events through this class instead of importing the engine
 * tracking API. This keeps tracking, puzzle identifiers and the System Recovery Petri-net adapter
 * in one place. The net receives only authoritative state events; it never validates source code
 * or performs world effects.
 */
public final class SystemRecoveryPuzzleEvents {
  private SystemRecoveryPuzzleEvents() {}

  /**
   * Creates the standard callbacks for one riddle interaction.
   *
   * <p>Every success, failure and completion event created here uses the same puzzle metadata. New
   * riddle controllers should request their callbacks here instead of calling tracking directly.
   */
  public static RiddleCallbacks forPuzzle(
      SystemRecoveryPuzzle puzzle, String objectId, String answerKind) {
    return new RiddleCallbacks(
        attempt -> attempt(puzzle, objectId, answerKind, attempt.input(), true, attempt.playerId()),
        attempt ->
            attempt(puzzle, objectId, answerKind, attempt.input(), false, attempt.playerId()),
        () -> solved(puzzle));
  }

  /** Creates callbacks using the default tracking metadata owned by the puzzle definition. */
  public static RiddleCallbacks forPuzzle(SystemRecoveryPuzzle puzzle) {
    return forPuzzle(puzzle, puzzle.defaultObjectId(), puzzle.defaultAnswerKind());
  }

  /** Records a server-evaluated interaction attempt. */
  public static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      int playerId) {
    SystemRecoveryTracking.attempt(puzzle, objectId, answerKind, rawAnswer, correct, playerId);
    if (correct) SystemRecoveryProgressNet.successfulInteraction(puzzle, answerKind);
  }

  /** Records a server-evaluated interaction attempt made by an entity. */
  public static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    SystemRecoveryTracking.attempt(puzzle, objectId, answerKind, rawAnswer, correct, player);
    if (correct) SystemRecoveryProgressNet.successfulInteraction(puzzle, answerKind);
  }

  /** Records the result of a terminal submission against the state that received it. */
  public static void terminalAttempt(int state, String source, boolean correct, int playerId) {
    SystemRecoveryTracking.terminalAttempt(state, source, correct, playerId);
    if (correct) SystemRecoveryProgressNet.terminalSuccess(state);
  }

  /** Records an authoritative puzzle completion exactly at the state transition boundary. */
  public static void solved(SystemRecoveryPuzzle puzzle) {
    SystemRecoveryTracking.solved(puzzle);
    SystemRecoveryProgressNet.solved(puzzle);
  }

  /** Records that a puzzle became available to the player flow. */
  public static void started(SystemRecoveryPuzzle puzzle) {
    SystemRecoveryTracking.started(puzzle);
  }

  /** Emits a non-terminal progress event, such as the asynchronous chip delivery. */
  public static void progress(SystemRecoveryPuzzle puzzle, String answerKind) {
    SystemRecoveryProgressNet.successfulInteraction(puzzle, answerKind);
  }

  /** Emits the authoritative completion of the opening telephone call. */
  public static void openingCallFinished() {
    SystemRecoveryProgressNet.openingCallFinished();
  }
}
