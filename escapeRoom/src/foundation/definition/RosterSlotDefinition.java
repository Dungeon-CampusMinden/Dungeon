package foundation.definition;

/**
 * One stable slot in the Foundation player capacity.
 *
 * @param id stable slot identifier
 * @param number one-based slot number in the range one through four
 */
public record RosterSlotDefinition(String id, int number) {
  /** Creates a roster-slot definition. */
  public RosterSlotDefinition {
    id = DefinitionChecks.requireId(id, "roster slot id");
    if (number < 1 || number > 4) {
      throw new IllegalArgumentException("roster slot number must be in the range 1..4");
    }
  }
}
