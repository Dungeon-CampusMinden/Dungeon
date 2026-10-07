package rooms.systemRecovery.time;

import engine.Entity;
import engine.utils.Point;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.timer.WorldTimerFactory;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the interactive world countdown shown at the custom point {@code timer}. */
public final class SystemRecoveryTimerFactory {

  /** Entity name of the shared countdown. */
  public static final String NAME = "system_recovery_timer";

  private SystemRecoveryTimerFactory() {}

  /**
   * Creates the countdown entity; clicking it names the remaining minutes.
   *
   * @param position world position of the display
   * @param timeLimit authoritative countdown carried by the entity
   * @return countdown entity, not yet added to the game
   */
  public static Entity create(Point position, SystemRecoveryTimeLimit timeLimit) {
    // The budget counts from the play clock's start after the intro.
    Entity timer =
        WorldTimerFactory.createWorldTimer(position, 0L, SystemRecoveryTimeLimit.TOTAL_SECONDS);
    timer.name(NAME);
    timer.add(timeLimit);
    timer.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogFactory.showOkDialog(
                        remainingTimeText(timeLimit.remainingSeconds()),
                        SystemRecoveryText.key("timer.title"),
                        () -> {},
                        who.id()))));
    return timer;
  }

  /**
   * @param remainingSeconds current countdown
   * @return transport-safe text naming the started minutes, with a singular for the last one
   */
  static String remainingTimeText(int remainingSeconds) {
    int minutes = (remainingSeconds + 59) / 60;
    if (minutes == 1) return SystemRecoveryText.key("timer.remaining-one");
    return SystemRecoveryText.key("timer.remaining", minutes);
  }
}
