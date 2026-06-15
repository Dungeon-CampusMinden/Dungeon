package core.network.telemetry;

import java.util.Objects;

/**
 * A drawable network telemetry text segment.
 *
 * @param text text to draw
 * @param severity severity used to choose the text color
 */
public record TelemetrySpan(String text, TelemetrySeverity severity) {

  /**
   * Creates a telemetry span.
   *
   * @param text text to draw
   * @param severity severity used to choose the text color
   */
  public TelemetrySpan {
    text = Objects.requireNonNullElse(text, "");
    severity = Objects.requireNonNullElse(severity, TelemetrySeverity.NORMAL);
  }
}
