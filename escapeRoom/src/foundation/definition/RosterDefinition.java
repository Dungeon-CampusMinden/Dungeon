package foundation.definition;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Ordered player capacity used by one Foundation session.
 *
 * @param slots one to four stable slots in strictly ascending one-based slot order
 */
public record RosterDefinition(List<RosterSlotDefinition> slots) {
  /** Creates an ordered player-capacity definition. */
  public RosterDefinition {
    slots = List.copyOf(Objects.requireNonNull(slots, "slots"));
    if (slots.isEmpty() || slots.size() > 4) {
      throw new IllegalArgumentException("roster must contain 1..4 slots");
    }
    if (new HashSet<>(slots.stream().map(RosterSlotDefinition::id).toList()).size()
        != slots.size()) {
      throw new IllegalArgumentException("roster slot identifiers must be unique");
    }
    int previousNumber = 0;
    for (RosterSlotDefinition slot : slots) {
      if (slot.number() <= previousNumber) {
        throw new IllegalArgumentException("roster slots must be ordered by unique slot number");
      }
      previousNumber = slot.number();
    }
  }
}
