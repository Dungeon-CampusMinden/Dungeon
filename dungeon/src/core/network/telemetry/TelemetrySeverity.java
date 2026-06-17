package core.network.telemetry;

/** Severity used when rendering a network telemetry text span. */
public enum TelemetrySeverity {
  /** Normal informational text. */
  NORMAL,

  /** A value that violates the telemetry overlay's expected runtime range. */
  BAD
}
