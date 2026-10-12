package rooms.lasthour.petrinet;

import java.util.EnumSet;
import java.util.Set;

/** Irreversible room milestones; reversible controls remain part of the saved room state. */
public enum LastHourMilestone {
  POWER_ON,
  LOGIN_SUCCEEDED,
  STORAGE_OPENED,
  BLUE_USB_INSERTED,
  PAPERS_SPAWNED,
  CODE_ASSEMBLED,
  EXIT_UNLOCKED,
  EXIT_OPENED,
  ESCAPED;

  /**
   * Returns the milestones that must already be completed before this transition can fire.
   *
   * @return the prerequisite milestones for this transition
   */
  public Set<LastHourMilestone> prerequisites() {
    return switch (this) {
      case LOGIN_SUCCEEDED -> EnumSet.of(POWER_ON);
      case BLUE_USB_INSERTED -> EnumSet.of(LOGIN_SUCCEEDED, STORAGE_OPENED);
      case PAPERS_SPAWNED, EXIT_UNLOCKED -> EnumSet.of(BLUE_USB_INSERTED);
      case CODE_ASSEMBLED -> EnumSet.of(PAPERS_SPAWNED);
      case EXIT_OPENED -> EnumSet.of(EXIT_UNLOCKED);
      case ESCAPED -> EnumSet.of(EXIT_OPENED);
      default -> Set.of();
    };
  }
}
