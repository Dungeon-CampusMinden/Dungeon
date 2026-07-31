package foundation.definition;

/**
 * The one common exit associated with the room's controlled door.
 *
 * @param id stable exit identifier
 * @param doorId stable reference to the common door
 */
public record ExitDefinition(String id, String doorId) {
  /** Creates an exit definition. */
  public ExitDefinition {
    id = DefinitionChecks.requireId(id, "exit id");
    doorId = DefinitionChecks.requireId(doorId, "exit door id");
  }
}
