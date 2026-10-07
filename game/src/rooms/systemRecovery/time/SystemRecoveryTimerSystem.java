package rooms.systemRecovery.time;

import engine.Entity;
import engine.System;
import feature.timer.WorldTimerComponent;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;

/** Drives the authoritative countdown even while dialogs or the pause menu are open. */
public final class SystemRecoveryTimerSystem extends System {

  /** Creates the server-only, non-pausable clock system. */
  public SystemRecoveryTimerSystem() {
    super(AuthoritativeSide.SERVER, SystemRecoveryTimeLimit.class, WorldTimerComponent.class);
  }

  @Override
  public void execute() {
    if (SystemRecovery.levelEditorMode()) return;
    SystemRecoveryLevel.currentLevel()
        .ifPresent(level -> filteredEntityStream().forEach(entity -> update(entity, level)));
  }

  private void update(Entity entity, SystemRecoveryLevel level) {
    SystemRecoveryTimeLimit clock = entity.fetch(SystemRecoveryTimeLimit.class).orElseThrow();
    boolean complete =
        SystemRecoveryProgressNet.activeStep().orElse(null) == SystemRecoveryLearningStep.COMPLETE;
    if (complete) clock.finish();

    int remaining = clock.remainingSeconds();
    int now = (int) (java.lang.System.currentTimeMillis() / 1000L);
    WorldTimerComponent display = entity.fetch(WorldTimerComponent.class).orElseThrow();
    if (display.duration() != remaining
        || ((!clock.started() || complete) && display.timestamp() != now)) {
      entity.add(new WorldTimerComponent(now, remaining));
    }
    if (clock.expired()) level.expireTimeLimit();
  }

  @Override
  public void stop() {
    // The deletion deadline continues during dialogs and pauses.
  }
}
