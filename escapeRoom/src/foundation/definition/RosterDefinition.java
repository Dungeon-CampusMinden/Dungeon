package foundation.definition;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Ordered player capacity used by one Foundation session.
 *
 * @param slots one to four stable slots numbered consecutively from one in list order
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
    for (int index = 0; index < slots.size(); index++) {
      if (slots.get(index).number() != index + 1) {
        throw new IllegalArgumentException(
            "roster slots must be consecutively numbered from 1 in list order");
      }
    }
  }
}
