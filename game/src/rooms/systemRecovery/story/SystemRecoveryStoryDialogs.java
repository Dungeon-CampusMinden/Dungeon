package rooms.systemRecovery.story;

import engine.Game;
import engine.components.PlayerComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogFactory;
import feature.systems.LevelEditorSystem;
import java.util.Arrays;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import rooms.systemRecovery.modules.computer.SystemRecoveryDialogTypes;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Sends the remote user's instructions one step at a time.
 *
 * <p>Each {@link StoryStep} describes exactly one upcoming action. A step is queued only after the
 * previous action has succeeded, so the story never previews later parts of a riddle. The server
 * owns the queue; the client only receives the resulting dialog and questlog update.
 */
public final class SystemRecoveryStoryDialogs {

  private static final long STORY_DELAY_MS = 900L;

  /** The values required after the energy array exists, announced by AXIOM. */
  public static final StoryStep ENERGY_VALUES = axiomStep("energy-values", "riddle1", "values");

  /**
   * The physical battery sequence after the energy values have been accepted, announced by AXIOM.
   */
  public static final StoryStep ENERGY_BATTERY = axiomStep("energy-battery", "riddle1", "battery");

  /** The next declaration for the module-storage room. */
  public static final StoryStep MODULE_ARRAY = step("module-array", "riddle2", "array");

  /** The module assignments after the module array exists. */
  public static final StoryStep MODULE_VALUES = step("module-values", "riddle2", "values");

  /** The instruction to inspect the modules after their assignments were accepted. */
  public static final StoryStep MODULE_ASSIGNMENT =
      step("module-assignment", "riddle2", "assignment");

  /** The diagnosis shown after the player examines the defective GPU. */
  public static final StoryStep GPU_FAULT = step("gpu-fault", "riddle2", "gpu-fault");

  /** The array-length read required before the scanner can be used. */
  public static final StoryStep READ_MODULE_LENGTH = step("module-length", "riddle2", "length");

  /** The instruction shown after the player examines the confirmed array length. */
  public static final StoryStep OPEN_SCANNER_DOOR = step("open-scanner-door", "riddle2", "door");

  /** The counting loop for the inventory scanner. */
  public static final StoryStep SCANNER_CODE = step("scanner-code", "riddle3", "loop");

  /** The physical scanner action after its code has been accepted. */
  public static final StoryStep SCANNER_LEVER = step("scanner-lever", "riddle3", "scan");

  /** AXIOM's transition message after the inventory scan points to the transport storage. */
  public static final StoryStep SCANNER_COMPLETE =
      axiomStep("scanner-complete", "riddle3", "transport");

  /** AXIOM's instruction to restore the package profile in the transport storage. */
  public static final StoryStep PACKAGES_ARRAY = axiomStep("packages-array", "riddle4", "array");

  /** AXIOM's instruction to complete the package-processing routine. */
  public static final StoryStep PACKAGES_LOOP = axiomStep("packages-loop", "riddle4", "loop");

  /** ECHO's instruction to begin the manual comparison exercise. */
  public static final StoryStep MANUAL_SORTING = echoStep("manual-sorting", "riddle5", "compare");

  /** ECHO's instruction to complete the Bubble Sort program after manual sorting. */
  public static final StoryStep BUBBLE_SORT_CODE = echoStep("bubble-sort-code", "riddle6", "code");

  /** AXIOM's irritated instruction to continue in the data archive. */
  public static final StoryStep ARCHIVE_INTRO = axiomStep("archive-intro", "riddle7", "intro");

  /** AXIOM's instruction to reconstruct the missing archive data stores. */
  public static final StoryStep ARCHIVE_ARRAYS = axiomStep("archive-arrays", "riddle7", "arrays");

  /** AXIOM's transition message after the archive arrays unlock the storage room. */
  public static final StoryStep STORAGE_UNLOCKED =
      axiomStep("storage-unlocked", "riddle8", "intro");

  /** AXIOM's instruction to restore the two-dimensional storage structure. */
  public static final StoryStep STORAGE_ARRAY = axiomStep("storage-array", "riddle8", "create");

  /** AXIOM's instruction to restore the marked storage cells. */
  public static final StoryStep STORAGE_VALUES = axiomStep("storage-values", "riddle8", "fill");

  /** AXIOM's reaction when the search robot has recovered the system-core access module. */
  public static final StoryStep ACCESS_MODULE_FOUND =
      axiomStep("access-module-found", "riddle9", "access");

  /** ECHO's instruction for the first central-computer check. */
  public static final StoryStep CENTRAL_SORT = echoStep("central-sort", "riddle10", "sort");

  /** ECHO's instruction for the central module-count check. */
  public static final StoryStep CENTRAL_COUNT = echoStep("central-count", "riddle10", "count");

  /** ECHO's instruction for the central map-search check. */
  public static final StoryStep CENTRAL_SEARCH = echoStep("central-search", "riddle10", "search");

