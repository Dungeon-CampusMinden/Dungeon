package escaperoom.foundation.multiplayer.game;

import engine.Entity;
import engine.tracking.Tracking;
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

  void hintUsed(final String riddleId, final ReleasedHint hint, final Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participantId -> Tracking.hintUsed(riddleId, hint.id(), participantId));
  }
}
