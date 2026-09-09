package rooms.programming.modules.loops;

import java.util.Objects;

/** A collectible loop form belonging to one forge station. */
public record LoopRune(String id, String challengeId, LoopType type) {

  /** Creates a rune with its stable content identifiers. */
  public LoopRune {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(challengeId, "challengeId");
    Objects.requireNonNull(type, "type");
    if (id.isBlank() || challengeId.isBlank()) {
      throw new IllegalArgumentException("rune identifiers must not be blank");
    }
  }
}
