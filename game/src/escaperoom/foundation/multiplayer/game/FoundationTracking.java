package escaperoom.foundation.multiplayer.game;

import engine.Entity;
import engine.tracking.Tracking;
import escaperoom.foundation.runtime.CodeAttemptResult;
import escaperoom.foundation.runtime.CodeOutcome;
import escaperoom.foundation.runtime.Projection;
import escaperoom.foundation.runtime.ReleasedHint;
import tools.jackson.databind.json.JsonMapper;

/** Small server-authoritative tracking adapter for every Foundation room. */
final class FoundationTracking {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  /**
   * Encodes a structured answer for {@link #attempt}, e.g. a sorted choice selection as a JSON
   * array or a sorted item placement as a JSON object.
   *
   * @param answer answer to encode
   * @return compact JSON
   */
  static String json(final Object answer) {
    return JSON.writeValueAsString(answer);
  }

  synchronized void observe(final Projection projection) {
    projection
        .riddles()
        .forEach(
            riddle -> {
              if (riddle.status() != Projection.ProgressStatus.LOCKED) {
                Tracking.puzzleStarted(riddle.id());
              }
              if (riddle.status() == Projection.ProgressStatus.SOLVED) {
                Tracking.puzzleSolved(riddle.id());
              }
            });
  }

  synchronized void attempt(
      final String riddleId,
      final String inputId,
      final String answerKind,
      final String rawAnswer,
      final CodeAttemptResult result,
      final Entity player) {
    if (result.outcome() == CodeOutcome.NOT_EVALUATED) {
      return;
    }
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    riddleId,
                    inputId,
                    answerKind,
                    rawAnswer,
                    result.outcome() == CodeOutcome.CORRECT,
                    participantId));
  }

  void hintUsed(final String riddleId, final ReleasedHint hint, final Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(riddleId, hint.id(), participantId));
  }
}
