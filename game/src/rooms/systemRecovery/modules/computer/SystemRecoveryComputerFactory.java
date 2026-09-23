package rooms.systemRecovery.modules.computer;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.computer.content.SearchProgramTab;
import rooms.systemRecovery.modules.computer.content.SortProgramTab;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/** Factory and registration helpers for the System Recovery computer interaction. */
public final class SystemRecoveryComputerFactory {

  /** Dialog attribute indicating that an empty sort stick is currently mounted. */
  public static final String SORT_PROGRAM_INSERTED = "sortProgramInserted";

  /** Dialog attribute indicating that an empty locator chip is currently mounted. */
  public static final String SEARCH_PROGRAM_INSERTED = "searchProgramInserted";

  /** Dialog attribute indicating that the system-core module is currently mounted. */
  public static final String ACCESS_MODULE_INSERTED = "accessModuleInserted";

  /** Dialog attribute indicating that the final system-core input mask is available. */
  public static final String SYSTEM_CORE_META_AVAILABLE = "systemCoreMetaAvailable";

  /** Dialog attribute containing accepted array identifiers and data types for Memory Watch. */
  public static final String MEMORY_ARRAY_ENTRIES = "memoryArrayEntries";

  private SystemRecoveryComputerFactory() {}

  /** Registers the custom System Recovery computer dialog. */
  public static void ensureRegistration() {
    DialogFactory.register(SystemRecoveryDialogTypes.COMPUTER, SystemRecoveryComputerDialog::build);
    TerminalInterpreterSetup.setupPreviewStates();
  }

  /**
   * Adds the computer dialog interaction to a terminal entity.
   *
   * @param terminal terminal entity receiving the interaction
   */
  public static void attachComputerDialog(Entity terminal) {
    terminal.add(
        new InteractionComponent(new Interaction((interacted, who) -> openComputerForPlayer(who))));
  }

  private static void openComputerForPlayer(Entity player) {
    if (!SystemRecoveryLevel.terminalsUnlocked()) {
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("computer.locked-before-call"),
          SystemRecoveryText.key("computer.terminal"),
          player.id());
      return;
    }

    SearchProgramChipItem emptySearchChip = findEmptySearchChip(player);
    if (emptySearchChip != null) {
      showChipChoice(
          player,
          emptySearchChip,
          SystemRecoveryText.key("computer.empty-search-prompt"),
          SystemRecoveryText.key("computer.insert-search"),
          ComputerProgramKind.SEARCH);
      return;
    }

    SortProgramStickItem emptySortStick = findEmptySortStick(player);
    if (emptySortStick != null) {
      showChipChoice(
          player,
          emptySortStick,
          SystemRecoveryText.key("computer.empty-sort-prompt"),
          SystemRecoveryText.key("computer.insert-sort"),
          ComputerProgramKind.SORT);
      return;
    }

    SystemCoreAccessChipItem accessChip = findAccessChip(player);
    if (accessChip != null) {
      showChipChoice(
          player,
          accessChip,
          SystemRecoveryText.key("computer.empty-access-prompt"),
          SystemRecoveryText.key("computer.insert-access"),
          ComputerProgramKind.ACCESS);
      return;
    }

