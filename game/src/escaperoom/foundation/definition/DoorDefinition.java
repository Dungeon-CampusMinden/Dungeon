package escaperoom.foundation.definition;

/**
 * The one common exit door controlled by Foundation authority.
 *
 * @param id stable door identifier
 */
public record DoorDefinition(String id) {
  /** Creates a door definition. */
  public DoorDefinition {
    id = DefinitionChecks.requireId(id, "door id");
  }
}
