package rooms.systemRecovery.story;

import engine.Game;
import engine.components.PlayerComponent;
import engine.utils.IVoidFunction;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
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

  // Story instructions wait in active play time; intro and credits use their own cutscenes.
  private static final long STORY_DELAY_MS = 900L;

  /** The values required after the energy array exists, announced by AXIOM. */
  public static final StoryStep ENERGY_VALUES =
      axiomStep("energy-values", "riddle1", "values", SystemRecoveryLearningStep.ENERGY_VALUES);

  /**
   * The physical battery sequence after the energy values have been accepted, announced by AXIOM.
   */
  public static final StoryStep ENERGY_BATTERY =
      axiomStep(
          "energy-battery", "riddle1", "battery", SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY);

  /** AXIOM's instruction to declare the module-storage array. */
  public static final StoryStep MODULE_ARRAY =
      axiomStep("module-array", "riddle2", "array", SystemRecoveryLearningStep.MODULE_ARRAY);

  /** AXIOM's instruction to assign the module values. */
  public static final StoryStep MODULE_VALUES =
      axiomStep("module-values", "riddle2", "values", SystemRecoveryLearningStep.MODULE_VALUES);

  /** AXIOM's instruction to inspect the modules after their assignments were accepted. */
  public static final StoryStep MODULE_ASSIGNMENT =
      axiomStep(
          "module-assignment",
          "riddle2",
          "assignment",
          SystemRecoveryLearningStep.MODULE_REMOVE_GPU);

  /** AXIOM's diagnosis shown after the player examines the defective GPU. */
  public static final StoryStep GPU_FAULT =
      axiomStep("gpu-fault", "riddle2", "gpu-fault", SystemRecoveryLearningStep.MODULE_REMOVE_GPU);

  /** AXIOM's instruction to read the module-array length before the scanner can be used. */
  public static final StoryStep READ_MODULE_LENGTH =
      axiomStep("module-length", "riddle2", "length", SystemRecoveryLearningStep.MODULE_LENGTH);

  /** AXIOM's instruction after the player examines the confirmed array length. */
  public static final StoryStep OPEN_SCANNER_DOOR =
      axiomStep("open-scanner-door", "riddle2", "door", SystemRecoveryLearningStep.ROOM2_DOOR_CODE);

  /** AXIOM's instruction for the inventory-scanner counting loop. */
  public static final StoryStep SCANNER_CODE =
      axiomStep("scanner-code", "riddle3", "loop", SystemRecoveryLearningStep.INVENTORY_COUNT);

  /** AXIOM's instruction for the physical scanner action after its code is accepted. */
  public static final StoryStep SCANNER_LEVER =
      axiomStep("scanner-lever", "riddle3", "scan", SystemRecoveryLearningStep.ROOM3_DOOR_CODE);

  /** AXIOM's transition message after the inventory scan points to the transport storage. */
  public static final StoryStep SCANNER_COMPLETE =
      axiomStep(
          "scanner-complete", "riddle3", "transport", SystemRecoveryLearningStep.ROOM3_DOOR_CODE);

  /** AXIOM's instruction to restore the package profile in the transport storage. */
  public static final StoryStep PACKAGES_ARRAY =
      axiomStep("packages-array", "riddle4", "array", SystemRecoveryLearningStep.TRANSPORT_ARRAY);

  /** AXIOM's instruction to complete the package-processing routine. */
  public static final StoryStep PACKAGES_LOOP =
      axiomStep(
          "packages-loop", "riddle4", "loop", SystemRecoveryLearningStep.TRANSPORT_COLLECT_LOOP);

  /** ECHO's instruction to begin the manual comparison exercise. */
  public static final StoryStep MANUAL_SORTING =
      echoStep("manual-sorting", "riddle5", "compare", SystemRecoveryLearningStep.MANUAL_SORTING);

  /** ECHO's instruction to complete the Bubble Sort program after manual sorting. */
  public static final StoryStep BUBBLE_SORT_CODE =
      echoStep(
          "bubble-sort-code", "riddle6", "code", SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION);

  /** AXIOM's irritated instruction to continue in the data archive. */
  public static final StoryStep ARCHIVE_INTRO =
      axiomStep("archive-intro", "riddle7", "intro", SystemRecoveryLearningStep.ARCHIVE_ACCESS);

  /** AXIOM's instruction to reconstruct the missing archive data stores. */
  public static final StoryStep ARCHIVE_ARRAYS =
      axiomStep("archive-arrays", "riddle7", "arrays", SystemRecoveryLearningStep.ARCHIVE_ARRAYS);

  /** AXIOM's transition message after the archive arrays unlock the storage room. */
  public static final StoryStep STORAGE_UNLOCKED =
      axiomStep("storage-unlocked", "riddle8", "intro", SystemRecoveryLearningStep.STORAGE_ARRAY);

  /** AXIOM's instruction to restore the two-dimensional storage structure. */
  public static final StoryStep STORAGE_ARRAY =
      axiomStep("storage-array", "riddle8", "create", SystemRecoveryLearningStep.STORAGE_ARRAY);

  /** AXIOM's instruction to restore the marked storage cells. */
  public static final StoryStep STORAGE_VALUES =
      axiomStep("storage-values", "riddle8", "fill", SystemRecoveryLearningStep.STORAGE_VALUES);

  /** AXIOM's reaction when the search robot has recovered the system-core access module. */
  public static final StoryStep ACCESS_MODULE_FOUND =
      axiomStep(
          "access-module-found",
          "riddle9",
          "access",
          SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS);

  /** The search robot's clumsy announcement when its scan begins. */
  public static final StoryStep SEARCH_ROBOT_START =
      robotStep(
          "search-robot-start", "riddle9", "search", SystemRecoveryLearningStep.SEARCH_ROBOT_RUN);

  /** The search robot's clumsy announcement after delivering the access module. */
  public static final StoryStep SEARCH_ROBOT_COMPLETE =
      robotStep(
          "search-robot-complete",
          "riddle9",
          "search",
          SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS);

  /** ECHO's instruction for the first central-computer check. */
  public static final StoryStep CENTRAL_SORT =
      echoStep("central-sort", "riddle10", "sort", SystemRecoveryLearningStep.CORE_SORT);

  /** ECHO's instruction for the central module-count check. */
  public static final StoryStep CENTRAL_COUNT =
      echoStep("central-count", "riddle10", "count", SystemRecoveryLearningStep.CORE_COUNT);

  /** ECHO's instruction for the central map-search check. */
  public static final StoryStep CENTRAL_SEARCH =
      echoStep("central-search", "riddle10", "search", SystemRecoveryLearningStep.CORE_SEARCH);

  /** ECHO's instruction for combining the three central-computer results. */
  public static final StoryStep CENTRAL_META =
      echoStep("central-meta", "riddle10", "meta", SystemRecoveryLearningStep.CORE_META);

  /** AXIOM's irritated response after all central checks have been accepted. */
  public static final StoryStep COMPLETED =
      axiomStep("completed", "riddle10", "complete", SystemRecoveryLearningStep.COMPLETE);

  private final Set<String> shownToPlayer = ConcurrentHashMap.newKeySet();
  private final Queue<PendingDialog> pendingDialogs = new ConcurrentLinkedQueue<>();
  private final LongSupplier clock;
  private final Predicate<StoryStep> isRelevant;

  /** Creates the story controller for one authoritative level instance. */
  public SystemRecoveryStoryDialogs() {
    this(() -> Game.playClock().activeMs(), SystemRecoveryStoryDialogs::matchesActiveStep);
  }

  SystemRecoveryStoryDialogs(LongSupplier clock) {
    this(clock, SystemRecoveryStoryDialogs::matchesActiveStep);
  }

  /**
   * Creates the story controller with the owning level's additional world-state checks.
   *
   * @param isRelevant current Petri and physical-state predicate for one story step
   */
  public SystemRecoveryStoryDialogs(Predicate<StoryStep> isRelevant) {
    this(() -> Game.playClock().activeMs(), isRelevant);
  }

  SystemRecoveryStoryDialogs(LongSupplier clock, Predicate<StoryStep> isRelevant) {
    this.clock = clock;
    this.isRelevant = isRelevant;
  }

  /** Dispatches queued dialogs, including story messages while the level editor is active. */
  public void tick() {
    long now = clock.getAsLong();
    int pendingCount = pendingDialogs.size();
    Set<Integer> blockedPlayers = new HashSet<>();
    for (int index = 0; index < pendingCount; index++) {
      PendingDialog pending = pendingDialogs.poll();
      if (pending == null) return;

      if (!isRelevant.test(pending.step())) {
        pending.afterClose().execute();
        continue;
      }

      // A player's earlier dialog must close before the next one can appear. Other players are
      // independent, and obsolete instructions can be discarded even while a dialog is open.
      if (pending.executeAt() > now
          || blockedPlayers.contains(pending.playerId())
          || hasOpenBlockingDialog(pending.playerId())) {
        pendingDialogs.add(pending);
        blockedPlayers.add(pending.playerId());
        continue;
      }

      String script = pending.step().script();
      DialogFactory.showDialogDialog(script, pending.afterClose(), pending.playerId());
      SystemRecoveryQuestLogUtil.addDialogEntry(
          pending.step().riddleKey(), pending.step().id(), script);
      blockedPlayers.add(pending.playerId());
    }
  }

  /**
   * Queues one instruction for one player after a successful personal action.
   *
   * @param step story step to announce
   * @param playerId player receiving the instruction
   */
  public void announceForPlayer(StoryStep step, int playerId) {
    announceForPlayer(step, playerId, () -> {});
  }

  /**
   * Queues a story step and runs an authoritative action after its dialog closes.
   *
   * @param step story step to announce
   * @param playerId player receiving the instruction
   * @param afterClose action to run after the dialog closes
   */
  public void announceForPlayer(StoryStep step, int playerId, IVoidFunction afterClose) {
    showStepAfterDelay(step, playerId, afterClose);
  }

  /**
   * Queues one shared-room instruction for every currently connected player.
   *
   * @param step story step to announce
   */
  public void announceToAllPlayers(StoryStep step) {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .mapToInt(engine.Entity::id)
        .forEach(playerId -> showStepAfterDelay(step, playerId, () -> {}));
  }

  /**
   * Announces two shared story steps sequentially to each connected player.
   *
   * <p>The second step is queued only after that player's first dialog is closed, preventing
   * simultaneous overlays from obscuring the intended speaker order.
   *
   * @param firstStep story dialog shown first
   * @param secondStep story dialog shown after the first is closed
   */
  public void announceSequenceToAllPlayers(StoryStep firstStep, StoryStep secondStep) {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .mapToInt(engine.Entity::id)
        .forEach(playerId -> announceSequenceForPlayer(firstStep, secondStep, playerId));
  }

  /**
   * Announces two story steps sequentially to one player.
   *
   * @param firstStep story dialog shown first
   * @param secondStep story dialog shown after the first is closed
   * @param playerId player receiving both dialogs
   */
  public void announceSequenceForPlayer(StoryStep firstStep, StoryStep secondStep, int playerId) {
    if (firstStep == null || secondStep == null || playerId < 0) return;
    announceForPlayer(firstStep, playerId, () -> announceForPlayer(secondStep, playerId));
  }

  /**
   * Queues AXIOM's last response and starts the final call after its first dialog closes.
   *
   * @param afterFirstClose action that starts ECHO's final telephone call
   */
  public void announceCompletionToAllPlayers(IVoidFunction afterFirstClose) {
    List<Integer> playerIds =
        Game.levelEntities(Set.of(PlayerComponent.class)).map(engine.Entity::id).toList();
    if (playerIds.isEmpty()) {
      afterFirstClose.execute();
      return;
    }
    AtomicBoolean callbackRun = new AtomicBoolean();
    for (int playerId : playerIds) {
      announceForPlayer(
          COMPLETED,
          playerId,
          () -> {
            if (callbackRun.compareAndSet(false, true)) afterFirstClose.execute();
          });
    }
  }

  private void showStepAfterDelay(StoryStep step, int playerId, IVoidFunction afterClose) {
    if (step == null || playerId < 0) return;
    String key = step.id() + ":" + playerId;
    if (!shownToPlayer.add(key)) return;
    pendingDialogs.add(
        new PendingDialog(
            clock.getAsLong() + STORY_DELAY_MS,
            step,
            playerId,
            afterClose == null ? () -> {} : afterClose));
  }

  /**
   * Checks the authoritative UI registry for a blocking dialog targeting one player.
   *
   * <p>The check uses target IDs instead of client-local visibility. Other players can receive
   * their own story dialogs while this player is reading or using a computer.
   *
   * @param playerId player whose dialog state is checked
   * @return whether this player has a blocking dialog open
   */
  private static boolean hasOpenBlockingDialog(int playerId) {
    return Game.levelEntities()
        .map(entity -> entity.fetch(UIComponent.class).orElse(null))
        .filter(java.util.Objects::nonNull)
        .anyMatch(ui -> ui.willPauseGame() && targetsPlayer(ui, playerId));
  }

  private static boolean targetsPlayer(UIComponent ui, int playerId) {
    int[] targets = ui.targetEntityIds();
    return targets.length == 0 || Arrays.stream(targets).anyMatch(target -> target == playerId);
  }

  private static boolean matchesActiveStep(StoryStep step) {
    return SystemRecoveryProgressNet.activeStep().orElse(null) == step.validAt();
  }

  private static StoryStep axiomStep(
      String id, String riddleKey, String entryKey, SystemRecoveryLearningStep validAt) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.AXIOM, validAt);
  }

  private static StoryStep echoStep(
      String id, String riddleKey, String entryKey, SystemRecoveryLearningStep validAt) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.ECHO, validAt);
  }

  private static StoryStep robotStep(
      String id, String riddleKey, String entryKey, SystemRecoveryLearningStep validAt) {
    return new StoryStep(id, riddleKey, entryKey, id, Speaker.ROBOT, validAt);
  }

  /**
   * One atomic instruction shown after the previous puzzle action.
   *
   * @param id stable story-step identifier
   * @param riddleKey quest-log tab key
   * @param entryKey quest-log entry key
   * @param messageKey story message key
   * @param speaker voice used for the dialog
   * @param validAt active learning step at which the instruction remains relevant
   */
  public record StoryStep(
      String id,
      String riddleKey,
      String entryKey,
      String messageKey,
      Speaker speaker,
      SystemRecoveryLearningStep validAt) {
    /**
     * Creates the keyed dialog script; the target client localizes it when displayed.
     *
     * @return keyed story script
     */
    public String script() {
      return switch (speaker) {
        case AXIOM -> SystemRecoveryText.axiomCall(messageKey);
        case ECHO -> SystemRecoveryText.echoCall(messageKey);
        case ROBOT -> SystemRecoveryText.robotCall(messageKey);
      };
    }

    /**
     * Returns the translation key for the speaker label used by this dialog.
     *
     * @return speaker translation key
     */
    public String speakerKey() {
      return switch (speaker) {
        case AXIOM -> "axiom";
        case ECHO -> "echo";
        case ROBOT -> "search-robot";
      };
    }
  }

  private enum Speaker {
    AXIOM,
    ECHO,
    ROBOT
  }

  private record PendingDialog(
      long executeAt, StoryStep step, int playerId, IVoidFunction afterClose) {}
}
