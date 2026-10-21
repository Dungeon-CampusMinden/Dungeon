package escaperoom.foundation.definition;

/** Closed Foundation input definition. */
public sealed interface InputDefinition permits CollectionInputDefinition, SurfaceInputDefinition {
  /**
   * Returns the stable input identifier.
   *
   * @return stable identifier
   */
  String id();
}
