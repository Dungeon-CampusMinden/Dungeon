package rooms.systemRecovery.modules.computer;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
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
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
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

  private SystemRecoveryComputerFactory() {}

  /** Registers the custom System Recovery computer dialog. */
  public static void ensureRegistration() {
    DialogFactory.register(SystemRecoveryDialogTypes.COMPUTER, SystemRecoveryComputerDialog::build);
    TerminalInterpreterSetup.setupPreviewStates();
  }

  /** Adds the computer dialog interaction to a terminal entity. */
  public static void attachComputerDialog(Entity terminal) {
    terminal.add(
        new InteractionComponent(new Interaction((interacted, who) -> openComputerForPlayer(who))));
  }

  private static void openComputerForPlayer(Entity player) {
    if (!SystemRecoveryLevel.terminalsUnlocked()) {
      DialogUtils.showTextPopup(
          SystemRecoveryText.text("computer.locked-before-call"),
          SystemRecoveryText.text("computer.terminal"),
          player.id());
      return;
    }

    SearchProgramChipItem emptySearchChip = findEmptySearchChip(player);
    if (emptySearchChip != null) {
      showChipChoice(
          player,
          emptySearchChip,
          SystemRecoveryText.text("computer.empty-search-prompt"),
          SystemRecoveryText.text("computer.insert-search"),
          ProgramKind.SEARCH);
      return;
    }

    SortProgramStickItem emptySortStick = findEmptySortStick(player);
    if (emptySortStick != null) {
      showChipChoice(
          player,
          emptySortStick,
          SystemRecoveryText.text("computer.empty-sort-prompt"),
          SystemRecoveryText.text("computer.insert-sort"),
          ProgramKind.SORT);
      return;
    }

    SystemCoreAccessChipItem accessChip = findAccessChip(player);
    if (accessChip != null) {
      showChipChoice(
          player,
          accessChip,
          SystemRecoveryText.text("computer.empty-access-prompt"),
          SystemRecoveryText.text("computer.insert-access"),
          ProgramKind.ACCESS);
      return;
    }

    showComputerDialog(player.id(), ProgramKind.NONE);
  }

  private static void showChipChoice(
      Entity player, Item chip, String prompt, String insertLabel, ProgramKind programKind) {
    DialogFactory.showMultipleChoiceDialog(
        prompt,
        SystemRecoveryText.text("computer.terminal"),
        List.of(
            ChoiceOption.of(insertLabel, "insert"),
            ChoiceOption.of(SystemRecoveryText.text("computer.without-chip"), "cancel")),
        false,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)
              && "insert".equals(choice)) {
            SystemRecoveryPuzzleEvents.attempt(
                puzzleFor(programKind), "computer-chip", "insert", choice, true, player);
            player.fetch(InventoryComponent.class).ifPresent(inv -> inv.remove(chip));
            showComputerDialog(player.id(), programKind);
          } else {
            SystemRecoveryPuzzleEvents.attempt(
                puzzleFor(programKind), "computer-chip", "insert", "cancel", false, player);
            showComputerDialog(player.id(), ProgramKind.NONE);
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

  private static void showComputerDialog(int targetEntityId, ProgramKind programKind) {
    final boolean[] programReturned = {false};
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(SystemRecoveryDialogTypes.COMPUTER)
                .put(SORT_PROGRAM_INSERTED, programKind == ProgramKind.SORT)
                .put(SEARCH_PROGRAM_INSERTED, programKind == ProgramKind.SEARCH)
                .put(ACCESS_MODULE_INSERTED, programKind == ProgramKind.ACCESS)
                .build(),
            targetEntityId);
    ui.registerCallback(
        DialogContextKeys.ON_CLOSE,
        data -> {
          if (programReturned[0]) return;
          Game.findEntityById(targetEntityId)
              .flatMap(entity -> entity.fetch(InventoryComponent.class))
              .ifPresent(
                  inventory -> {
                    switch (programKind) {
                      case SORT -> inventory.add(new SortProgramStickItem());
                      case SEARCH -> inventory.add(new SearchProgramChipItem());
                      case ACCESS -> inventory.add(new SystemCoreAccessChipItem());
                      case NONE -> {}
                    }
                  });
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_SEND,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String source)) {
            SystemRecoveryLevel.interpretTerminalInput(source, targetEntityId);
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SORT_PROGRAM_SAVE,
        data -> {
          if (!(data instanceof DialogResponseMessage.StringValue(String source))) return;
          if (programReturned[0]) return;
          if (!isBubbleSortCondition(source)) {
            SystemRecoveryPuzzleEvents.attempt(
                SystemRecoveryPuzzle.BUBBLE_SORT,
                "sort-program",
                "source",
                source,
                false,
                targetEntityId);
            DialogUtils.showTextPopup(
                SystemRecoveryText.text("computer.sort-invalid"),
                SystemRecoveryText.text("computer.sort-tab"),
                targetEntityId);
            return;
          }
          SystemRecoveryPuzzleEvents.attempt(
              SystemRecoveryPuzzle.BUBBLE_SORT,
              "sort-program",
              "source",
              source,
              true,
              targetEntityId);
          addToInventory(targetEntityId, new SortProgramStickItem(true));
          programReturned[0] = true;
          DialogUtils.showTextPopup(
              SystemRecoveryText.text("computer.sort-saved"),
              SystemRecoveryText.text("computer.sort-tab"),
              targetEntityId);
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_SAVE,
        data -> {
          if (!(data instanceof DialogResponseMessage.StringValue(String source))) return;
          if (programReturned[0]) return;
          if (!TerminalInterpreterSetup.matchesSearchRobotProgram(source)) {
            SystemRecoveryPuzzleEvents.attempt(
                SystemRecoveryPuzzle.SEARCH_ROBOT,
                "search-program",
                "source",
                source,
                false,
                targetEntityId);
            DialogUtils.showTextPopup(
                SystemRecoveryText.text("computer.search-invalid"),
                SystemRecoveryText.text("computer.search-tab"),
                targetEntityId);
            return;
          }
          SystemRecoveryPuzzleEvents.attempt(
              SystemRecoveryPuzzle.SEARCH_ROBOT,
              "search-program",
              "source",
              source,
              true,
              targetEntityId);
          addToInventory(targetEntityId, new SearchProgramChipItem(true));
          programReturned[0] = true;
          DialogUtils.showTextPopup(
              SystemRecoveryText.text("computer.search-saved"),
              SystemRecoveryText.text("computer.search-tab"),
              targetEntityId);
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SYSTEM_CORE_SCRIPT_RUN,
        data -> {
          if (programKind != ProgramKind.ACCESS) return;
          programReturned[0] = true;
          SystemRecoveryPuzzleEvents.attempt(
              SystemRecoveryPuzzle.SYSTEM_CORE,
              "access-script",
              "execute",
              "run",
              true,
              targetEntityId);
          SystemRecoveryLevel.completeSystemCoreAccess(targetEntityId);
          DialogUtils.showTextPopup(
              SystemRecoveryText.text("computer.access-success"),
              SystemRecoveryText.text("computer.access-tab"),
              targetEntityId);
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_NEXT_STEP,
        data -> {
          if (SystemRecovery.DEBUG_MODE) {
            SystemRecoveryLevel.advanceTerminalStateForDebug(targetEntityId);
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.DEBUG_SPAWN_SORT_USB,
        data -> {
          if (!SystemRecovery.DEBUG_MODE) return;
          Game.findEntityById(targetEntityId)
              .flatMap(entity -> entity.fetch(InventoryComponent.class))
              .ifPresent(
                  inventory -> {
                    if (inventory.add(new SortProgramStickItem(true))) {
                      DialogUtils.showTextPopup(
                          SystemRecoveryText.text("computer.debug-sort"),
                          SystemRecoveryText.text("computer.debug-title"),
                          targetEntityId);
                    } else {
                      DialogUtils.showTextPopup(
                          SystemRecoveryText.text("computer.debug-full"),
                          SystemRecoveryText.text("computer.debug-title"),
                          targetEntityId);
                    }
                  });
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.DEBUG_PETRI_NET,
        data -> {
          if (SystemRecovery.DEBUG_MODE) {
            SystemRecoveryLevel.showPetriNetDebug(targetEntityId);
          }
        });
  }

  private static SystemRecoveryPuzzle puzzleFor(ProgramKind programKind) {
    return switch (programKind) {
      case SORT -> SystemRecoveryPuzzle.BUBBLE_SORT;
      case SEARCH -> SystemRecoveryPuzzle.SEARCH_ROBOT;
      case ACCESS, NONE -> SystemRecoveryPuzzle.SYSTEM_CORE;
    };
  }

  private static void addToInventory(int entityId, Item item) {
    Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(InventoryComponent.class))
        .ifPresent(inventory -> inventory.add(item));
  }

  private static boolean isBubbleSortCondition(String source) {
    return source.replaceAll("\\s+", "").contains("array[j]>array[j+1]");
  }

  private enum ProgramKind {
    NONE,
    SORT,
    SEARCH,
    ACCESS
  }
}
