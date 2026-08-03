package foundation.runtime;

import foundation.definition.HintSeverity;
import java.util.Objects;

/**
 * Non-content preview of the next hint used for server-authoritative confirmation.
 *
 * @param id stable identity captured before confirmation
 * @param severity disclosure category visible before confirmation
 */
public record HintPreview(String id, HintSeverity severity) {
  /** Creates an immutable hint preview without authored title or text. */
  public HintPreview {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(severity, "severity");
  }
}
