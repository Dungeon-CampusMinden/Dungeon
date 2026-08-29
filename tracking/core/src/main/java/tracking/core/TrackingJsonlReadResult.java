package tracking.core;

import java.util.List;
import java.util.Objects;

/**
 * Result of reading an outbox with recovery enabled.
 *
 * @param events complete events in file order
 * @param truncatedTailIgnored whether an incomplete or invalid UTF-8 unterminated tail was ignored
 */
public record TrackingJsonlReadResult(List<TrackingEvent> events, boolean truncatedTailIgnored) {
  /** Copies the event list so callers cannot change the result. */
  public TrackingJsonlReadResult {
    events = List.copyOf(Objects.requireNonNull(events, "events"));
  }
}
