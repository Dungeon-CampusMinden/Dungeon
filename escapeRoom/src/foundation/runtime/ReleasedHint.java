package foundation.runtime;

import foundation.definition.HintSeverity;
import java.util.Objects;

/**
 * One authored hint that authority has explicitly released.
 *
 * @param id stable hint identifier
 * @param title player-facing title
 * @param text player-facing text
 * @param severity disclosure category announced before release
 */
public record ReleasedHint(String id, String title, String text, HintSeverity severity) {
  /** Creates a released immutable hint. */
  public ReleasedHint {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(severity, "severity");
  }
}
