package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.Optional;
import java.util.function.Consumer;
import rooms.programming.modules.decisions.DecisionMaze.Values;
import tools.jackson.databind.json.JsonMapper;

/** Typed, revisioned Act IV state shared by the mounted player and the world. */
public final class ProgrammingDecisions {
  public static final String ID = "programming.decisions";

  /**
   * Public view of the server-owned run.
   *
   * @param revision monotonically increasing state revision
   * @param golemId entity followed by the camera
   * @param driver mounted player controlling Nox, or -1 when free
   * @param junction zero-based current junction, six when complete
   * @param failures completed return trips
   * @param values actual values carried by Nox
   * @param active whether Act III has unlocked this act
   * @param moving whether a physical route is running
   * @param blocked whether Weiter is needed to resume
   * @param completed whether Nox reached the Herzfeuer
   * @param feedback visible event or movement status
   */
  public record State(
      int revision,
      int golemId,
      int driver,
      int junction,
      int failures,
      Values values,
      boolean active,
      boolean moving,
      boolean blocked,
      boolean completed,
      String feedback) {}

  /**
   * A direction or continuation request, never a client-supplied result.
   *
   * @param revision expected authoritative revision
   * @param operation LEFT, RIGHT or RESUME
   */
  public record Intent(int revision, String operation) {}

  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static State received;

  private enum Type implements DialogType {
    DECISIONS;

    public String type() {
      return ID;
    }
  }

  private ProgrammingDecisions() {}

  /** Registers the same typed dialog factory on server and client. */
  public static void register() {
    DialogFactory.register(
        Type.DECISIONS,
        context ->
            Game.isHeadless()
                ? new HeadlessDialogGroup()
                : new ProgrammingDecisionUI(
                    context.dialogId(),
                    decode(context.require(ID, String.class)),
                    context.require("viewer", Integer.class)));
  }

  static void open(Entity who, State state, Consumer<Intent> callback) {
    if (Game.hud().blocksGameplayInput(who)) return;
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(Type.DECISIONS)
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .put(ID, encode(state))
                .put("viewer", who.id())
                .build(),
            false,
            false,
            false,
            who.id());
    ui.registerCallback(
        "intent",
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue value)) return;
          try {
            callback.accept(JSON.readValue(value.value(), Intent.class));
          } catch (tools.jackson.core.JacksonException ignored) {
          }
        });
  }

  /**
   * @return the latest authoritative or received snapshot
   */
  public static Optional<State> state() {
    return Optional.ofNullable(received);
  }

  static void publish(State state) {
    received = state;
    ProgrammingRiderSystem.refresh();
  }

  /**
   * @param state snapshot to transmit
   * @return JSON dialog and world metadata payload
   */
  public static String encode(State state) {
    return JSON.writeValueAsString(state);
  }

  static State decode(String text) {
    return JSON.readValue(text, State.class);
  }

  static String intent(Intent intent) {
    return JSON.writeValueAsString(intent);
  }

  /**
   * @param text authoritative JSON snapshot received by the client
   */
  public static void receive(String text) {
    received = decode(text);
    ProgrammingRiderSystem.refresh();
  }

  static void reset() {
    received = null;
    ProgrammingRiderSystem.refresh();
  }
}
