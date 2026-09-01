package rooms.lasthour.util;

import engine.Entity;
import engine.tracking.Tracking;

/** Server-side tracking hooks for the concrete puzzles in The Last Hour. */
public final class LastHourTracking {
  private LastHourTracking() {}

  /**
   * Records a puzzle start once per running room session.
   *
   * @param puzzle room-local puzzle
   */
  public static void started(LastHourPuzzle puzzle) {
    Tracking.puzzleStarted(puzzle.id());
  }

  /**
   * Records a puzzle solution once per running room session.
   *
   * @param puzzle room-local puzzle
   */
  public static void solved(LastHourPuzzle puzzle) {
    started(puzzle);
    Tracking.puzzleSolved(puzzle.id());
  }

  /**
   * Records one server-evaluated answer for the acting player.
   *
   * @param puzzle room-local puzzle
   * @param objectId stable identifier of the answered object
   * @param answerKind shape of the raw answer
   * @param rawAnswer complete answer submitted by the player
   * @param correct server-evaluated correctness
   * @param player server-side player entity
   */
  public static void attempt(
      LastHourPuzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    started(puzzle);
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    puzzle.id(), objectId, answerKind, rawAnswer, correct, participantId));
  }

  /**
   * Records one meaningful use of a concrete-room hint for the acting player.
   *
   * @param puzzle room-local puzzle
   * @param hintId stable room-local hint identifier
   * @param player server-side player entity
   */
  public static void hintUsed(LastHourPuzzle puzzle, String hintId, Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(puzzle.id(), hintId, participantId));
  }
}