    showComputerDialog(player.id(), ComputerProgramKind.NONE, null);
  }

  private static void showChipChoice(
      Entity player,
      Item chip,
      String prompt,
      String insertLabel,
      ComputerProgramKind programKind) {
    DialogFactory.showMultipleChoiceDialog(
        prompt,
        SystemRecoveryText.key("computer.terminal"),
        List.of(
            ChoiceOption.of(insertLabel, "insert"),
            ChoiceOption.of(SystemRecoveryText.key("computer.without-chip"), "cancel")),
        false,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)
              && "insert".equals(choice)) {
            var step = SystemRecoveryProgressNet.activeStep().orElse(null);
            if (!ComputerProgramRules.canMount(programKind, step)) {
              showActionUnavailable(player.id(), puzzleFor(programKind), "computer.terminal");
              return;
            }
            Item insertedChip = removeMountedChip(player.id(), chip).orElse(null);
            if (insertedChip == null) {
              SystemRecoveryPuzzleEvents.attempt(
                  puzzleFor(programKind), "computer-chip", "insert", choice, false, player);
              DialogUtils.showTextPopup(
                  SystemRecoveryText.key("computer.chip-missing"),
                  SystemRecoveryText.key("computer.terminal"),
                  player.id());
              return;
            }
            if (!recordChipMount(programKind, choice, player)) {
              returnInsertedChip(player.id(), insertedChip);
              DialogUtils.showTextPopup(
                  SystemRecoveryText.key("computer.action-unavailable"),
                  SystemRecoveryText.key("computer.terminal"),
                  player.id());
              return;
            }
            showComputerDialog(player.id(), programKind, insertedChip);
          } else {
            SystemRecoveryPuzzleEvents.attempt(
                puzzleFor(programKind), "computer-chip", "insert", "cancel", false, player);
            showComputerDialog(player.id(), ComputerProgramKind.NONE, null);
          }
        },
        () -> {},
        player.id());
  }

  private static SearchProgramChipItem findEmptySearchChip(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .flatMap(
            inventory ->
                Arrays.stream(inventory.items())
                    .filter(SearchProgramChipItem.class::isInstance)
                    .map(SearchProgramChipItem.class::cast)
                    .filter(chip -> !chip.programmed())
                    .findFirst())
        .orElse(null);
  }

  private static SortProgramStickItem findEmptySortStick(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .flatMap(
            inventory ->
                Arrays.stream(inventory.items())
                    .filter(SortProgramStickItem.class::isInstance)
                    .map(SortProgramStickItem.class::cast)
                    .filter(stick -> !stick.programmed())
                    .findFirst())
        .orElse(null);
  }

  private static SystemCoreAccessChipItem findAccessChip(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .flatMap(
            inventory ->
                Arrays.stream(inventory.items())
                    .filter(SystemCoreAccessChipItem.class::isInstance)
                    .map(SystemCoreAccessChipItem.class::cast)
                    .findFirst())
        .orElse(null);
  }

  private static void showComputerDialog(
      int targetEntityId, ComputerProgramKind programKind, Item insertedChip) {
    ComputerChipSession chipSession = new ComputerChipSession();
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(SystemRecoveryDialogTypes.COMPUTER)
                .put(SORT_PROGRAM_INSERTED, programKind == ComputerProgramKind.SORT)
                .put(SEARCH_PROGRAM_INSERTED, programKind == ComputerProgramKind.SEARCH)
                .put(ACCESS_MODULE_INSERTED, programKind == ComputerProgramKind.ACCESS)
                .put(SYSTEM_CORE_META_AVAILABLE, SystemRecoveryLevel.systemCoreMetaAvailable())
                .put(MEMORY_ARRAY_ENTRIES, SystemRecoveryLevel.memoryWatchArrayEntries())
                .build(),
            targetEntityId);
    ui.registerCallback(
        DialogContextKeys.ON_CLOSE,
        data -> {
          if (insertedChip == null) return;
          chipSession.resolve(() -> returnInsertedChip(targetEntityId, insertedChip));
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_SEND,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String source)) {
            SystemRecoveryLevel.interpretTerminalInput(
                source, targetEntityId, ui.dialogContext().dialogId());
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SYSTEM_CORE_META_SUBMIT,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String payload)) {
            SystemRecoveryLevel.submitSystemCoreMeta(
                payload, targetEntityId, ui.dialogContext().dialogId());
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SORT_PROGRAM_SAVE,
        data -> {
          if (!(data instanceof DialogResponseMessage.StringValue(String source))) return;
          handleProgramUpload(
              ui,
              targetEntityId,
              chipSession,
              programKind,
              ComputerProgramKind.SORT,
              SortProgramTab.KEY,
              source,
              SystemRecoveryPuzzle.BUBBLE_SORT,
              "sort-program",
              "sort",
              "computer.sort-write-error",
              SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION,
              () -> isBubbleSortCondition(source),
              () -> new SortProgramStickItem(true));
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_SAVE,
        data -> {
          if (!(data instanceof DialogResponseMessage.StringValue(String source))) return;
          handleProgramUpload(
              ui,
              targetEntityId,
              chipSession,
              programKind,
              ComputerProgramKind.SEARCH,
              SearchProgramTab.KEY,
              source,
              SystemRecoveryPuzzle.SEARCH_ROBOT,
              "search-program",
              "search",
              "computer.search-write-error",
              SystemRecoveryLearningStep.SEARCH_PROGRAM,
              () -> TerminalInterpreterSetup.matchesSearchRobotProgram(source),
              () -> new SearchProgramChipItem(true));
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SYSTEM_CORE_SCRIPT_RUN,
        data -> {
          if (programKind != ComputerProgramKind.ACCESS) {
            showActionUnavailable(
                targetEntityId, SystemRecoveryPuzzle.SYSTEM_CORE, "computer.access-tab");
            return;
          }
          if (chipSession.resolved()) return;
          chipSession.resolve(
              () -> {
                if (!SystemRecoveryLevel.completeSystemCoreAccess(targetEntityId)) {
                  DialogUtils.showTextPopup(
                      SystemRecoveryText.key("computer.access-unavailable"),
                      SystemRecoveryText.key("computer.access-tab"),
                      targetEntityId);
                  return false;
                }
                DialogUtils.showTextPopup(
                    SystemRecoveryText.key("computer.access-success"),
                    SystemRecoveryText.key("computer.access-tab"),
                    targetEntityId);
                return true;
              });
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.DEBUG_PETRI_NET,
        data -> {
          if (SystemRecovery.debugMode()) {
            SystemRecoveryLevel.showPetriNetDebug(targetEntityId);
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.DEBUG_GIVE_USB,
        data -> {
          if (SystemRecovery.debugMode()) {
            addToInventory(targetEntityId, new SortProgramStickItem());
          }
        });
  }

  /**
   * Executes the authoritative transaction shared by the two programmable USB riddles.
   *
   * <p>The inventory mutation happens before the Petri transition so a failed transition can be
   * rolled back. The mounted session is resolved only after the transaction succeeds, preventing
   * duplicate programmed items when a client repeats the callback.
   *
   * @param ui computer dialog receiving server feedback
   * @param targetEntityId player entity receiving the programmed item
   * @param chipSession mounted-chip session guarding duplicate callbacks
   * @param mountedKind program currently mounted in the computer
   * @param expectedKind program required by this upload callback
   * @param tabKey computer tab receiving feedback
   * @param source submitted program source
   * @param puzzle tracking puzzle owning the upload
   * @param trackingStep tracking step for the upload
   * @param achievementKind achievement category for the upload
   * @param invalidSourceKey translation key for a rejected program
   * @param completedStep Petri learning step completed by a valid upload
   * @param sourceMatches validator for the submitted source
   * @param programmedItemFactory creates the programmed replacement item
   */
  private static void handleProgramUpload(
      UIComponent ui,
      int targetEntityId,
      ComputerChipSession chipSession,
      ComputerProgramKind mountedKind,
      ComputerProgramKind expectedKind,
      String tabKey,
      String source,
      SystemRecoveryPuzzle puzzle,
      String trackingStep,
      String achievementKind,
      String invalidSourceKey,
      SystemRecoveryLearningStep completedStep,
      BooleanSupplier sourceMatches,
      Supplier<Item> programmedItemFactory) {
    String dialogId = ui.dialogContext().dialogId();
    if (chipSession.resolved()
        || mountedKind != expectedKind
        || !ComputerProgramRules.canSave(
            mountedKind, SystemRecoveryProgressNet.activeStep().orElse(null))) {
      SystemRecoveryComputerFeedback.send(
          dialogId, tabKey, source, targetEntityId, "computer.write-unavailable", false);
      return;
    }
    if (!sourceMatches.getAsBoolean()) {
      SystemRecoveryAchievements.chipUploadAttempt(achievementKind, false);
      SystemRecoveryPuzzleEvents.attempt(
          puzzle, trackingStep, "source", source, false, targetEntityId);
      SystemRecoveryComputerFeedback.send(
          dialogId, tabKey, source, targetEntityId, invalidSourceKey, false);
      return;
    }
    chipSession.resolve(
        () -> {
          Item programmedItem = programmedItemFactory.get();
          if (!addToInventory(targetEntityId, programmedItem)) {
            recordUploadFailure(
                puzzle, trackingStep, source, targetEntityId, dialogId, tabKey, false);
            return false;
          }
          if (!SystemRecoveryProgressNet.complete(completedStep)) {
            removeFromInventory(targetEntityId, programmedItem);
            recordUploadFailure(
                puzzle, trackingStep, source, targetEntityId, dialogId, tabKey, false);
            return false;
          }
          SystemRecoveryPuzzleEvents.attempt(
              puzzle, trackingStep, "source", source, true, targetEntityId);
          SystemRecoveryAchievements.chipUploadAttempt(achievementKind, true);
          SystemRecoveryLevel.recordAcceptedSolution(completedStep, source);
          SystemRecoveryComputerFeedback.send(
              dialogId, tabKey, source, targetEntityId, "computer.write-success", true);
          return true;
        });
  }

  private static void recordUploadFailure(
      SystemRecoveryPuzzle puzzle,
      String trackingStep,
      String source,
      int targetEntityId,
      String dialogId,
      String tabKey,
      boolean success) {
    SystemRecoveryPuzzleEvents.attempt(
        puzzle, trackingStep, "source", source, success, targetEntityId);
    SystemRecoveryComputerFeedback.send(
        dialogId, tabKey, source, targetEntityId, "computer.write-unavailable", success);
  }

  private static boolean recordChipMount(
      ComputerProgramKind programKind, String rawChoice, Entity player) {
    String answerKind =
        switch (programKind) {
          case SORT -> "resume-programming";
          case SEARCH -> "mount-for-programming";
          case ACCESS -> "mount-access-module";
          case NONE -> "insert";
        };
    SystemRecoveryPuzzleEvents.attempt(
        puzzleFor(programKind), "computer-chip", answerKind, rawChoice, true, player);
    return programKind != ComputerProgramKind.NONE;
  }

  private static SystemRecoveryPuzzle puzzleFor(ComputerProgramKind programKind) {
    return switch (programKind) {
      case SORT -> SystemRecoveryPuzzle.BUBBLE_SORT;
      case SEARCH -> SystemRecoveryPuzzle.SEARCH_ROBOT;
      case ACCESS, NONE -> SystemRecoveryPuzzle.SYSTEM_CORE;
    };
  }

  static boolean addToInventory(int entityId, Item item) {
    return Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(InventoryComponent.class))
        .map(inventory -> inventory.add(item))
        .orElse(false);
  }

  private static void removeFromInventory(int entityId, Item item) {
    Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(InventoryComponent.class))
        .ifPresent(inventory -> inventory.remove(item));
  }

  static java.util.Optional<Item> removeMountedChip(int entityId, Item chip) {
    return Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(InventoryComponent.class))
        .flatMap(inventory -> inventory.remove(chip));
  }

  /**
   * Returns a removed chip to the player, dropping it at their position if the inventory is full.
   *
   * @param playerId player who owns the computer dialog
   * @param chip chip to return
   * @return whether the chip was returned to inventory or dropped in the world
   */
  static boolean returnInsertedChip(int playerId, Item chip) {
    Entity player = Game.findEntityById(playerId).orElse(null);
    if (player == null) return false;

    boolean added =
        player.fetch(InventoryComponent.class).map(inventory -> inventory.add(chip)).orElse(false);
    if (added) return true;

    return Game.positionOf(player)
        .map(
            position -> {
              Game.add(WorldItemBuilder.buildWorldItem(chip, position));
              return true;
            })
        .orElse(false);
  }

  private static void showActionUnavailable(
      int playerId, SystemRecoveryPuzzle puzzle, String titleKey) {
    SystemRecoveryPuzzleEvents.attempt(
        puzzle, "computer-action", "phase", "invalid", false, playerId);
    DialogUtils.showTextPopup(
        SystemRecoveryText.key("computer.action-unavailable"),
        SystemRecoveryText.key(titleKey),
        playerId);
  }

  private static boolean isBubbleSortCondition(String source) {
    return source.replaceAll("\\s+", "").contains("array[j]>array[j+1]");
  }
}
