package rooms.systemRecovery.modules.computer;

import engine.Entity;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Factory and registration helpers for the System Recovery computer interaction. */
public final class SystemRecoveryComputerFactory {

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
        new InteractionComponent(
            new Interaction((interacted, who) -> showComputerDialog(who.id()))));
  }

  private static void showComputerDialog(int targetEntityId) {
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(SystemRecoveryDialogTypes.COMPUTER).build(),
            targetEntityId);
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> {});
    ui.registerCallback(
        SystemRecoveryComputerCallbacks.TERMINAL_SEND,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String source)) {
            TerminalInterpreter.instance().interpret(source);
          }
        });
  }
}
