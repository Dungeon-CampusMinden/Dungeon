package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.canvas.CanvasStore;
import feature.canvas.CanvasUI;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Optional;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.state.VariablePuzzleStage;

/** Server-owned assignments carried by the existing room dialog and snapshot metadata. */
public final class ProgrammingBinding {
  static final String ID = "programming.binding";
  private static BindingState received;

  private ProgrammingBinding() {}

  private enum Type implements DialogType {
    BINDING;

    public String type() {
      return ID;
    }
  }

  static void register() {
    ProgrammingBindingBook.register();
    ProgrammingBindingNode.register();
    DialogFactory.register(
        Type.BINDING,
        context ->
            Game.isHeadless()
                ? new HeadlessDialogGroup()
                : new ProgrammingBindingUI(
                    context.dialogId(), decode(context.require(ID, String.class))));
  }

  static void open(Entity who, ProgrammingGolemRuntime runtime) {
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(Type.BINDING)
                .put(ID, encode(runtime.bindingState()))
                .build(),
            false,
            true,
            false,
            who.id());
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
        .filter(ProgrammingLevel.class::isInstance)
        .map(ProgrammingLevel.class::cast)
        .map(ProgrammingLevel::runtime)
        .map(ProgrammingGolemRuntime::bindingState)
        .or(() -> Optional.ofNullable(received));
  }

  /**
   * Encodes room state without changing the shared snapshot protocol.
   *
   * @param state shared workbench state
   * @return compact metadata value
   */
  public static String encode(BindingState state) {
    StringBuilder values = new StringBuilder();
    for (var property : GolemProperty.values()) {
      values
          .append(
              state.vessels().containsKey(property) ? state.vessels().get(property).name() : "-")
          .append(':')
          .append(
              state.essences().containsKey(property) ? state.essences().get(property).name() : "-")
          .append(',');
    }
    return state.stage()
        + "|"
        + state.propertiesCollected()
        + "|"
        + state.vesselsCollected()
        + "|"
        + values
        + "|"
        + Base64.getEncoder().encodeToString(state.feedback().getBytes(StandardCharsets.UTF_8));
  }

  static BindingState decode(String value) {
    String[] parts = value.split("\\|", -1);
    var vessels = new EnumMap<GolemProperty, SoulVessel>(GolemProperty.class);
    var essences = new EnumMap<GolemProperty, MagicalEssence>(GolemProperty.class);
    String[] entries = parts[3].split(",");
    for (var property : GolemProperty.values()) {
      String[] pair = entries[property.ordinal()].split(":");
      if (!pair[0].equals("-")) vessels.put(property, SoulVessel.valueOf(pair[0]));
      if (!pair[1].equals("-")) essences.put(property, MagicalEssence.valueOf(pair[1]));
    }
    return new BindingState(
        VariablePuzzleStage.valueOf(parts[0]),
        Boolean.parseBoolean(parts[1]),
        Boolean.parseBoolean(parts[2]),
        vessels,
        essences,
        new String(Base64.getDecoder().decode(parts[4]), StandardCharsets.UTF_8));
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
