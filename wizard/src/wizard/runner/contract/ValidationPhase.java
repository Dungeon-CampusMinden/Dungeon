package wizard.runner.contract;

/** Ordered phases in which Runner issues can be produced. */
public enum ValidationPhase {
  /** Input byte and project boundary checks. */
  INPUT,
  /** JSON Schema checks. */
  SCHEMA,
  /** Cross-reference checks. */
  REFERENCES,
  /** Graph checks. */
  GRAPH,
  /** Supported-capability checks. */
  CAPABILITY,
  /** Asset checks. */
  ASSETS,
  /** Runner feasibility checks before a room is started. */
  FEASIBILITY
}