  /** ECHO's instruction for combining the three central-computer results. */
  public static final StoryStep CENTRAL_META = echoStep("central-meta", "riddle10", "meta");

  /** AXIOM's irritated response after all central checks have been accepted. */
  public static final StoryStep COMPLETED = axiomStep("completed", "riddle10", "complete");

  private final Set<String> shownToPlayer = ConcurrentHashMap.newKeySet();
  private final Queue<PendingDialog> pendingDialogs = new ConcurrentLinkedQueue<>();

  /** Creates the story controller for one authoritative level instance. */
  public SystemRecoveryStoryDialogs() {}

  /** Dispatches delayed dialogs. This is intentionally inert while the level editor is active. */
  public void tick() {
    if (!Game.isHeadless() && LevelEditorSystem.active()) return;

    long now = System.currentTimeMillis();
    int pendingCount = pendingDialogs.size();
    for (int index = 0; index < pendingCount; index++) {
      PendingDialog pending = pendingDialogs.poll();
      if (pending == null) return;

      // Keep this player's story message queued while the computer is open. Other players must
      // remain independent and may still receive their own queued message in the same tick.
      if (pending.executeAt() > now || hasOpenComputer(pending.playerId())) {
        pendingDialogs.add(pending);
        continue;
      }

      SystemRecoveryQuestLogUtil.addDialogEntry(
          pending.step().riddleKey(),
          pending.step().id(),
          pending.step().speakerKey(),
          pending.step().messageKey());
      DialogFactory.showDialogDialog(pending.step().script(), () -> {}, pending.playerId());
    }
  }

  /**
   * Queues one instruction for one player after a successful personal action.
   *
   * @param step story step to announce
   * @param playerId player receiving the instruction
   */
  public void announceForPlayer(StoryStep step, int playerId) {
    showStepAfterDelay(step, playerId);
  }

  /**
   * Queues one shared-room instruction for every currently connected player.
   *
   * @param step story step to announce
   */
  public void announceToAllPlayers(StoryStep step) {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .mapToInt(engine.Entity::id)
        .forEach(playerId -> showStepAfterDelay(step, playerId));
  }

  /** Queues the final shared story response. */
  public void announceCompletionToAllPlayers() {
    announceToAllPlayers(COMPLETED);
  }

  private void showStepAfterDelay(StoryStep step, int playerId) {
    if (step == null || playerId < 0) return;
    String key = step.id() + ":" + playerId;
    if (!shownToPlayer.add(key)) return;
    pendingDialogs.add(
        new PendingDialog(System.currentTimeMillis() + STORY_DELAY_MS, step, playerId));
  }

  /**
   * Checks the authoritative UI registry for an open System Recovery computer for one player.
   *
   * <p>The check uses the target IDs instead of client-local visibility. This keeps story delivery
   * correct in multiplayer: only the player currently working at a computer is deferred.
   *
   * @param playerId player whose computer state is checked
   * @return whether this player's computer dialog is open
   */
  private static boolean hasOpenComputer(int playerId) {
    return Game.levelEntities()
        .map(entity -> entity.fetch(UIComponent.class).orElse(null))
        .filter(java.util.Objects::nonNull)
        .anyMatch(
            ui ->
                ui.dialogContext().dialogType() == SystemRecoveryDialogTypes.COMPUTER
                    && targetsPlayer(ui, playerId));
  }

  private static boolean targetsPlayer(UIComponent ui, int playerId) {
    int[] targets = ui.targetEntityIds();
    return targets.length == 0 || Arrays.stream(targets).anyMatch(target -> target == playerId);
  }

  private static StoryStep step(String id, String riddleKey, String entryKey) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.STORY);
  }

  private static StoryStep axiomStep(String id, String riddleKey, String entryKey) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.AXIOM);
  }

  private static StoryStep echoStep(String id, String riddleKey, String entryKey) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.ECHO);
  }

  /**
   * One atomic instruction shown after the previous puzzle action.
   *
   * @param id stable story-step identifier
   * @param riddleKey quest-log tab key
   * @param entryKey quest-log entry key
   * @param messageKey story message key
   * @param speaker voice used for the dialog
   */
  public record StoryStep(
      String id, String riddleKey, String entryKey, String messageKey, Speaker speaker) {
    /**
     * Creates the keyed dialog script; the target client localizes it when displayed.
     *
     * @return keyed story script
     */
    public String script() {
      return switch (speaker) {
        case AXIOM -> SystemRecoveryText.axiomCall(messageKey);
        case ECHO -> SystemRecoveryText.echoCall(messageKey);
        case STORY -> SystemRecoveryText.story(messageKey);
      };
    }

    /** Returns the translation key for the speaker label used by this dialog. */
    public String speakerKey() {
      return switch (speaker) {
        case AXIOM -> "axiom";
        case ECHO -> "echo";
        case STORY -> "speaker";
      };
    }
  }

  private enum Speaker {
    STORY,
    AXIOM,
    ECHO
  }

  private record PendingDialog(long executeAt, StoryStep step, int playerId) {}
}
