package rooms.systemRecovery.util.tracking;

import engine.Entity;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/**
 * Server-side boundary for System Recovery attempt tracking and explicit learning-step callbacks.
 *
 * <p>Tracking an attempt or marking a riddle solved does not implicitly change Petri progress.
 * Progress moves only at a concrete success callback that names its expected learning step.
 */
public final class SystemRecoveryPuzzleEvents {

  private SystemRecoveryPuzzleEvents() {}

  /**
   * Creates callbacks for interactions that only need attempt and completion tracking.
   *
   * @param puzzle riddle that owns the interaction
   * @param objectId stable object identifier
   * @param answerKind stable answer identifier
   * @return callbacks that never advance the progress net implicitly
   */
  public static RiddleCallbacks forPuzzle(
      SystemRecoveryPuzzle puzzle, String objectId, String answerKind) {
    return callbacks(puzzle, objectId, answerKind, null);
  }

  /**
   * Creates callbacks whose first successful riddle completion owns one learning step.
   *
   * @param puzzle riddle that owns the interaction
   * @param objectId stable object identifier
   * @param answerKind stable answer identifier
   * @param completedStep step completed by the riddle's solved callback
   * @return callbacks with an explicit, typed completion mapping
   */
  public static RiddleCallbacks forPuzzleCompleting(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      SystemRecoveryLearningStep completedStep) {
    return callbacks(puzzle, objectId, answerKind, completedStep);
  }

  /**
   * Creates callbacks using the puzzle's default tracking metadata without Petri progression.
   *
   * @param puzzle riddle that owns the interaction
   * @return callbacks using the puzzle's default object and answer identifiers
   */
  public static RiddleCallbacks forPuzzle(SystemRecoveryPuzzle puzzle) {
    return forPuzzle(puzzle, puzzle.defaultObjectId(), puzzle.defaultAnswerKind());
  }

  private static RiddleCallbacks callbacks(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      SystemRecoveryLearningStep completedStep) {
    return new RiddleCallbacks(
        attempt ->
            SystemRecoveryTracking.attempt(
                puzzle, objectId, answerKind, attempt.input(), true, attempt.playerId()),
        attempt ->
            SystemRecoveryTracking.attempt(
                puzzle, objectId, answerKind, attempt.input(), false, attempt.playerId()),
        () -> {
          solved(puzzle);
          if (completedStep != null) SystemRecoveryProgressNet.complete(completedStep);
        });
  }

  /**
   * Records an authoritative interaction attempt without changing progression.
   *
   * @param puzzle owning riddle
   * @param objectId stable object identifier
   * @param answerKind stable answer identifier
   * @param rawAnswer submitted value
   * @param correct authoritative validator result
   * @param playerId authoritative player ID, or negative if unavailable
   */
  public static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      int playerId) {
    SystemRecoveryAchievements.physicalAttempt(puzzle, rawAnswer, correct, playerId);
    SystemRecoveryTracking.attempt(puzzle, objectId, answerKind, rawAnswer, correct, playerId);
  }

  /**
   * Records an authoritative interaction attempt made by a player entity.
   *
   * @param puzzle riddle that owns the interaction
   * @param objectId stable object identifier
   * @param answerKind stable answer identifier
   * @param rawAnswer submitted value
   * @param correct authoritative validator result
   * @param player player entity that submitted the value
   */
  public static void attempt(
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    SystemRecoveryTracking.attempt(puzzle, objectId, answerKind, rawAnswer, correct, player);
  }

  /**
   * Records a terminal attempt and completes its one explicitly mapped learning step on success.
   *
   * @param state interpreter state that received the input
   * @param source full submitted source
   * @param correct whether the authoritative interpreter accepted it
   * @param playerId authoritative player ID, or negative if unavailable
   */
  public static void terminalAttempt(int state, String source, boolean correct, int playerId) {
    SystemRecoveryAchievements.terminalAttempt(
        new TerminalAttempt(state, source, playerId, null),
        correct);
    SystemRecoveryTracking.terminalAttempt(state, source, correct, playerId);
    if (!correct) return;
    TerminalStep.fromStateId(state)
        .flatMap(SystemRecoveryLearningStep::fromTerminalStep)
        .ifPresent(SystemRecoveryProgressNet::complete);
  }

  /**
   * Records a puzzle completion without inferring any progress transition.
   *
   * @param puzzle completed puzzle
   */
  public static void solved(SystemRecoveryPuzzle puzzle) {
    SystemRecoveryAchievements.puzzleSolved(puzzle);
    SystemRecoveryTracking.solved(puzzle);
  }

  /**
   * Records that a puzzle became available in the shared room progression.
   *
   * @param puzzle puzzle that became available
   */
  public static void started(SystemRecoveryPuzzle puzzle) {
    SystemRecoveryTracking.started(puzzle);
  }

  /**
   * Records one accepted telephone hint for its requesting participant.
   *
   * @param puzzle puzzle for which the hint was accepted
   * @param hintId stable identifier of the accepted hint stage
   * @param player participant who requested the hint
   */
  public static void hintUsed(SystemRecoveryPuzzle puzzle, String hintId, Entity player) {
    if (puzzle == null || hintId == null || player == null) return;
    SystemRecoveryAchievements.hintUsed(puzzle, hintId);
    SystemRecoveryTracking.hintUsed(puzzle, hintId, player);
  }
}
