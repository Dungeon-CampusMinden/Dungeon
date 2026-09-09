package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import java.util.Objects;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.VariablePuzzleStage;

/** Physical supply tokens, labelled storage sockets, and the golem's assembly preview. */
final class ProgrammingBindingNode extends CanvasNode {
  enum Kind {
    VESSEL,
    ESSENCE,
    SOCKET,
    CORE,
    FEEDBACK,
    HEADING
  }

  private static final String TYPE = "programming.binding-node";
  private static final Color INK = Color.valueOf("11191d");
  private static final Color SURFACE = Color.valueOf("283338");
  private static final Color TEXT = Color.valueOf("efe8d8");
  private static final Color MUTED = Color.valueOf("a8b2b0");
  private static final Color GOLD = Color.valueOf("e8b566");
  private Kind kind;
  private BindingState state;
  private Label title;
  private Label detail;
  private Label value;
  private Label erase;
  private TextureRegion texture;
  private String imagePath = "";
  private float time;
  private float pulse;
  private boolean over;
  private boolean dragging;
  private float homeX;
  private float homeY;

  static void register() {
    if (!CanvasNodeType.isRegistered(TYPE))
      CanvasNodeType.register(
          TYPE, s -> new ProgrammingBindingNode(s.id(), Kind.valueOf(s.prop("kind", "SOCKET"))));
  }

  ProgrammingBindingNode(String id, Kind kind) {
    super(
        id,
        switch (kind) {
          case VESSEL -> 220;
          case ESSENCE -> 136;
          case SOCKET -> 278;
          case CORE -> 220;
          case FEEDBACK -> 800;
          case HEADING -> 300;
        },
        switch (kind) {
          case VESSEL -> 86;
          case ESSENCE -> 90;
          case SOCKET -> 146;
          case CORE -> 340;
          case FEEDBACK -> 96;
          case HEADING -> 36;
        });
    this.kind = kind;
    deletable(false);
    movable(kind == Kind.VESSEL || kind == Kind.ESSENCE);
    selectable(false);
    if (kind == Kind.FEEDBACK || kind == Kind.HEADING)
      setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put("kind", kind.name());
  }

  @Override
  protected void readProps(NodeState state) {
    kind = Kind.valueOf(state.prop("kind", "SOCKET"));
  }

  void update(BindingState next) {
    if (Objects.equals(state, next)) return;
    boolean changed =
        state != null
            && kind == Kind.SOCKET
            && (state.vessels().get(property()) != next.vessels().get(property())
                || state.essences().get(property()) != next.essences().get(property()));
    state = next;
    if (changed) pulse = 1;
    if (kind == Kind.VESSEL) {
      setVisible(next.vesselsCollected());
      movable(next.propertiesCollected() && next.stage() == VariablePuzzleStage.VESSELS);
    }
    if (kind == Kind.ESSENCE) {
      setVisible(next.stage() != VariablePuzzleStage.VESSELS);
      movable(next.stage() == VariablePuzzleStage.ESSENCES);
    }
    if (kind == Kind.SOCKET) setVisible(next.propertiesCollected());
    if (kind == Kind.HEADING)
      setVisible(
          id().equals("vessel-heading")
              ? next.vesselsCollected()
              : next.stage() != VariablePuzzleStage.VESSELS);
    if (title != null) refreshText();
    invalidateLayout();
  }

  private GolemProperty property() {
    return GolemProperty.valueOf(id());
  }

  private SoulVessel vessel() {
    return SoulVessel.valueOf(id().substring("vessel-".length()));
  }

  private MagicalEssence essence() {
    return MagicalEssence.valueOf(id().substring("essence-".length()));
  }

  @Override
  protected void buildContent() {
    title = label(20, TEXT);
    detail = label(16, MUTED);
    value = label(24, TEXT);
    erase = label(22, MUTED);
    refreshText();
  }

  private Label label(int size, Color color) {
    Label label = Scene2dElementFactory.createLabel("", size, color);
    label.setAlignment(Align.left);
    label.setWrap(true);
    addActor(label);
    return label;
  }

