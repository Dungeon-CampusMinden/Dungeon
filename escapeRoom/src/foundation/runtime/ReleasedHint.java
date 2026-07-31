package foundation.runtime;

import java.util.Objects;

/**
 * One authored hint that authority has explicitly released.
 *
 * @param id stable hint identifier
 * @param title player-facing title
 * @param text player-facing text
 * @param severity one-based authored release order
 */
public record ReleasedHint(String id, String title, String text, int severity) {
  /** Creates a released immutable hint. */
  public ReleasedHint {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(text, "text");
    if (severity < 1) {
      throw new IllegalArgumentException("severity must be positive");
    }
  }
}
