package escaperoom.foundation.definition;

import feature.tasks.ChoiceTask;
import java.util.Objects;

/**
 * Host-evaluated single- or multiple-choice question input.
 *
 * @param id stable input identifier
 * @param surfaceId authored terminal surface identifier
 * @param task question, options, and correct options; option values are the option identifiers
 */
public record ChoiceInputDefinition(String id, String surfaceId, ChoiceTask task)
    implements SurfaceInputDefinition {
  /** Creates an immutable choice input. */
  public ChoiceInputDefinition {
    id = DefinitionChecks.requireId(id, "choice input id");
    surfaceId = DefinitionChecks.requireId(surfaceId, "choice input surface id");
    Objects.requireNonNull(task, "task");
  }
}