  private void refreshText() {
    title.setText("");
    detail.setText("");
    value.setText("");
    erase.setText("");
    if (state == null) return;
    switch (kind) {
      case VESSEL -> {
        title.setText(vessel().label());
        detail.setText(state.revealed() ? vessel().javaType() : vessel().capacity());
      }
      case ESSENCE -> value.setText(essence().literal());
      case SOCKET -> {
        var property = property();
        var container = state.vessels().get(property);
        var stored = state.essences().get(property);
        title.setText(property.label());
        detail.setText("Soll: " + VariablePuzzle.essenceSolution().get(property).literal());
        value.setText(
            container == null ? "Gefäß ablegen" : stored == null ? "leer" : stored.literal());
        if (state.revealed())
          detail.setText(
              container.javaType() + " " + property.identifier() + " = " + stored.literal() + ";");
        if (!state.revealed()
            && (state.stage() == VariablePuzzleStage.VESSELS ? container != null : stored != null))
          erase.setText("×");
      }
      case CORE -> {
        title.setText("Nox");
        title.setAlignment(Align.center);
        detail.setAlignment(Align.center);
        detail.setText(state.revealed() ? "Seelenbindung vollständig" : "Seelenkern");
        value.setAlignment(Align.center);
        value.setText(state.stage() == VariablePuzzleStage.REVEAL ? "Aktivieren" : "");
      }
      case FEEDBACK -> {
        title.setText(
            state.revealed()
                ? "Gefäß · Name · Wert"
                : state.stage() == VariablePuzzleStage.VESSELS
                    ? "Gefäße zuordnen"
                    : "Werte einsetzen");
        detail.setText(state.feedback());
      }
      case HEADING -> title.setText(id().equals("vessel-heading") ? "Gefäßvorrat" : "Essenzfach");
    }
  }

  @Override
  protected void layoutContent() {
    switch (kind) {
      case VESSEL -> {
        title.setBounds(66, 42, 146, 34);
        detail.setBounds(66, 8, 146, 34);
      }
      case ESSENCE -> {
        value.setBounds(8, 10, width() - 16, 34);
        value.setAlignment(Align.center);
      }
      case SOCKET -> {
        title.setBounds(16, height() - 39, width() - 56, 26);
        detail.setBounds(
            16, state != null && state.revealed() ? 4 : height() - 65, width() - 32, 30);
        value.setBounds(78, 34, width() - 94, 40);
        erase.setBounds(width() - 34, height() - 40, 24, 28);
      }
      case CORE -> {
        title.setBounds(0, 308, width(), 28);
        detail.setBounds(0, 65, width(), 38);
        value.setBounds(4, 10, width() - 8, 40);
      }
      case FEEDBACK -> {
        title.setBounds(18, 60, width() - 36, 26);
        detail.setBounds(18, 6, width() - 36, 50);
      }
      case HEADING -> title.setBounds(0, 0, width(), height());
    }
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    time += delta;
    pulse = Math.max(0, pulse - delta * 2);
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
    if (state == null) return;
    if (kind == Kind.CORE) {
      int charged =
          (int)
              state.essences().entrySet().stream()
                  .filter(e -> VariablePuzzle.essenceSolution().get(e.getKey()) == e.getValue())
                  .count();
      sprite(
          batch,
          "character/monster/programming_golem/programming_golem.png",
          x() + 20,
          y() + 106,
          180,
          202,
          alpha * (state.revealed() ? 1 : .65f));
      for (int i = 0; i < 6; i++)
        CanvasGraphics.fill(
            batch, i < charged ? GOLD : SURFACE, alpha, x() + 22 + i * 30, y() + 100, 22, 5);
      if (state.stage() == VariablePuzzleStage.REVEAL) {
        CanvasGraphics.fill(batch, SURFACE, alpha, x() + 4, y() + 8, width() - 8, 44);
        CanvasGraphics.outline(
            batch,
            GOLD,
            alpha * (.75f + .25f * (float) Math.sin(time * 3)),
            x() + 4,
            y() + 8,
            width() - 8,
            44,
            2);
      }
      return;
    }
    if (kind == Kind.HEADING) return;
    CanvasGraphics.fill(batch, SURFACE, alpha, x(), y(), width(), height());
    CanvasGraphics.fill(
        batch, GOLD, alpha * (over ? .9f : .35f), x(), y() + height() - 2, width(), 2);
    if (kind == Kind.SOCKET) {
      var container = state.vessels().get(property());
      CanvasGraphics.fill(batch, INK, alpha, x() + 12, y() + 32, width() - 24, 46);
      if (container != null)
        sprite(batch, vesselImage(container), x() + 20, y() + 34, 40, 40, alpha);
      if (over || pulse > 0)
        CanvasGraphics.outline(
            batch, GOLD, alpha * Math.max(over ? .85f : 0, pulse), x(), y(), width(), height(), 2);
    } else if (kind == Kind.VESSEL)
      sprite(
          batch, vesselImage(vessel()), x() + 10, y() + 20, 48, 48, alpha * (movable() ? 1 : .45f));
    else if (kind == Kind.ESSENCE)
      sprite(batch, "items/rpg/item_gem_quartz.png", x() + 52, y() + 48, 32, 32, alpha);
  }

