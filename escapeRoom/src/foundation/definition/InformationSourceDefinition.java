package foundation.definition;

import java.util.List;
import java.util.Objects;

/**
 * Readable source content which is independent of riddle activation.
 *
 * @param id stable source identifier
 * @param surfaceId authored container surface identifier
 * @param resourceIds ordered resource identifiers
 */
public record InformationSourceDefinition(String id, String surfaceId, List<String> resourceIds) {
  /** Creates an immutable nonempty information source. */
  public InformationSourceDefinition {
    id = DefinitionChecks.requireId(id, "information source id");
    surfaceId = DefinitionChecks.requireId(surfaceId, "information source surface id");
    resourceIds = List.copyOf(Objects.requireNonNull(resourceIds, "resourceIds"));
    if (resourceIds.isEmpty()) {
      throw new IllegalArgumentException("information source requires resources");
    }
    resourceIds =
        resourceIds.stream()
            .map(resource -> DefinitionChecks.requireId(resource, "resource id"))
            .toList();
  }
}
