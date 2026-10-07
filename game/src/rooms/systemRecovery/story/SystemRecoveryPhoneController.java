package rooms.systemRecovery.story;

import engine.Entity;
import engine.Game;
import engine.sound.Sounds;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.utils.EntityUtils;
import rooms.lasthour.util.LastHourSounds;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Owns the shared System Recovery phone, its call lifecycle, and its hint interaction. */
public final class SystemRecoveryPhoneController {

  private final Runnable openDataStorage;
  private final Runnable openElevator;
  private final Runnable saveCheckpoint;
  private Entity phone;
  private Entity ringingPhoneEmote;
  private boolean openingCallTriggered;
  private boolean dataStorageProblemCallTriggered;
  private boolean systemCoreWarningCallTriggered;
  private boolean systemCoreWarningCallAnswered;
  private boolean finalEchoCallTriggered;
  private boolean finalEchoCallPending;
  private Call ringingCall;

  /**
   * @param openDataStorage action performed after ECHO's data-storage call
   * @param openElevator action performed after ECHO's final call
   */
  public SystemRecoveryPhoneController(Runnable openDataStorage, Runnable openElevator) {
    this(openDataStorage, openElevator, () -> {});
  }

  /**
   * @param openDataStorage action performed after ECHO's data-storage call
   * @param openElevator action performed after ECHO's final call
   * @param saveCheckpoint persists call completion at the current puzzle checkpoint
   */
  public SystemRecoveryPhoneController(
      Runnable openDataStorage, Runnable openElevator, Runnable saveCheckpoint) {
    this.openDataStorage = openDataStorage == null ? () -> {} : openDataStorage;
    this.openElevator = openElevator == null ? () -> {} : openElevator;
    this.saveCheckpoint = saveCheckpoint == null ? () -> {} : saveCheckpoint;
  }

  /**
   * Creates the phone entity and attaches its interaction.
   *
   * @param phonePoint custom point at which the phone is spawned
   */
  public void setup(Point phonePoint) {
    phone = DecoFactory.createDeco(phonePoint, Deco.Phone);
    phone.remove(feature.components.DecoComponent.class);
    engine.systems.DrawSystem.getInstance()
        .changeEntityDepth(phone, DepthLayer.AbovePlayer.depth());
    Game.add(phone);
    updatePhoneInteraction();
  }

  /**
   * Marks the opening call as already handled when a save starts after the introduction.
   *
   * @param pastIntroduction whether the restored checkpoint is beyond the introduction
   */
  public void restorePastIntroduction(boolean pastIntroduction) {
    openingCallTriggered = pastIntroduction;
  }

  /** Starts ECHO's introductory call after the first rejected terminal attempt. */
  public void triggerOpeningCall() {
    startOpeningCall("opening-call");
  }

  /** Starts ECHO's successful first-contact call after AXIOM's dialog has closed. */
  public void triggerCorrectOpeningCall() {
    startOpeningCall("opening-call-correct");
  }

  /**
   * Introduces ECHO after inactivity before the player has submitted any terminal input.
   *
   * @return whether the first-contact call was started
   */
  public boolean triggerIdleOpeningCall() {
    return startOpeningCall("opening-call-idle");
  }

  private boolean startOpeningCall(String key) {
    if (openingCallTriggered || ringingCall != null || phone == null) return false;
    openingCallTriggered = true;
    startRingingCall(key);
    return true;
  }

  /**
   * Offers help after inactivity without replacing an unanswered main-quest call.
   *
   * @return whether a reminder call was started
   */
  public boolean triggerHintReminder() {
    if (phone == null || ringingCall != null) return false;
    if (!openingCallTriggered) {
      return triggerIdleOpeningCall();
    }
    startRingingCall("hint-reminder");
    return true;
  }

  /** Removes an obsolete optional reminder after progress, but never cancels a story call. */
  public void cancelHintReminder() {
    if (ringingCall == null || !"hint-reminder".equals(ringingCall.key())) return;
    ringingCall = null;
    clearRingingPhoneEmote();
    updatePhoneInteraction();
  }

  /** Starts ECHO's transition call before the data-storage room opens. */
  public void triggerDataStorageProblemCall() {
    if (dataStorageProblemCallTriggered || phone == null) return;
    dataStorageProblemCallTriggered = true;
    startRingingCall("data-storage-problem");
  }

  /** Starts ECHO's warning call after AXIOM has obtained system-core access. */
  public void triggerSystemCoreWarningCall() {
    if (systemCoreWarningCallAnswered || systemCoreWarningCallTriggered || phone == null) return;
    systemCoreWarningCallTriggered = true;
    startRingingCall("system-core-warning");
  }

