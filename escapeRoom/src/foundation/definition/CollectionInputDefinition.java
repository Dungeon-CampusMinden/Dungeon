package foundation.definition;

/**
 * Required active-state interaction with one information source.
 *
 * @param id stable input identifier
 * @param informationSourceId referenced source identifier
 */
public record CollectionInputDefinition(String id, String informationSourceId)
    implements InputDefinition {
  /** Creates an immutable collection input. */
  public CollectionInputDefinition {
    id = DefinitionChecks.requireId(id, "collection input id");
    informationSourceId =
        DefinitionChecks.requireId(informationSourceId, "collection information source id");
  }
}
