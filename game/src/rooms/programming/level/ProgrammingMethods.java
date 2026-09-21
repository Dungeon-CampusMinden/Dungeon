package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.canvas.CanvasStore;
import feature.canvas.CanvasUI;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.Optional;
import java.util.function.Consumer;
import rooms.programming.modules.methods.MethodsWorkshop;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Typed room dialog bridge. Only the server callback applies a learner's intent. */
public final class ProgrammingMethods {
  public static final String ID = "programming.methods";
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static MethodsWorkshop.State received;

  private ProgrammingMethods() {}

  private enum Type implements DialogType {
    METHODS;

    public String type() {
      return ID;
    }
  }

  /** Registers the methods dialog and canvas node in each runtime. */
  public static void register() {
    ProgrammingMethodsNode.register();
    DialogFactory.register(
        Type.METHODS,
        context -> {
          context.find(ProgrammingHelp.ID, String.class).ifPresent(ProgrammingHelp::receive);
          return Game.isHeadless()
              ? new HeadlessDialogGroup()
              : new ProgrammingMethodsUI(
                  context.dialogId(),
                  decode(context.require(ID, String.class)),
                  context.require("editorViewer", Integer.class));
        });
  }

  /**
   * Opens the shared editor and routes typed intents to its authoritative runtime.
   *
   * @param who player opening the dialog
   * @param state initial workshop snapshot
   * @param callback server handler for that player's intents
   */
  static void open(
      Entity who, MethodsWorkshop.State state, Consumer<MethodsWorkshop.Intent> callback) {
    if (Game.hud().blocksGameplayInput(who)) return;
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            ProgrammingHelp.context(DialogContext.builder())
                .type(Type.METHODS)
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .put(ID, encode(state))
                .put("editorViewer", who.id())
                .build(),
            false,
            false,
            true,
            who.id());
    ProgrammingHelp.callbacks(ui, who);
    ui.registerCallback(
        "intent",
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue value)) return;
          MethodsWorkshop.Intent intent;
          try {
            intent = JSON.readValue(value.value(), MethodsWorkshop.Intent.class);
          } catch (JacksonException ignored) {
            return;
          }
          callback.accept(intent);
        });
    ui.registerCallback(
        CanvasUI.EVENT_CLOSE,
        ignored -> {
          state()
              .ifPresent(
                  current ->
                      callback.accept(
                          new MethodsWorkshop.Intent(
                              current.revision(),
                              current.stage(),
                              MethodsWorkshop.Operation.RELEASE,
                              "")));
          UIUtils.closeDialog(ui, true);
        });
  }

  /**
   * Returns the latest published or received workshop snapshot.
   *
   * @return current snapshot, if available
   */
  public static Optional<MethodsWorkshop.State> state() {
    return Optional.ofNullable(received);
  }

  /**
   * Exposes server state to dialogs and world synchronization.
   *
   * @param state authoritative workshop snapshot
   */
  static void publish(MethodsWorkshop.State state) {
    received = state;
  }

  /**
   * Encodes a snapshot for dialogs and world metadata.
   *
   * @param state workshop snapshot to send
   * @return JSON representation
   */
  public static String encode(MethodsWorkshop.State state) {
    return JSON.writeValueAsString(state);
  }

  static MethodsWorkshop.State decode(String value) {
    return JSON.readValue(value, MethodsWorkshop.State.class);
  }

  static String encodeIntent(MethodsWorkshop.Intent intent) {
    return JSON.writeValueAsString(intent);
  }

  /**
   * Applies a client snapshot.
   *
   * @param value encoded server state
   */
  public static void receive(String value) {
    received = decode(value);
  }

  /** Clears the preceding room's snapshot and local canvas state. */
  static void reset() {
    received = null;
    CanvasStore.clear(ID);
  }
}
