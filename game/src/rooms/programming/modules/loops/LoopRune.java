package rooms.programming.modules.loops;

import java.util.Objects;

/**
 * A collectible loop form belonging to one forge station.
 *
 * @param id the canonical rune ID
 * @param challengeId the station this rune belongs to
 * @param type the loop form to execute
 */
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
