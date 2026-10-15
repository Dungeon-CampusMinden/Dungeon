package escaperoom.foundation.definition;

/** Input answered at its own placed device surface. */
public sealed interface SurfaceInputDefinition extends InputDefinition
    permits AssignmentInputDefinition, ChoiceInputDefinition, NumericInputDefinition {
  /**
   * Returns the authored device surface identifier.
   *
   * @return stable surface identifier
   */
  String surfaceId();
}
