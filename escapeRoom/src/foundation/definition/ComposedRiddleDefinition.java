package foundation.definition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One riddle completed atomically when all of its inputs are satisfied.
 *
 * @param id stable riddle identifier
 * @param informationSources readable source definitions
 * @param inputs mandatory AND-composed inputs
 * @param hints ordered optional hints
 */
public record ComposedRiddleDefinition(
    String id,
    List<InformationSourceDefinition> informationSources,
    List<InputDefinition> inputs,
    List<HintDefinition> hints) {
  /** Creates an immutable composed riddle. */
  public ComposedRiddleDefinition {
    id = DefinitionChecks.requireId(id, "riddle id");
    informationSources =
        List.copyOf(Objects.requireNonNull(informationSources, "informationSources"));
    inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
    if (inputs.isEmpty()) {
      throw new IllegalArgumentException("riddle requires at least one input");
    }
    hints = DefinitionChecks.copyOrderedHints(hints, "riddle hints");
    Map<String, String> ids = new LinkedHashMap<>();
    informationSources.forEach(source -> register(ids, source.id(), "information source"));
    inputs.forEach(input -> register(ids, input.id(), "input"));
    for (InputDefinition input : inputs) {
      if (input instanceof CollectionInputDefinition collection
          && informationSources.stream()
              .noneMatch(source -> source.id().equals(collection.informationSourceId()))) {
        throw new IllegalArgumentException(
            "collection input references unknown information source");
      }
    }
  }

  private static void register(final Map<String, String> ids, final String id, final String owner) {
    if (ids.putIfAbsent(id, owner) != null) {
      throw new IllegalArgumentException("duplicate composed riddle component id: " + id);
    }
  }
}
