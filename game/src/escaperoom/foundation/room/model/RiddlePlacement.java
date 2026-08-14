package escaperoom.foundation.room.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Explicit placement of one riddle interaction and its optional hint interaction.
 *
 * @param riddleId stable riddle identifier
 * @param components surface-bound source and input placements
 * @param hintPoint explicit hint point, absent when the riddle has no hints
 */
public record RiddlePlacement(
    String riddleId, List<ComponentPlacement> components, Optional<RoomPoint> hintPoint) {
  /** Creates one immutable explicit placement. */
  public RiddlePlacement {
    riddleId = RoomModelChecks.requireId(riddleId, "placement riddle id");
    components =
        RoomModelChecks.copyUnique(
            components, ComponentPlacement::componentId, "riddle component placements");
    if (components.isEmpty()) {
      throw new IllegalArgumentException("riddle placement requires at least one component");
    }
    hintPoint = Objects.requireNonNull(hintPoint, "hintPoint");
    boolean collides =
        hintPoint.isPresent()
            && components.stream()
                .map(ComponentPlacement::point)
                .toList()
                .contains(hintPoint.orElseThrow());
    if (collides) {
      throw new IllegalArgumentException("riddle component and hint points must not collide");
    }
  }
}
