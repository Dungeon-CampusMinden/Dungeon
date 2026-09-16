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

  /** The first instruction after the player pulls the damaged energy lever. */
  public static final StoryStep ENERGY_ARRAY = step("energy-array", "riddle1", "array");

  /** The values required after the energy array exists. */
  public static final StoryStep ENERGY_VALUES = step("energy-values", "riddle1", "values");

  /** The physical battery sequence after the energy values have been accepted. */
  public static final StoryStep ENERGY_BATTERY = step("energy-battery", "riddle1", "battery");

  /** The next declaration for the module-storage room. */
  public static final StoryStep MODULE_ARRAY = step("module-array", "riddle2", "array");

  /** The module assignments after the module array exists. */
  public static final StoryStep MODULE_VALUES = step("module-values", "riddle2", "values");

  /** The single removal operation for the defective GPU. */
  public static final StoryStep REMOVE_GPU = step("remove-gpu", "riddle2", "remove_gpu");

  /** The array-length read required before the scanner can be used. */
  public static final StoryStep READ_MODULE_LENGTH = step("module-length", "riddle2", "length");

  /** The instruction shown after the player examines the confirmed array length. */
  public static final StoryStep OPEN_SCANNER_DOOR = step("open-scanner-door", "riddle2", "door");

  /** The counting loop for the inventory scanner. */
  public static final StoryStep SCANNER_CODE = step("scanner-code", "riddle3", "loop");

  /** The physical scanner action after its code has been accepted. */
  public static final StoryStep SCANNER_LEVER = step("scanner-lever", "riddle3", "scan");

  /** The package array declaration for the transport storage. */
  public static final StoryStep PACKAGES_ARRAY = step("packages-array", "riddle4", "array");

  /** The loop that hands every package to the transport scanner. */
  public static final StoryStep PACKAGES_LOOP = step("packages-loop", "riddle4", "loop");

  /** The short transition message after the transport scan. */
  public static final StoryStep DATA_STORAGE_PROBLEM =
      step("data-storage-problem", "riddle4", "problem");

  /** The next manual comparison after the transport sequence. */
  public static final StoryStep MANUAL_SORTING = step("manual-sorting", "riddle5", "compare");

  /** The missing comparison expression for the sort-program chip. */
  public static final StoryStep BUBBLE_SORT_CODE = step("bubble-sort-code", "riddle6", "code");

  /** The instruction shown before the player enters the data archive. */
  public static final StoryStep ARCHIVE_INTRO = step("archive-intro", "riddle7", "intro");

  /** The three array declarations for the data archive. */
  public static final StoryStep ARCHIVE_ARRAYS = step("archive-arrays", "riddle7", "arrays");

  /** The transition message after the archive arrays unlock the storage room. */
  public static final StoryStep STORAGE_UNLOCKED = step("storage-unlocked", "riddle8", "intro");

  /** The two-dimensional array declaration for the storage room. */
  public static final StoryStep STORAGE_ARRAY = step("storage-array", "riddle8", "create");

  /** The marked coordinate assignments in the storage room. */
  public static final StoryStep STORAGE_VALUES = step("storage-values", "riddle8", "fill");

  /** The prepared search-chip program after the empty chip has been inserted. */
  public static final StoryStep SEARCH_PROGRAM = step("search-program", "riddle9", "program");

  /** The first central-computer check. */
  public static final StoryStep CENTRAL_SORT = step("central-sort", "riddle10", "sort");

  /** The central module-count check. */
  public static final StoryStep CENTRAL_COUNT = step("central-count", "riddle10", "count");

  /** The central map search check. */
  public static final StoryStep CENTRAL_SEARCH = step("central-search", "riddle10", "search");

  /** The final combination of the three central-computer results. */
  public static final StoryStep CENTRAL_META = step("central-meta", "riddle10", "meta");

  /** The final story response after all central checks. */
  public static final StoryStep COMPLETED = step("completed", "riddle10", "complete");

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
          pending.step().riddleKey(), pending.step().entryKey());
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
    return new StoryStep(id, riddleKey, entryKey, id);
  }

  /**
   * One atomic instruction shown after the previous puzzle action.
   *
   * @param id stable story-step identifier
   * @param riddleKey quest-log tab key
   * @param entryKey quest-log entry key
   * @param messageKey story message key
   */
  public record StoryStep(String id, String riddleKey, String entryKey, String messageKey) {
    /**
     * Creates the keyed dialog script; the target client localizes it when displayed.
     *
     * @return keyed story script
     */
    public String script() {
      return SystemRecoveryText.story(messageKey);
    }
  }

  private record PendingDialog(long executeAt, StoryStep step, int playerId) {}
}
