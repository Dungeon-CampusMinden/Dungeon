package rooms.lasthour.util;

import engine.Entity;
import engine.tracking.Tracking;

/** Server-side tracking hooks for the concrete puzzles in The Last Hour. */
public final class LastHourTracking {
  private LastHourTracking() {}

  /**
   * Records a puzzle start once per running room session.
   *
   * @param puzzleId stable room-local puzzle identifier
   */
  public static void started(String puzzleId) {
    Tracking.puzzleStarted(puzzleId);
  }

  /**
   * Records a puzzle solution once per running room session.
   *
   * @param puzzleId stable room-local puzzle identifier
   */
  public static void solved(String puzzleId) {
    started(puzzleId);
    Tracking.puzzleSolved(puzzleId);
  }

  /**
   * Records one server-evaluated answer for the acting player.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable identifier of the answered object
   * @param answerKind shape of the raw answer
   * @param rawAnswer complete answer submitted by the player
   * @param correct server-evaluated correctness
   * @param player server-side player entity
   */
  public static void attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    started(puzzleId);
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    puzzleId, objectId, answerKind, rawAnswer, correct, participantId));
  }

  /**
   * Records one meaningful use of a concrete-room hint for the acting player.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param hintId stable room-local hint identifier
   * @param player server-side player entity
   */
  public static void hintUsed(String puzzleId, String hintId, Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(puzzleId, hintId, participantId));
  }
}