  /**
   * Reconstructs pending system-core calls after loading a core checkpoint.
   *
   * @param warningCallAnswered whether the warning call was completed before saving
   * @param callsPending whether any core call should still be presented
   * @param finalCallPending whether the final call should follow the warning call
   */
  public void restoreSystemCoreCalls(
      boolean warningCallAnswered, boolean callsPending, boolean finalCallPending) {
    systemCoreWarningCallAnswered = warningCallAnswered;
    systemCoreWarningCallTriggered = warningCallAnswered;
    if (!callsPending) return;
    if (warningCallAnswered) {
      if (finalCallPending) triggerFinalEchoCall();
      return;
    }
    this.finalEchoCallPending = finalCallPending;
    triggerSystemCoreWarningCall();
  }

  /**
   * @return whether the player has completed ECHO's system-core warning call
   */
  public boolean systemCoreWarningCallAnswered() {
    return systemCoreWarningCallAnswered;
  }

  /** Starts ECHO's final call after the system-core routines are complete. */
  public void triggerFinalEchoCall() {
    if (finalEchoCallTriggered || phone == null) return;
    finalEchoCallTriggered = true;
    cancelHintReminder();
    if (ringingCall != null) {
      finalEchoCallPending = true;
      return;
    }
    startRingingCall("final-call");
  }

  private void startRingingCall(String callKey) {
    ringingCall = new Call(callKey);
    Sounds.play(LastHourSounds.PHONE_RINGING);
    updatePhoneInteraction();
    if (ringingPhoneEmote == null) {
      ringingPhoneEmote =
          EmoteFactory.createEmote(
              EntityUtils.getPosition(phone), Emote.EXCLAMATION, 60 * 60 * 1000);
      Game.add(ringingPhoneEmote);
    }
  }

  /** Keeps the phone usable for calls, hints, or the dead-line response. */
  private void updatePhoneInteraction() {
    if (phone == null) return;
    phone.remove(InteractionComponent.class);
    phone.add(
        new InteractionComponent(
            new Interaction(
                (_, who) -> {
                  if (ringingCall != null) {
                    // The call is now being answered. Remove the visual ringing state immediately
                    // so a dismissed first call cannot leave an obsolete bubble behind.
                    clearRingingPhoneEmote();
                    Call call = ringingCall;
                    DialogFactory.showDialogDialog(
                        SystemRecoveryText.echoCall(call.key()),
                        SystemRecoveryText.echoSpeakerImage(),
                        () -> finishEchoCall(call),
                        who.id());
                    return;
                  }
                  if (!openingCallTriggered) {
                    DialogFactory.showDialogDialog(
                        SystemRecoveryText.echoCall("dead-line"), () -> {}, who.id());
                    return;
                  }
                  SystemRecoveryHintPhone.request(who);
                })));
  }

  private void finishEchoCall(Call completedCall) {
    // Identity matters: an old callback must not finish a later call with the same script.
    if (completedCall != ringingCall) return;
    String completedCallKey = completedCall.key();
    ringingCall = null;
    updatePhoneInteraction();
    clearRingingPhoneEmote();
    switch (completedCallKey) {
      case "hint-reminder" -> {}
      case "system-core-warning" -> {
        systemCoreWarningCallAnswered = true;
        SystemRecoveryQuestLogUtil.addDialogEntry(
            "riddle10", completedCallKey, SystemRecoveryText.echoCall(completedCallKey));
        saveCheckpoint.run();
      }
      case "data-storage-problem" -> {
        SystemRecoveryAchievements.phoneCallAnswered(true);
        SystemRecoveryQuestLogUtil.addDialogEntry(
            "riddle5", completedCallKey, SystemRecoveryText.echoCall(completedCallKey));
        openDataStorage.run();
      }
      case "final-call" -> {
        SystemRecoveryQuestLogUtil.addDialogEntry(
            "riddle10", completedCallKey, SystemRecoveryText.echoCall(completedCallKey));
        openElevator.run();
      }
      default -> {
        SystemRecoveryAchievements.phoneCallAnswered(false);
        SystemRecoveryQuestLogUtil.addDialogEntry(
            "riddle1", completedCallKey, SystemRecoveryText.echoCall(completedCallKey));
      }
    }
    if (finalEchoCallPending) {
      finalEchoCallPending = false;
      finalEchoCallTriggered = true;
      startRingingCall("final-call");
    }
  }

  private void clearRingingPhoneEmote() {
    if (ringingPhoneEmote == null) return;
    Game.remove(ringingPhoneEmote);
    ringingPhoneEmote = null;
  }

  private record Call(String key) {}
}
