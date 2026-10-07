package rooms.systemRecovery.time;

import engine.Game;
import engine.System;
import feature.hints.HintSystem;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.story.SystemRecoveryHintPhone;

/** Observes shared progress and triggers inactivity assistance on the authoritative server. */
public final class SystemRecoveryTimedHintSystem extends System {

  private final boolean forceHints;
  private SystemRecoveryTimeLimit observedClock;
  private SystemRecoveryLearningStep observedStep;
  private int observedHintCount;

  /** Creates the non-pausable hint scheduler using the room's configured assistance mode. */
  public SystemRecoveryTimedHintSystem() {
    this(SystemRecovery.FORCE_HINTS);
  }

  SystemRecoveryTimedHintSystem(boolean forceHints) {
    super(AuthoritativeSide.SERVER, SystemRecoveryTimeLimit.class);
    this.forceHints = forceHints;
  }

  @Override
  public void execute() {
    if (SystemRecovery.levelEditorMode()) return;
    SystemRecoveryLevel.currentLevel()
        .ifPresent(
            level ->
                filteredEntityStream()
                    .map(entity -> entity.fetch(SystemRecoveryTimeLimit.class).orElseThrow())
                    .forEach(clock -> update(level, clock)));
  }

  private void update(SystemRecoveryLevel level, SystemRecoveryTimeLimit clock) {
    SystemRecoveryLearningStep step = SystemRecoveryProgressNet.activeStep().orElse(null);
    if (clock != observedClock) {
      // Start/load already sets the idle deadline; observing it must not grant extra time.
      observedClock = clock;
      observedStep = step;
      observedHintCount = 0;
    } else if (step != observedStep) {
      observedStep = step;
      observedHintCount = 0;
      level.resetTimedHintDelay();
    }
    if (step == null || step == SystemRecoveryLearningStep.COMPLETE) return;

    if (step == SystemRecoveryLearningStep.ENERGY_ARRAY
        && !level.initialTerminalAttemptRecorded()) {
      if (clock.hintDue() && level.triggerIdleOpeningCall()) {
        clock.postponeHint();
      }
      return;
    }

    Game.system(HintSystem.class, hints -> updateSharedHints(level, clock, step, hints));
  }

  private void updateSharedHints(
      SystemRecoveryLevel level,
      SystemRecoveryTimeLimit clock,
      SystemRecoveryLearningStep step,
      HintSystem hints) {
    var placeId = SystemRecoveryProgressNet.hintEntityId(step);
    if (placeId.isEmpty()) return;
    var place = Game.findEntityById(placeId.orElseThrow()).orElse(null);
    if (place == null) return;
    int accepted = hints.sharedProgress(place).acceptedCount();
    if (accepted != observedHintCount) {
      observedHintCount = accepted;
      level.resetTimedHintDelay();
    }
    if (!clock.hintDue() || hints.peekSharedHint().isEmpty()) return;
    boolean offered =
        forceHints
            ? SystemRecoveryHintPhone.deliverAutomaticHint()
            : level.triggerTimedHintReminder();
    if (offered) {
      observedHintCount = hints.sharedProgress(place).acceptedCount();
      clock.postponeHint();
    }
  }

  @Override
  public void stop() {
    // Assistance must still become due while a player is working in the computer dialog.
  }
}
