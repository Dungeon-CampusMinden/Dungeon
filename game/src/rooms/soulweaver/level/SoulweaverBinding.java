package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.canvas.CanvasStore;
import feature.canvas.CanvasUI;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.Optional;
import rooms.soulweaver.modules.variables.BindingState;

/** Server-owned assignments carried by the existing room dialog and snapshot metadata. */
public final class SoulweaverBinding {
  static final String ID = "soulweaver.binding";
  private static BindingState received;

  private SoulweaverBinding() {}

  private enum Type implements DialogType {
    BINDING;

    public String type() {
      return ID;
    }
  }

  static void register() {
    SoulweaverBindingBook.register();
    SoulweaverBindingNode.register();
    DialogFactory.register(
        Type.BINDING,
        context -> {
          context.find(SoulweaverHelp.ID, String.class).ifPresent(SoulweaverHelp::receive);
          return Game.isHeadless()
              ? new HeadlessDialogGroup()
              : new SoulweaverBindingUI(
                  context.dialogId(), decode(context.require(ID, String.class)));
        });
  }

  static void open(Entity who, SoulweaverGolemRuntime runtime) {
    if (Game.hud().blocksGameplayInput(who)) return;
    SoulweaverTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            SoulweaverHelp.context(DialogContext.builder())
                .type(Type.BINDING)
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .put(ID, encode(runtime.bindingState()))
                .build(),
            false,
            true,
            false,
            who.id());
    SoulweaverHelp.callbacks(ui, who);
    ui.registerCallback(CanvasUI.EVENT_CLOSE, ignored -> UIUtils.closeDialog(ui));
    for (String event : new String[] {"vessel", "essence"}) {
      ui.registerCallback(
          event,
          payload -> {
            if (payload instanceof DialogResponseMessage.StringValue value) {
              String[] parts = value.value().split(":", -1);
              if (parts.length == 2)
                runtime.assignBinding(who, parts[0], parts[1], event.equals("vessel"));
            }
          });
    }
    ui.registerCallback(
        "clear",
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue value)
            runtime.clearBinding(who, value.value());
        });
    ui.registerCallback("activate", ignored -> runtime.activate(who));
  }

  static void closeAll() {
    Game.levelEntities()
        .flatMap(e -> e.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType().type().equals(ID))
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
  }

  /**
   * @return authoritative binding on the host, or its latest client snapshot
   */
  public static Optional<BindingState> state() {
    return Game.currentLevel()
        .filter(SoulweaverLevel.class::isInstance)
        .map(SoulweaverLevel.class::cast)
        .map(SoulweaverLevel::runtime)
        .map(SoulweaverGolemRuntime::bindingState)
        .or(() -> Optional.ofNullable(received));
  }

  /**
   * Encodes the workbench state for snapshot metadata and dialog values.
   *
   * @param state shared workbench state
   * @return compact state representation
   */
  public static String encode(BindingState state) {
    return SoulweaverStateCodec.encode(state);
  }

  static BindingState decode(String value) {
    return SoulweaverStateCodec.decode(value, BindingState.class);
  }

  /**
   * Applies the shared workbench snapshot.
   *
   * @param value encoded binding state
   */
  public static void receive(String value) {
    received = decode(value);
  }

  static void reset() {
    received = null;
    CanvasStore.clear(ID);
  }
}
