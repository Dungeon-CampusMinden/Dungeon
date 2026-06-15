package core.network.telemetry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A line in the structured network telemetry report.
 *
 * @param spans drawable text spans that make up the line
 */
public record TelemetryLine(List<TelemetrySpan> spans) {

  /**
   * Creates a telemetry line.
   *
   * @param spans drawable text spans that make up the line
   */
  public TelemetryLine {
    spans = List.copyOf(Objects.requireNonNull(spans, "spans"));
  }

  /**
   * Returns this line without styling information.
   *
   * @return concatenated text from all spans
   */
  public String plainText() {
    return spans.stream().map(TelemetrySpan::text).collect(Collectors.joining());
  }
}