  private String vesselImage(SoulVessel vessel) {
    return switch (vessel) {
      case IRON_CHEST -> "objects/crate/basic.png";
      case CRYSTAL_BOTTLE -> "items/potion/water_bottle.png";
      case PARCHMENT -> "items/rpg/item_scroll.png";
      case RUNE_STONE -> "items/rpg/item_gem_amethyst.png";
      case LIGHT_ORB -> "items/rpg/item_orb.png";
    };
  }

  private void sprite(
      Batch batch, String path, float x, float y, float width, float height, float alpha) {
    if (!imagePath.equals(path)) {
      imagePath = path;
      texture = new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(path)));
      if (kind == Kind.CORE) texture.setRegion(0, 0, 64, 72);
    }
    Color previous = batch.getColor().cpy();
    batch.setColor(1, 1, 1, alpha);
    batch.draw(texture, x, y, width, height);
    batch.setColor(previous);
  }

  @Override
  public void onMove(float dx, float dy) {
    if (!movable()) return;
    if (!dragging) {
      clearActions();
      homeX = x();
      homeY = y();
      dragging = true;
    }
    super.onMove(dx, dy);
  }

  private void returnToSupply() {
    dragging = false;
    clearActions();
    addAction(Actions.moveTo(homeX, homeY, .16f));
  }

  @Override
  public void onDrop(float worldX, float worldY) {
    dragging = false;
  }

  @Override
  public void onDragEnter(CanvasDragContext context) {
    over = kind == Kind.SOCKET;
  }

  @Override
  public void onDragExit(CanvasDragContext context) {
    over = false;
  }

  @Override
  public boolean onNodeDropped(CanvasDragContext context) {
    if (kind != Kind.SOCKET
        || state == null
        || state.revealed()
        || !(context.draggedNode() instanceof ProgrammingBindingNode source)) return false;
    if (!source.movable() || (source.kind != Kind.VESSEL && source.kind != Kind.ESSENCE))
      return false;
    source.returnToSupply();
    boolean vessel = source.kind == Kind.VESSEL;
    if ((state.stage() == VariablePuzzleStage.VESSELS) != vessel) return true;
    canvas()
        .fireServerEvent(
            vessel ? "vessel" : "essence",
            new DialogResponseMessage.StringValue(
                id() + ":" + (vessel ? source.vessel().name() : source.essence().name())));
    return true;
  }

  @Override
  public void onClick(float localX, float localY, int button) {
    if (button != 0 || state == null) return;
    if (kind == Kind.CORE && state.stage() == VariablePuzzleStage.REVEAL && localY < 60)
      canvas().fireServerEvent("activate", new DialogResponseMessage.StringValue(""));
    if (kind == Kind.SOCKET && !state.revealed() && localX > width() - 42 && localY > height() - 48)
      canvas().fireServerEvent("clear", new DialogResponseMessage.StringValue(id()));
  }
}
