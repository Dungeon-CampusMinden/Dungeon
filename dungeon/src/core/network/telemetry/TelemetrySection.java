package core.network.telemetry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A titled section in the structured network telemetry report.
 *
 * @param title section title
 * @param lines section lines
 */
public record TelemetrySection(String title, List<TelemetryLine> lines) {

  /**
   * Creates a telemetry section.
   *
   * @param title section title
   * @param lines section lines
   */
  public TelemetrySection {
    title = Objects.requireNonNullElse(title, "");
    lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
  }

  /**
   * Returns this section without styling information.
   *
   * @return section title and indented lines
   */
  public String plainText() {
    if (lines.isEmpty()) {
      return title;
    }
    return title
        + "\n"
        + lines.stream().map(line -> "  " + line.plainText()).collect(Collectors.joining("\n"));
  }
}
