package escaperoom.foundation.room.model;

import java.util.Objects;

/**
 * Door that opens once its riddle leaves the locked state.
 *
 * @param riddleId stable riddle identifier whose activation opens the door
 * @param point door coordinate
 */
public record RiddleDoor(String riddleId, RoomPoint point) {
  /** Creates one riddle door. */
  public RiddleDoor {
    riddleId = RoomModelChecks.requireId(riddleId, "riddle door id");
    Objects.requireNonNull(point, "point");
  }
}
