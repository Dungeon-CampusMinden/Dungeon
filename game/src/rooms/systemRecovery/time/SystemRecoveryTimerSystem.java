package rooms.systemRecovery.time;

import engine.Entity;
import engine.System;
import feature.timer.WorldTimerComponent;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;

/**
 * Freezes the countdown at the final puzzle and starts the failure ending when the budget expires.
 * Runs while dialogs pause the simulation, because the play clock keeps running during them.
 */
public final class SystemRecoveryTimerSystem extends System {

  /** Creates the server-only, non-pausable timer system. */
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
    SystemRecoveryTimeLimit timeLimit = entity.fetch(SystemRecoveryTimeLimit.class).orElseThrow();
    if (SystemRecoveryProgressNet.activeStep().orElse(null)
        == SystemRecoveryLearningStep.COMPLETE) {
      long finishedAt = timeLimit.finish();
      WorldTimerComponent display = entity.fetch(WorldTimerComponent.class).orElseThrow();
      if (display.stoppedAtActiveMs() != finishedAt) entity.add(display.stoppedAt(finishedAt));
    }
    if (timeLimit.expired()) level.expireTimeLimit();
  }

  @Override
  public void stop() {
    // The deadline follows the play clock, which keeps running while dialogs are open.
  }
}
