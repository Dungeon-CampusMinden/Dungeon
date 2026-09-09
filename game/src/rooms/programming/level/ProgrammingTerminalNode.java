package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import java.util.Optional;
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopRune;
import rooms.programming.modules.loops.TerminalState;

/** Room-specific map, program cards and the single execution slot. */
final class ProgrammingTerminalNode extends CanvasNode {
  private static final String TYPE = "programming.terminal-node";
  private static final int MAP_INSET = 24;
  private static final int CELL_STEP = 48;
  private static final int CELL_SIZE = 44;
  private String kind;
  private TerminalState state;
  private Label heading;
  private Label text;
  private boolean hover;
  private boolean pending;
  private float pendingTime;
  private TextureRegion head;
  private TextureRegion runeImage;
  private boolean dragging;
  private float dragStartX;
  private float dragStartY;
  private boolean slotted;
  private float homeX;
  private float homeY;

  static void register() {
    if (!CanvasNodeType.isRegistered(TYPE))
      CanvasNodeType.register(
          TYPE, s -> new ProgrammingTerminalNode(s.id(), s.prop("kind", "rune")));
  }

  ProgrammingTerminalNode(String id, String kind) {
    super(
        id,
        kind.equals("map") ? 380 : kind.equals("rune") ? 64 : kind.equals("executor") ? 88 : 464,
        kind.equals("map")
            ? 556
            : kind.equals("rune")
                ? 64
                : kind.equals("executor") ? 88 : kind.equals("help") ? 210 : 168);
    this.kind = kind;
    deletable(false);
    movable(kind.equals("rune"));
    if (kind.equals("status")) setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put("kind", kind);
    if (kind.equals("rune")) {
      props.put("slotted", slotted);
      props.put("homeX", homeX);
      props.put("homeY", homeY);
    }
  }

  @Override
  protected void readProps(NodeState value) {
    kind = value.prop("kind", "rune");
    slotted = value.boolProp("slotted", false);
    homeX = value.floatProp("homeX", x());
    homeY = value.floatProp("homeY", y());
  }

  void update(TerminalState value) {
    state = value;
    if (kind.equals("rune")) {
      boolean inserted = value.activeRune().equals(id());
      movable(!(inserted && value.busy()));
      if (inserted) {
        if (!slotted) {
          homeX = dragging ? dragStartX : x();
          homeY = dragging ? dragStartY : y();
          slotted = true;
        }
        if (value.busy()) dragging = false;
        if (!dragging && canvas() != null)
          canvas()
              .nodeById("executor")
              .ifPresent(
                  slot -> {
                    position(slot.centerX() - width() / 2, slot.centerY() - height() / 2);
                    if (z() <= slot.z()) z(slot.z() + 1);
                  });
      } else if (slotted) {
        slotted = false;
        if (!dragging) position(homeX, homeY);
      }
    }
    if (value.busy()) pending = false;
    if (text != null) text.setText(caption());
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (pending && (pendingTime += delta) > 2) {
      pending = false;
      pendingTime = 0;
    }
  }

  private String caption() {
    if (kind.equals("help"))
      return "schritt(): ein Feld vorwärts\nspringen(): über eine Grube, zwei Felder\nangreifen(): Gegner direkt voraus\nbodenVoraus(): Boden, auch mit Gegner\namWegzeichen(): aktuelles Ziel erreicht\nDrehen: eine Vierteldrehung";
    if (kind.equals("status")) {
      return state == null ? "Eine Rune hier ablegen" : state.status();
    }
    return "";
  }

  @Override
  protected void buildContent() {
    if (kind.equals("rune") || kind.equals("executor")) return;
    heading =
        Scene2dElementFactory.createLabel(
            switch (kind) {
              case "map" -> "Keller";
              case "status" -> "Executor";
              default -> "Befehle";
            },
            22,
            Color.valueOf("f1eadc"));
    heading.setAlignment(Align.left);
    addActor(heading);
    if (!kind.equals("map")) {
      text =
          Scene2dElementFactory.createLabel(
              caption(), kind.equals("help") ? 16 : 18, Color.valueOf("f1eadc"));
      text.setAlignment(kind.equals("status") ? Align.left : Align.topLeft);
      text.setWrap(true);
      addActor(text);
    }
    if (kind.equals("map")) {
      head =
          new TextureRegion(
              TextureMap.instance()
                  .textureAt(
                      new SimpleIPath("character/monster/programming_golem/programming_golem.png")),
              24,
              5,
              16,
              19);
      for (int i = 0; i < LoopMaze.checkpoints().size(); i++) {
        var cell = LoopMaze.checkpoints().get(i).goal();
        String arrow =
            switch (LoopMaze.goalFacing(i)) {
              case EAST -> ">";
              case WEST -> "<";
              case NORTH -> "^";
              case SOUTH -> "v";
            };
        Label marker =
            Scene2dElementFactory.createLabel("" + (i + 1) + arrow, 16, ProgrammingTerminal.ACCENT);
        marker.setBounds(
            MAP_INSET + (cell.x() + 1) * CELL_STEP + 3,
            MAP_INSET + cell.y() * CELL_STEP + 2,
            CELL_SIZE - 6,
            CELL_SIZE - 4);
        marker.setAlignment(Align.bottomLeft);
        addActor(marker);
      }
      for (var cell : java.util.List.of(LoopMaze.monster(), LoopMaze.pit())) {
        Label marker = Scene2dElementFactory.createLabel("!", 24, ProgrammingTerminal.ACCENT);
        marker.setBounds(
            MAP_INSET + (cell.x() + 1) * CELL_STEP,
            MAP_INSET + cell.y() * CELL_STEP,
            CELL_SIZE,
            CELL_SIZE);
        marker.setAlignment(Align.center);
        addActor(marker);
      }
    }
  }

