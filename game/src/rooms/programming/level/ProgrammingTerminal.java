package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasStore;
import feature.canvas.CanvasUI;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import rooms.programming.modules.loops.TerminalState;

/** The shared loop terminal, with server state and a locally arranged canvas. */
public final class ProgrammingTerminal {
  static final String ID = "programming.loop-terminal";
  static final String STATE = "programming.terminal";
  static final Color INK = Color.valueOf("101820");
  static final Color PAPER = Color.valueOf("263440");
  static final Color ACCENT = Color.valueOf("eab66c");
  private static TerminalState received;

  private ProgrammingTerminal() {}

  enum Type implements DialogType {
    TERMINAL,
    OBSERVATION;

    public String type() {
      return "programming." + name().toLowerCase(Locale.ROOT);
    }
  }

  /** Registers room dialog and node types in each runtime. */
  public static void register() {
    ProgrammingTerminalNode.register();
    DialogFactory.register(
        Type.TERMINAL,
        context -> {
          if (Game.isHeadless()) return new HeadlessDialogGroup();
          TerminalState initial = decode(context.require(STATE, String.class));
          return new ProgrammingTerminalUI(context.dialogId(), initial);
        });
    ProgrammingObservation.register();
  }

  static void open(Entity who, ProgrammingGolemRuntime runtime) {
    stopWalking(who);
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(Type.TERMINAL)
                .put(STATE, encode(runtime.terminalState()))
                .build(),
            true,
            true,
            false,
            who.id());
    ui.registerCallback(CanvasUI.EVENT_CLOSE, payload -> UIUtils.closeDialog(ui));
    ui.registerCallback(
        "execute",
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue value)
            runtime.executeRune(value.value(), who);
        });
  }

  static void stopWalking(Entity who) {
    who.fetch(engine.components.VelocityComponent.class)
        .ifPresent(
            velocity -> {
              velocity.removeForce(feature.entities.HeroController.MOVEMENT_ID);
              velocity.currentVelocity(engine.utils.Vector2.ZERO);
            });
  }

  /**
   * Returns authoritative room state on the server, or the latest received client state.
   *
   * @return the current room state, if available
   */
  public static Optional<TerminalState> state() {
    return Game.currentLevel()
        .filter(ProgrammingLevel.class::isInstance)
        .map(ProgrammingLevel.class::cast)
        .map(ProgrammingLevel::runtime)
        .map(ProgrammingGolemRuntime::terminalState)
        .or(() -> Optional.ofNullable(received));
  }

  /**
   * Applies the small room state carried by golem snapshot metadata.
   *
   * @param value the encoded terminal state
   */
  public static void receive(String value) {
    received = decode(value);
  }

  static void reset() {
    received = null;
    CanvasStore.clear(ID);
  }

  /**
   * Compact string encoding using existing metadata and dialog string values.
   *
   * @param s the terminal state to encode
   * @return the encoded terminal state
   */
  public static String encode(TerminalState s) {
    return String.join(
        "|",
        String.join(",", s.collectedRunes()),
        "" + s.checkpoint(),
        "" + s.golemId(),
        "" + s.cellX(),
        "" + s.cellY(),
        s.facing(),
        "" + s.busy(),
        "" + s.observationReady(),
        s.activeRune(),
        Base64.getEncoder().encodeToString(s.status().getBytes(StandardCharsets.UTF_8)),
        "" + s.completed(),
        "" + s.finished());
  }

  static TerminalState decode(String value) {
    String[] p = value.split("\\|", -1);
    return new TerminalState(
        p[0].isEmpty() ? List.of() : List.of(p[0].split(",")),
        Integer.parseInt(p[1]),
        Integer.parseInt(p[2]),
        Integer.parseInt(p[3]),
        Integer.parseInt(p[4]),
        p[5],
        Boolean.parseBoolean(p[6]),
        Boolean.parseBoolean(p[7]),
        p[8],
        new String(Base64.getDecoder().decode(p[9]), StandardCharsets.UTF_8),
        Integer.parseInt(p[10]),
        Boolean.parseBoolean(p[11]));
  }

  static List<CanvasNode> nodes(TerminalState state) {
    List<CanvasNode> result = new ArrayList<>();
    result.add(new ProgrammingTerminalNode("map", "map").position(0, 0));
    result.add(new ProgrammingTerminalNode("executor", "executor").position(420, 375));
    result.add(new ProgrammingTerminalNode("commands", "help").position(800, 300));
    for (int i = 0; i < state.collectedRunes().size(); i++) {
      result.add(card(state.collectedRunes().get(i), i));
    }
    return result;
  }

  static ProgrammingTerminalNode card(String id, int index) {
    ProgrammingTerminalNode node = new ProgrammingTerminalNode(id, "rune");
    node.position(420 + index / 12 * 380, -10 - index % 12 * 36);
    return node;
  }
}
