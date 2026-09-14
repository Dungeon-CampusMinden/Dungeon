package rooms.systemRecovery.story;

import engine.Game;
import engine.components.PlayerComponent;
import feature.hud.dialogs.DialogFactory;
import feature.systems.LevelEditorSystem;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
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

  /** The final story response after all central checks. */
  public static final StoryStep COMPLETED = step("completed", "riddle10", "complete");

  private final Set<String> shownToPlayer = ConcurrentHashMap.newKeySet();
  private final Queue<PendingDialog> pendingDialogs = new ConcurrentLinkedQueue<>();
  private static final ThreadLocal<Integer> TERMINAL_PLAYER = new ThreadLocal<>();

  /** Creates the story controller for one authoritative level instance. */
  public SystemRecoveryStoryDialogs() {}

  /** Dispatches delayed dialogs. This is intentionally inert while the level editor is active. */
  public void tick() {
    if (!Game.isHeadless() && LevelEditorSystem.active()) return;

    long now = System.currentTimeMillis();
    PendingDialog pending;
    while ((pending = pendingDialogs.peek()) != null && pending.executeAt() <= now) {
      pendingDialogs.poll();
      SystemRecoveryQuestLogUtil.addDialogEntry(
          pending.step().riddleKey(), pending.step().entryKey());
      DialogFactory.showDialogDialog(pending.step().script(), () -> {}, pending.playerId());
    }
  }

  /** Queues one instruction for one player after a successful personal action. */
  public void announceForPlayer(StoryStep step, int playerId) {
    showStepAfterDelay(step, playerId);
  }

  /** Queues one shared-room instruction for every currently connected player. */
  public void announceToAllPlayers(StoryStep step) {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .mapToInt(engine.Entity::id)
        .forEach(playerId -> showStepAfterDelay(step, playerId));
  }

  /** Queues the final shared story response. */
  public void announceCompletionToAllPlayers() {
    announceToAllPlayers(COMPLETED);
  }

  /** Executes terminal work with the submitting player available to success callbacks. */
  public static <T> T withTerminalPlayer(int playerId, Supplier<T> action) {
    Integer previous = TERMINAL_PLAYER.get();
    TERMINAL_PLAYER.set(playerId);
    try {
      return action.get();
    } finally {
      if (previous == null) TERMINAL_PLAYER.remove();
      else TERMINAL_PLAYER.set(previous);
    }
  }

  /**
   * @return the player currently executing a terminal callback, if there is one
   */
  public static OptionalInt currentTerminalPlayer() {
    Integer playerId = TERMINAL_PLAYER.get();
    return playerId == null ? OptionalInt.empty() : OptionalInt.of(playerId);
  }

  private void showStepAfterDelay(StoryStep step, int playerId) {
    if (step == null || playerId < 0) return;
    String key = step.id() + ":" + playerId;
    if (!shownToPlayer.add(key)) return;
    pendingDialogs.add(
        new PendingDialog(System.currentTimeMillis() + STORY_DELAY_MS, step, playerId));
  }

  private static StoryStep step(String id, String riddleKey, String entryKey) {
    return new StoryStep(id, riddleKey, entryKey, id);
  }

  /** One atomic instruction shown after the previous puzzle action. */
  public record StoryStep(String id, String riddleKey, String entryKey, String messageKey) {
    /** Resolves the localized dialog script at display time. */
    public String script() {
      return SystemRecoveryText.story(messageKey);
    }
  }

  private record PendingDialog(long executeAt, StoryStep step, int playerId) {}
}
