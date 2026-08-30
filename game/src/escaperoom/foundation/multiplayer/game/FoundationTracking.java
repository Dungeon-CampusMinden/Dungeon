package escaperoom.foundation.multiplayer.game;

import engine.Entity;
import engine.tracking.Tracking;
import escaperoom.foundation.runtime.CodeAttemptResult;
import escaperoom.foundation.runtime.CodeOutcome;
import escaperoom.foundation.runtime.Projection;
import escaperoom.foundation.runtime.ReleasedHint;

/** Small server-authoritative tracking adapter for every Foundation room. */
final class FoundationTracking {
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
      final String rawAnswer,
      final CodeAttemptResult result,
      final Entity player) {
    if (result.outcome() == CodeOutcome.NOT_EVALUATED) {
      return;
    }
    Tracking.puzzleStarted(riddleId);
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    riddleId,
                    inputId,
                    "numeric-code",
                    rawAnswer,
                    result.outcome() == CodeOutcome.CORRECT,
                    participantId));
  }

  void hintUsed(final String riddleId, final ReleasedHint hint, final Entity player) {
    Tracking.puzzleStarted(riddleId);
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(riddleId, hint.id(), participantId));
  }
}
