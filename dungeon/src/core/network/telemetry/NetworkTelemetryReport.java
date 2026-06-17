package core.network.telemetry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Structured network telemetry report used by the debug overlay and clipboard export.
 *
 * @param sections report sections
 */
public record NetworkTelemetryReport(List<TelemetrySection> sections) {

  /**
   * Creates a telemetry report.
   *
   * @param sections report sections
   */
  public NetworkTelemetryReport {
    sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
  }

  /**
   * Returns this report without styling information.
   *
   * @return all sections as plain text
   */
  public String plainText() {
    return sections.stream().map(TelemetrySection::plainText).collect(Collectors.joining("\n"));
  }
}
