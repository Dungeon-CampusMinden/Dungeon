package modules.computer;

import com.badlogic.gdx.utils.TimeUtils;
import core.System;
import core.game.PreRunConfiguration;
import util.Lore;

/**
 * Client-side ECS system that keeps the local {@link ComputerDialog} in sync with the shared {@link
 * ComputerStateComponent} every frame and drives the unknown-device shutdown timer locally.
 *
 * <p>State propagation runs in both single-player and multiplayer client setups. The local shutdown
 * timer only runs in single-player; in multiplayer the server's {@link
 * contrib.systems.EventScheduler} authoritatively drives the shutdown and broadcasts the resulting
 * state to clients.
 */
public class ComputerStateSyncSystem extends System {

  /** Sentinel value indicating no shutdown is currently scheduled. */
  private static final long NO_SHUTDOWN_SCHEDULED = 0L;

  /**
   * Absolute time (in ms, see {@link TimeUtils#millis()}) at which the unknown-device shutdown
   * should fire locally, or {@link #NO_SHUTDOWN_SCHEDULED} when no shutdown is pending.
   */
  private long unknownDeviceShutdownAt = NO_SHUTDOWN_SCHEDULED;

  /** Creates a new ComputerStateSyncSystem that operates on the client side. */
  public ComputerStateSyncSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    ComputerStateComponent.getState()
        .ifPresent(
            state -> {
              propagateToDialog(state);
              if (!PreRunConfiguration.multiplayerEnabled()) {
                driveLocalShutdownTimer(state);
              }
            });
  }

  /**
   * Pushes the latest shared state into the open {@link ComputerDialog}, if any.
   *
   * @param state the current shared computer state
   */
  private void propagateToDialog(ComputerStateComponent state) {
    ComputerDialog.getInstance()
        .ifPresent(
            cd -> {
              if (cd.sharedState() != state) {
                cd.updateState(state);
              }
            });
  }

  /**
   * Tracks the unknown-device infection locally and triggers {@link
   * ComputerFactory#shutdownPcAfterUnknownDevice()} once the configured delay has elapsed. Used in
   * single-player mode where the {@link contrib.systems.EventScheduler} is paused while the dialog
   * is open and therefore cannot fire the scheduled shutdown.
   *
   * @param state the current shared computer state
   */
  private void driveLocalShutdownTimer(ComputerStateComponent state) {
    boolean unknownDeviceActive =
        state.isInfected() && Lore.UnknownDeviceVirusType.equals(state.virusType());
    if (!unknownDeviceActive) {
      unknownDeviceShutdownAt = NO_SHUTDOWN_SCHEDULED;
      return;
    }
    if (unknownDeviceShutdownAt == NO_SHUTDOWN_SCHEDULED) {
      unknownDeviceShutdownAt =
          TimeUtils.millis() + ComputerFactory.UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS;
      return;
    }
    if (TimeUtils.millis() >= unknownDeviceShutdownAt) {
      unknownDeviceShutdownAt = NO_SHUTDOWN_SCHEDULED;
      ComputerFactory.shutdownPcAfterUnknownDevice();
    }
  }

  /** Cannot be paused. */
  @Override
  public void stop() {

  }
}
