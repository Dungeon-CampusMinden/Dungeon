package escaperoom.foundation.definition;

import java.util.Objects;

/**
 * Shared authoritative timer definition.
 *
 * @param limitMinutes time limit from one through 240 whole minutes
 * @param mode hard or soft expiry behavior
 */
public record TimerDefinition(int limitMinutes, TimerMode mode) {
  /** Creates a timer definition. */
  public TimerDefinition {
    if (limitMinutes < 1 || limitMinutes > 240) {
      throw new IllegalArgumentException("timer limit must be in the range 1..240");
    }
    Objects.requireNonNull(mode, "mode");
  }
}
