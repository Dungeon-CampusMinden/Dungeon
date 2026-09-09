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
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Factory and registration helpers for the System Recovery computer interaction. */
public final class SystemRecoveryComputerFactory {

  /** Dialog attribute indicating that the empty sort stick is currently mounted. */
  public static final String SORT_PROGRAM_INSERTED = "sortProgramInserted";

  private SystemRecoveryComputerFactory() {}

  /** Registers the custom System Recovery computer dialog. */
  public static void ensureRegistration() {
    DialogFactory.register(SystemRecoveryDialogTypes.COMPUTER, SystemRecoveryComputerDialog::build);
    TerminalInterpreterSetup.setupPreviewStates();
  }

  /**
   * Adds the computer dialog interaction to a terminal entity.
   *
   * @param terminal entity that should open the computer UI
   */
  public static void attachComputerDialog(Entity terminal) {
    terminal.add(
        new InteractionComponent(new Interaction((interacted, who) -> openComputerForPlayer(who))));
  }

  private static void openComputerForPlayer(Entity player) {
    SortProgramStickItem emptyStick = findEmptyStick(player);
    if (emptyStick == null) {
      showComputerDialog(player.id(), false);
      return;
    }

    DialogFactory.showMultipleChoiceDialog(
        "Ein leerer Sortierchip wurde erkannt. Soll er in den Rechner eingesetzt werden?",
        "Rechner",
        ChoiceOption.ofList("Sortierchip einlegen", "Ohne Chip öffnen"),
        false,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)
              && "Sortierchip einlegen".equals(choice)) {
            player.fetch(InventoryComponent.class).ifPresent(inv -> inv.remove(emptyStick));
            showComputerDialog(player.id(), true);
          } else {
            showComputerDialog(player.id(), false);
          }
        },
        () -> {},
        player.id());
  }

  private static SortProgramStickItem findEmptyStick(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .flatMap(
            inventory ->
                java.util.Arrays.stream(inventory.items())
                    .filter(SortProgramStickItem.class::isInstance)
                    .map(SortProgramStickItem.class::cast)
                    .filter(stick -> !stick.programmed())
                    .findFirst())
        .orElse(null);
  }

  private static void showComputerDialog(int targetEntityId, boolean sortProgramInserted) {
    final boolean[] programReturned = {false};
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(SystemRecoveryDialogTypes.COMPUTER)
                .put(SORT_PROGRAM_INSERTED, sortProgramInserted)
                .build(),
            targetEntityId);
    ui.registerCallback(
        DialogContextKeys.ON_CLOSE,
        data -> {
          if (sortProgramInserted && !programReturned[0]) {
            Game.findEntityById(targetEntityId)
                .flatMap(entity -> entity.fetch(InventoryComponent.class))
                .ifPresent(inventory -> inventory.add(new SortProgramStickItem()));
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_SEND,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String source)) {
            TerminalInterpreter.instance().interpret(source);
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.SORT_PROGRAM_SAVE,
        data -> {
          if (!(data instanceof DialogResponseMessage.StringValue(String source))) {
            return;
          }
          if (!isBubbleSortCondition(source)) {
            DialogUtils.showTextPopup(
                "Fehler: Die Vergleichsbedingung ist nicht korrekt.",
                "Sortierchip",
                targetEntityId);
            return;
          }
          Game.findEntityById(targetEntityId)
              .flatMap(entity -> entity.fetch(InventoryComponent.class))
              .ifPresent(inventory -> inventory.add(new SortProgramStickItem(true)));
          programReturned[0] = true;
          DialogUtils.showTextPopup(
              "Der Bubble-Sort-Vergleich wurde auf den Sortierchip geladen.",
              "Sortierchip",
              targetEntityId);
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_NEXT_STEP,
        data -> {
          if (SystemRecovery.DEBUG_MODE) {
            TerminalInterpreter.instance().advanceCurrentStateForDebug();
          }
        });
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.DEBUG_SPAWN_SORT_USB,
        data -> {
          if (!SystemRecovery.DEBUG_MODE) {
            return;
          }
          Game.findEntityById(targetEntityId)
              .flatMap(entity -> entity.fetch(InventoryComponent.class))
              .ifPresent(
                  inventory -> {
                    if (inventory.add(new SortProgramStickItem(true))) {
                      DialogUtils.showTextPopup(
                          "Ein programmierter Sortierchip wurde ins Inventar gelegt.",
                          "Debug",
                          targetEntityId);
                    } else {
                      DialogUtils.showTextPopup(
                          "Debug: Das Inventar ist voll.", "Debug", targetEntityId);
                    }
                  });
        });
  }

  private static boolean isBubbleSortCondition(String source) {
    return source.replaceAll("\\s+", "").contains("array[j]>array[j+1]");
  }
}
