package escaperoom.foundation.definition;

import java.util.Objects;
import java.util.Set;

/**
 * Host-evaluated single- or multiple-choice question input.
 *
 * <p>An attempt is correct when it selects exactly the correct option identifiers.
 *
 * @param id stable input identifier
 * @param surfaceId authored terminal surface identifier
 * @param correctOptionIds identifiers of all correct options
 */
public record ChoiceInputDefinition(String id, String surfaceId, Set<String> correctOptionIds)
    implements SurfaceInputDefinition {
  /** Creates an immutable choice input. */
  public ChoiceInputDefinition {
    id = DefinitionChecks.requireId(id, "choice input id");
    surfaceId = DefinitionChecks.requireId(surfaceId, "choice input surface id");
    correctOptionIds = Set.copyOf(Objects.requireNonNull(correctOptionIds, "correctOptionIds"));
    if (correctOptionIds.isEmpty()) {
      throw new IllegalArgumentException("choice input requires at least one correct option");
    }
    correctOptionIds.forEach(optionId -> DefinitionChecks.requireId(optionId, "choice option id"));
  }
}
