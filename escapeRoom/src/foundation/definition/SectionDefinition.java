package foundation.definition;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * One mandatory ordered section whose riddles progress in parallel with AND completion.
 *
 * @param id stable section identifier
 * @param riddles nonempty required riddles in deterministic order
 */
public record SectionDefinition(String id, List<RiddleDefinition> riddles) {
  /** Creates a mandatory section definition. */
  public SectionDefinition {
    id = DefinitionChecks.requireId(id, "section id");
    riddles = List.copyOf(Objects.requireNonNull(riddles, "riddles"));
    if (riddles.isEmpty()) {
      throw new IllegalArgumentException("section must contain at least one riddle");
    }
    if (new HashSet<>(riddles.stream().map(RiddleDefinition::id).toList()).size()
        != riddles.size()) {
      throw new IllegalArgumentException("section riddle identifiers must be unique");
    }
  }
}
