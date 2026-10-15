package escaperoom.foundation.definition;

import feature.tasks.AssignmentTask;
import java.util.Objects;

/**
 * Host-evaluated input that sorts answer items into containers.
 *
 * @param id stable input identifier
 * @param surfaceId authored assignment surface identifier shared by its containers and items
 * @param task container, item, and target identifiers
 */
public record AssignmentInputDefinition(String id, String surfaceId, AssignmentTask task)
    implements SurfaceInputDefinition {
  /** Creates an immutable assignment input. */
  public AssignmentInputDefinition {
    id = DefinitionChecks.requireId(id, "assignment input id");
    surfaceId = DefinitionChecks.requireId(surfaceId, "assignment input surface id");
    Objects.requireNonNull(task, "task");
  }
}
