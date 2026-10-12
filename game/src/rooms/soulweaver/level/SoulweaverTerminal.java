package rooms.soulweaver.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasStore;
import feature.canvas.CanvasUI;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import rooms.soulweaver.modules.loops.LoopMaze;
import rooms.soulweaver.modules.loops.TerminalState;

/** The shared loop terminal, with server state and a locally arranged canvas. */
public final class SoulweaverTerminal {
  static final String ID = "soulweaver.loop-terminal";
  static final String STATE = "soulweaver.terminal";
  static final Color INK = SoulweaverUI.INK;
  static final Color PAPER = SoulweaverUI.SURFACE;
  static final Color ACCENT = SoulweaverUI.GOLD;

  private static TerminalState received;

  private SoulweaverTerminal() {}

  enum Type implements DialogType {
    TERMINAL,
    OBSERVATION;

    public String type() {
      return "soulweaver." + name().toLowerCase(Locale.ROOT);
    }
  }

  /** Registers room dialog and node types in each runtime. */
  public static void register() {
    SoulweaverBinding.register();
    SoulweaverTerminalNode.register();
    DialogFactory.register(
        Type.TERMINAL,
        context -> {
          context.find(SoulweaverHelp.ID, String.class).ifPresent(SoulweaverHelp::receive);
          if (Game.isHeadless()) return new HeadlessDialogGroup();
          TerminalState initial = decode(context.require(STATE, String.class));
          return new SoulweaverTerminalUI(context.dialogId(), initial);
        });
    SoulweaverObservation.register();
  }

  static void open(Entity who, SoulweaverGolemRuntime runtime) {
    if (Game.hud().blocksGameplayInput(who)) return;
    stopWalking(who);
    UIComponent ui =
        DialogFactory.show(
            SoulweaverHelp.context(DialogContext.builder())
                .type(Type.TERMINAL)
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .put(STATE, encode(runtime.terminalState()))
                .build(),
            false,
            true,
            false,
            who.id());
    SoulweaverHelp.callbacks(ui, who);
    ui.registerCallback(CanvasUI.EVENT_CLOSE, payload -> UIUtils.closeDialog(ui));
    ui.registerCallback(
        "execute",
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue value)
            runtime.executeRune(value.value(), who);
        });
    ui.registerCallback(
        "removeRune",
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue value)
            runtime.removeRune(value.value(), who);
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
        .filter(SoulweaverLevel.class::isInstance)
        .map(SoulweaverLevel.class::cast)
        .map(SoulweaverLevel::runtime)
        .map(SoulweaverGolemRuntime::terminalState)
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

  /**
   * Uses the same visible entity position as the observation camera, including network smoothing.
   *
   * @param state latest authoritative terminal state
   * @return continuous grid position, falling back to the snapshot before the entity is available
   */
  static Point mapPosition(TerminalState state) {
    return Game.currentLevel()
        .map(level -> level.namedPoints().get("maze-origin"))
        .flatMap(
            origin ->
                Game.findEntityById(state.golemId())
                    .flatMap(entity -> entity.fetch(PositionComponent.class))
                    .map(
                        position ->
                            new Point(
                                (position.position().x() - origin.x()) / LoopMaze.CELL_WIDTH,
                                (position.position().y() - origin.y()) / LoopMaze.CELL_HEIGHT)))
        .orElseGet(() -> new Point(state.cellX(), state.cellY()));
  }

  static void reset() {
    SoulweaverHelp.reset();
    SoulweaverBinding.reset();
    received = null;
    CanvasStore.clear(ID);
  }

  /**
   * Encodes the terminal state for snapshot metadata and dialog values.
   *
   * @param state the terminal state
   * @return compact state representation
   */
  public static String encode(TerminalState state) {
    return SoulweaverStateCodec.encode(state);
  }

  static TerminalState decode(String value) {
    return SoulweaverStateCodec.decode(value, TerminalState.class);
  }

  static List<CanvasNode> nodes(TerminalState state) {
    List<CanvasNode> result = new ArrayList<>();
    result.add(new SoulweaverTerminalNode("map", "map").position(0, 0));
    result.add(new SoulweaverTerminalNode("executor", "executor").position(428, 412));
    result.add(new SoulweaverTerminalNode("feedback", "status").position(404, 388).z(-1));
    result.add(new SoulweaverTerminalNode("commands", "help").position(404, -222));
    for (int i = 0; i < state.collectedRunes().size(); i++) {
      result.add(card(state.collectedRunes().get(i), i));
    }
    return result;
  }

  static SoulweaverTerminalNode card(String id, int index) {
    SoulweaverTerminalNode node = new SoulweaverTerminalNode(id, "rune");
    node.position(404 + index % 6 * 80, 300 - index / 6 * 80);
    return node;
  }
}