  @Override
  protected void layoutContent() {
    if (heading != null) heading.setBounds(24, height() - 42, width() - 48, 26);
    if (text != null) {
      float inset = kind.equals("status") ? 136 : 24;
      text.setBounds(inset, 24, width() - inset - 24, height() - 80);
    }
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
    if (kind.equals("rune") || kind.equals("executor")) {
      boolean slot = kind.equals("executor");
      CanvasGraphics.fill(batch, ProgrammingTerminal.INK, alpha, x(), y(), width(), height());
      CanvasGraphics.outline(
          batch,
          slot ? ProgrammingTerminal.ACCENT : Color.valueOf("647784"),
          alpha,
          x(),
          y(),
          width(),
          height(),
          slot ? 3 : 1);
      if (hover)
        CanvasGraphics.fill(
            batch, ProgrammingTerminal.ACCENT, alpha * .2f, x(), y(), width(), height());
      String runeId = slot ? "" : id();
      LoopPuzzle.rune(runeId)
          .ifPresent(
              rune -> {
                int index = LoopPuzzle.runes().indexOf(rune);
                if (runeImage == null)
                  runeImage =
                      new TextureRegion(
                          TextureMap.instance()
                              .textureAt(new SimpleIPath("spritesheets/runes.png")));
                runeImage.setRegion(index % 8 * 16, index / 8 * 16, 16, 16);
                Color tint =
                    Color.valueOf(
                        switch (rune.type()) {
                          case WHILE -> "99ccff";
                          case DO_WHILE -> "dd99ff";
                          case FOR -> "ffcc88";
                        });
                batch.setColor(tint.r, tint.g, tint.b, alpha);
                batch.draw(runeImage, x() + (width() - 48) / 2, y() + (height() - 48) / 2, 48, 48);
                batch.setColor(Color.WHITE);
              });
      return;
    }
    CanvasGraphics.fill(batch, ProgrammingTerminal.PAPER, alpha, x(), y(), width(), height());
    CanvasGraphics.fill(
        batch,
        hover ? ProgrammingTerminal.ACCENT : Color.valueOf("647784"),
        alpha,
        x(),
        y() + height() - 3,
        width(),
        3);
    if (!kind.equals("map")) return;
    for (int row = 0; row < 10; row++)
      for (int col = -1; col < 6; col++) {
        boolean path = LoopMaze.cells().contains(new LoopMaze.Cell(col, row));
        CanvasGraphics.fill(
            batch,
            path ? Color.valueOf("50616a") : ProgrammingTerminal.INK,
            alpha,
            x() + MAP_INSET + (col + 1) * CELL_STEP,
            y() + MAP_INSET + row * CELL_STEP,
            CELL_SIZE,
            CELL_SIZE);
      }
    if (state == null) return;
    if (!state.finished() && state.checkpoint() < LoopMaze.checkpoints().size()) {
      var target = LoopMaze.checkpoints().get(state.checkpoint()).goal();
      CanvasGraphics.outline(
          batch,
          ProgrammingTerminal.ACCENT,
          alpha,
          x() + MAP_INSET + (target.x() + 1) * CELL_STEP,
          y() + MAP_INSET + target.y() * CELL_STEP,
          CELL_SIZE,
          CELL_SIZE,
          2);
    }
    if (!state.observationReady()) return;
    if (head != null) {
      batch.setColor(Color.WHITE);
      batch.draw(
          head,
          x() + MAP_INSET + (state.cellX() + 1) * CELL_STEP + (CELL_SIZE - 28) / 2f,
          y() + MAP_INSET + state.cellY() * CELL_STEP + (CELL_SIZE - 33) / 2f,
          28,
          33);
    }
  }

  Optional<String> hoverCode() {
    String runeId =
        kind.equals("rune")
            ? id()
            : kind.equals("executor") && state != null ? state.activeRune() : "";
    return LoopPuzzle.rune(runeId).map(LoopRune::code);
  }

  @Override
  public void onMove(float dx, float dy) {
    if (!movable()) return;
    if (!dragging) {
      dragStartX = x();
      dragStartY = y();
      dragging = true;
    }
    super.onMove(dx, dy);
  }

  @Override
  public void onDrop(float worldX, float worldY) {
    if (dragging && slotted && state != null && !state.busy()) {
      homeX = x();
      homeY = y();
      canvas().fireServerEvent("removeRune", new DialogResponseMessage.StringValue(id()));
    }
    dragging = false;
  }

  @Override
  public void onDragEnter(CanvasDragContext context) {
    hover = kind.equals("executor");
  }

  @Override
  public void onDragExit(CanvasDragContext context) {
    hover = false;
  }

  @Override
  public boolean onNodeDropped(CanvasDragContext context) {
    if (!kind.equals("executor")
        || state == null
        || !(context.draggedNode() instanceof ProgrammingTerminalNode rune)
        || !rune.kind.equals("rune")) return false;
    // Keep the existing slot until the server accepts the replacement, including during return.
    rune.position(rune.dragStartX, rune.dragStartY);
    rune.dragging = false;
    if (state.busy() || state.finished() || pending) return true;
    pending = true;
    pendingTime = 0;
    canvas().fireServerEvent("execute", new DialogResponseMessage.StringValue(rune.id()));
    return true;
  }
}
