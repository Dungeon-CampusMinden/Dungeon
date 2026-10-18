package feature.tasks;

import engine.Entity;
import engine.tracking.Tracking;
import java.util.Objects;

/**
 * Tracking identity of a task in a room: the puzzle it belongs to and the object players answer at.
 *
 * <p>Set it on a {@link TaskComponent} or a keypad's {@code KeypadComponent} to record every
 * submitted answer, or call {@link #attempt} where a room evaluates answers itself. The task
 * defines the answer kind and encoding. Puzzle start and completion stay with the room, because one
 * puzzle may consist of several tasks.
 *
 * @param puzzleId stable room-local puzzle identifier
 * @param objectId stable identifier of the answered object
 */
public record TaskTracking(String puzzleId, String objectId) {

  /** Creates a tracking identity. */
  public TaskTracking {
    Objects.requireNonNull(puzzleId, "puzzleId");
    Objects.requireNonNull(objectId, "objectId");
  }

  /**
   * Records one evaluated answer for the answering player, if tracking is active.
   *
   * @param task the answered task
   * @param answer the complete answer
   * @param correct whether the answer was correct
   * @param player the answering player
   * @param <T> the answer type
   */
  public <T> void attempt(
      final Task<T> task, final T answer, final boolean correct, final Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participantId ->
                Tracking.attempt(
                    puzzleId,
                    objectId,
                    task.answerKind(),
                    task.trackedAnswer(answer),
                    correct,
                    participantId));
  }
}
