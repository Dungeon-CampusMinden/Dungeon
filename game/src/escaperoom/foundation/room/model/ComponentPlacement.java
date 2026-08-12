package escaperoom.foundation.room.model;

import java.util.Objects;

/**
 * Explicit placement of one surface-bound riddle component.
 *
 * @param componentId stable source or input identifier
 * @param surfaceId authored surface identifier
 * @param point deterministic room point
 */
public record ComponentPlacement(String componentId, String surfaceId, RoomPoint point) {
  /** Creates an immutable component placement. */
  public ComponentPlacement {
    componentId = RoomModelChecks.requireId(componentId, "component id");
    surfaceId = RoomModelChecks.requireId(surfaceId, "component surface id");
    Objects.requireNonNull(point, "point");
  }
}
