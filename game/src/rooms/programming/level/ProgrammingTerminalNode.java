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
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopRune;
import rooms.programming.modules.loops.TerminalState;

/** Room-specific map, program cards and the single execution slot. */
final class ProgrammingTerminalNode extends CanvasNode {
  private static final String TYPE = "programming.terminal-node";
  private String kind;
  private TerminalState state;
  private Label text;
  private boolean hover;
  private boolean pending;
  private float pendingTime;
  private TextureRegion head;
  private boolean dragging;
  private float dragStartX;
  private float dragStartY;

  static void register() {
    if (!CanvasNodeType.isRegistered(TYPE))
      CanvasNodeType.register(
          TYPE, s -> new ProgrammingTerminalNode(s.id(), s.prop("kind", "rune")));
  }

  ProgrammingTerminalNode(String id, String kind) {
    super(
        id,
        kind.equals("map") ? 395 : 360,
        kind.equals("map") ? 580 : kind.equals("executor") ? 205 : kind.equals("help") ? 280 : 300);
    this.kind = kind;
    deletable(false);
    movable(kind.equals("rune"));
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put("kind", kind);
  }

  @Override
  protected void readProps(NodeState value) {
    kind = value.prop("kind", "rune");
  }

  void update(TerminalState value) {
    state = value;
    if (kind.equals("rune")) {
      boolean active = value.busy() && value.activeRune().equals(id());
      movable(!active);
      setVisible(!active);
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
      return "Befehle\n\nschritt(): ein Feld vorwärts\nspringen(): über eine Grube, zwei Felder\nangreifen(): Gegner direkt voraus\nbodenVoraus(): Boden, auch mit Gegner\namWegzeichen(): aktuelles Ziel erreicht\nDrehen: eine Vierteldrehung";
    if (kind.equals("rune"))
      return LoopPuzzle.rune(id()).map(r -> r.title() + "\n\n" + r.code()).orElse(id());
    if (kind.equals("executor")) {
      if (state == null) return "Ausführen\n\nEine Rune hier ablegen";
      return (state.busy()
              ? "Golem arbeitet"
              : state.finished() ? "Weg abgeschlossen" : "Ausführen")
          + "\n\n"
          + (state.busy()
              ? LoopPuzzle.rune(state.activeRune()).map(LoopRune::title).orElse("")
              : "Eine Rune hier ablegen")
          + "\n\n"
          + state.status();
    }
    return "Labyrinth\n"
        + (state == null ? "" : state.completed() + " / 5 Wegpunkte")
        + "\n! Hindernis · Pfeil = Zielblickrichtung";
  }

  @Override
  protected void buildContent() {
    text =
        Scene2dElementFactory.createLabel(
            caption(),
            kind.equals("rune") || kind.equals("help") ? 16 : 18,
            Color.valueOf("f1eadc"));
    text.setAlignment(Align.topLeft);
    text.setWrap(true);
    addActor(text);
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
      Label help =
          Scene2dElementFactory.createLabel(
              "1 Feld = 5 x 3 Weltkacheln\nLeertaste + Ziehen: verschieben · Rad: Zoom",
              13,
              Color.valueOf("bcc8cb"));
      help.setBounds(18, 14, width() - 36, 40);
      addActor(help);
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
        marker.setPosition(27 + (cell.x() + 1) * 49, 70 + cell.y() * 44);
        addActor(marker);
      }
      for (var cell : java.util.List.of(LoopMaze.monster(), LoopMaze.pit())) {
        Label marker = Scene2dElementFactory.createLabel("!", 24, ProgrammingTerminal.ACCENT);
        marker.setPosition(38 + (cell.x() + 1) * 49, 78 + cell.y() * 44);
        addActor(marker);
      }
    }
  }

  @Override
  protected void layoutContent() {
    text.setBounds(
        18,
        kind.equals("map") ? height() - 103 : 14,
        width() - 36,
        kind.equals("map") ? 85 : height() - 30);
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
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
    for (int row = 0; row < 9; row++)
      for (int col = -1; col < 6; col++) {
        boolean path = LoopMaze.cells().contains(new LoopMaze.Cell(col, row));
        CanvasGraphics.fill(
            batch,
            path ? Color.valueOf("50616a") : ProgrammingTerminal.INK,
            alpha,
            x() + 24 + (col + 1) * 49,
            y() + 68 + row * 44,
            45,
            40);
      }
    if (state == null) return;
    if (!state.finished() && state.checkpoint() < LoopMaze.checkpoints().size()) {
      var target = LoopMaze.checkpoints().get(state.checkpoint()).goal();
      CanvasGraphics.outline(
          batch,
          ProgrammingTerminal.ACCENT,
          alpha,
          x() + 24 + (target.x() + 1) * 49,
          y() + 68 + target.y() * 44,
          45,
          40,
          2);
    }
    if (!state.observationReady()) return;
    if (head != null) {
      batch.setColor(Color.WHITE);
      batch.draw(head, x() + 31 + (state.cellX() + 1) * 49, y() + 73 + state.cellY() * 44, 28, 33);
    }
  }

  @Override
  public void onMove(float dx, float dy) {
    if (!dragging) {
      dragStartX = x();
      dragStartY = y();
      dragging = true;
    }
    super.onMove(dx, dy);
  }

  @Override
  public void onDrop(float worldX, float worldY) {
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
        || state.busy()
        || state.finished()
        || pending
        || !(context.draggedNode() instanceof ProgrammingTerminalNode rune)
        || !rune.kind.equals("rune")) return false;
    // Cards remain canvas-owned. Only authoritative state hides the active card, preventing loss.
    rune.position(rune.dragStartX, rune.dragStartY);
    rune.dragging = false;
    pending = true;
    pendingTime = 0;
    canvas().fireServerEvent("execute", new DialogResponseMessage.StringValue(rune.id()));
    return true;
  }
}
