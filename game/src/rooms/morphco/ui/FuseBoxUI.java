package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.Cursors;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import rooms.morphco.puzzles.FuseBox;
import rooms.morphco.util.MorphCoText;

/**
 * The opened fuse box in the control room.
 *
 * <p>Loose cable ends hang out of the duct on the left, the sockets sit on the right in another
 * order. A cable end and its socket share label and color. The player drags a cable end onto a
 * socket, or clicks the cable end and then the socket. The right socket takes the cable and lights
 * up, any other one sends it back. Once every cable is in, the lamps of the box come on and the
 * wiring goes to the server, which checks it with {@link FuseBox}.
 */
final class FuseBoxUI extends MorphCoConsoleUI {

  /** Sockets from top to bottom; the cable ends hang in the order of {@link FuseBox.Cable}. */
  private static final List<FuseBox.Cable> SOCKETS =
      List.of(
          FuseBox.Cable.CONTROL,
          FuseBox.Cable.EMERGENCY,
          FuseBox.Cable.F04,
          FuseBox.Cable.CORRIDOR,
          FuseBox.Cable.A12);

  private static final Color F04_RED = Color.valueOf("e0483e");
  private static final Color A12_BLUE = Color.valueOf("3d7be0");

  private static final float BOX_WIDTH = 880;
  private static final float MARGIN = 16;
  private static final float TOP = 58;
  private static final float ROW = 62;
  private static final float BOTTOM = 14;
  private static final float DUCT_X = 20;
  private static final float DUCT_WIDTH = 22;
  private static final float REST_X = 96;
  private static final float PLUG_WIDTH = 190;
  private static final float PLUG_HEIGHT = 40;
  private static final float TIP = 18;
  private static final float SOCKET_WIDTH = 240;
  private static final float SOCKET_HEIGHT = 48;
  private static final int CABLE_PIECES = 16;

  private final Housing housing = area().addNode(new Housing());
  private final Map<FuseBox.Cable, Plug> plugs = new EnumMap<>(FuseBox.Cable.class);
  private final Map<FuseBox.Cable, Socket> sockets = new EnumMap<>(FuseBox.Cable.class);
  private final Set<FuseBox.Cable> plugged = EnumSet.noneOf(FuseBox.Cable.class);
  private FuseBox.Cable selected;

  private FuseBoxUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.control.fuse-box-title"),
        MorphCoText.text("world.control.fuse-box"),
        MorphCoText.text("world.control.fuse-box-help"));
    for (FuseBox.Cable cable : FuseBox.Cable.values()) {
      sockets.put(cable, area().addNode(new Socket(cable)));
      plugs.put(cable, area().addNode(new Plug(cable)));
    }
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new FuseBoxUI(context);
  }

  private static String label(FuseBox.Cable cable) {
    return MorphCoText.text("world.control.fuse-box-" + cable.name().toLowerCase(Locale.ROOT));
  }

  private static Color cableColor(FuseBox.Cable cable) {
    return switch (cable) {
      case F04 -> F04_RED;
      case A12 -> A12_BLUE;
      case CORRIDOR -> MorphCoUI.SIGNAL;
      case CONTROL -> MorphCoUI.TEXT;
      case EMERGENCY -> MorphCoUI.GO;
    };
  }

  private boolean allPlugged() {
    return plugged.size() == plugs.size();
  }

  private static float rowCenter(int row) {
    return -MARGIN - TOP - row * ROW - ROW / 2;
  }

  @Override
  protected float arrange(float width, float height) {
    float boxWidth = Math.min(BOX_WIDTH, width);
    float left = Math.round((width - boxWidth) / 2);
    float boxHeight = TOP + SOCKETS.size() * ROW + BOTTOM;
    housing.size(boxWidth, boxHeight).position(left, -MARGIN - boxHeight);
    for (int row = 0; row < SOCKETS.size(); row++) {
      sockets
          .get(SOCKETS.get(row))
          .position(left + boxWidth - 20 - SOCKET_WIDTH, rowCenter(row) - SOCKET_HEIGHT / 2);
    }
    FuseBox.Cable[] cables = FuseBox.Cable.values();
    for (int row = 0; row < cables.length; row++) {
      Plug plug = plugs.get(cables[row]);
      plug.anchorX = left + DUCT_X + DUCT_WIDTH;
      plug.anchorY = rowCenter(row);
      plug.restX = left + REST_X;
      plug.restY = rowCenter(row) - PLUG_HEIGHT / 2;
      plug.settle();
    }
    return MARGIN + boxHeight + MARGIN;
  }

  // Plugs a cable into a socket; the socket of another area sends the cable back.
  private void plug(FuseBox.Cable cable, Socket socket) {
    selected = null;
    Plug plug = plugs.get(cable);
    if (socket.cable != cable) {
      plug.bounce(socket);
      Sounds.playUi(CoreSounds.INTERFACE_BUTTON_BACKWARD);
      feedback(MorphCoText.text("world.control.fuse-box-wrong"), false);
      return;
    }
    plugged.add(cable);
    plug.settle();
    Sounds.playUi(CoreSounds.SETTINGS_TOGGLE_CLICK);
    if (!allPlugged()) {
      feedback("", true);
      return;
    }
    feedback(MorphCoText.text("world.control.fuse-box-done"), true);
    submit(FuseBox.encode(plugged));
  }

  // A cable that sags between the duct and the plug, drawn as short straight pieces with a dark
  // edge.
  private static void cable(
      Batch batch, Color color, float alpha, float x1, float y1, float x2, float y2) {
    float sag = Math.min(60, Vector2.dst(x1, y1, x2, y2) / 4);
    float middleX = (x1 + x2) / 2;
    float middleY = Math.min(y1, y2) - sag;
    for (int pass = 0; pass < 2; pass++) {
      Color tint = pass == 0 ? MorphCoUI.OUTLINE : color;
      float thickness = pass == 0 ? 12 : 8;
      float fromX = x1;
      float fromY = y1;
      for (int piece = 1; piece <= CABLE_PIECES; piece++) {
        float t = (float) piece / CABLE_PIECES;
        float u = 1 - t;
        float toX = u * u * x1 + 2 * u * t * middleX + t * t * x2;
        float toY = u * u * y1 + 2 * u * t * middleY + t * t * y2;
        line(batch, tint, alpha, fromX, fromY, toX, toY, thickness);
        fromX = toX;
        fromY = toY;
      }
    }
  }

  // A straight piece that reaches half its thickness past both ends, so the pieces close up.
  private static void line(
      Batch batch,
      Color color,
      float alpha,
      float x1,
      float y1,
      float x2,
      float y2,
      float thickness) {
    float half = thickness / 2;
    float angle = MathUtils.atan2(y2 - y1, x2 - x1) * MathUtils.radiansToDegrees;
    Color previous = batch.getColor().cpy();
    batch.setColor(color.r, color.g, color.b, color.a * alpha);
    batch.draw(
        CanvasGraphics.white(),
        x1 - half,
        y1 - half,
        half,
        half,
        Vector2.dst(x1, y1, x2, y2) + thickness,
        thickness,
        1,
        1,
        angle);
    batch.setColor(previous);
  }

  /** The opened box: steel housing with warning stripes, the lamps and the cable duct. */
  private final class Housing extends CanvasNode {
    private Label brand;

    private Housing() {
      super("housing", BOX_WIDTH, 400);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {
      brand = MorphCoUI.bold("MORPH & CO. · 400 V", 18, MorphCoUI.MUTED);
      brand.setWrap(false);
      brand.setAlignment(Align.right);
      addActor(brand);
    }

    @Override
    protected void layoutContent() {
      brand.setBounds(width() - 320, height() - 50, 300, 24);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float top = y + height();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      MorphCoUI.hazard(batch, alpha, x + 16, top - 18, width() - 32, 8);
      Color lamp = allPlugged() ? MorphCoUI.GO : MorphCoUI.LAMP_OFF;
      for (int index = 0; index < plugs.size(); index++) {
        CanvasGraphics.fill(
            batch, MorphCoUI.STEEL_DARK, alpha, x + 20 + index * 34, top - 50, 24, 24);
        CanvasGraphics.fill(batch, lamp, alpha, x + 24 + index * 34, top - 46, 16, 16);
      }
      CanvasGraphics.fill(
          batch, MorphCoUI.INSET, alpha, x + 12, y + 8, width() - 24, height() - TOP);
      MorphCoUI.plate(
          batch, MorphCoUI.STEEL_DARK, alpha, x + DUCT_X, y + 14, DUCT_WIDTH, height() - TOP - 12);
      // The cables run behind all plugs and sockets, so they never cover a label.
      for (Plug plug : plugs.values()) {
        cable(
            batch,
            cableColor(plug.cable),
            alpha,
            plug.anchorX,
            plug.anchorY,
            plug.x(),
            plug.y() + PLUG_HEIGHT / 2);
      }
    }
  }

  /** A socket in the box, labelled with the part of the factory it supplies. */
  private final class Socket extends CanvasNode {
    private final FuseBox.Cable cable;
    private Label heading;
    private boolean over;

    private Socket(FuseBox.Cable cable) {
      super("socket-" + cable, SOCKET_WIDTH, SOCKET_HEIGHT);
      this.cable = cable;
      z(1);
      movable(false);
      selectable(false);
      deletable(false);
      // Sockets handle their clicks themselves: the canvas would raise them above the plugs.
      addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (selected != null) plug(selected, Socket.this);
            }
          });
    }

    // The corner of a plug whose tip sits in this socket.
    private float plugX() {
      return x() - PLUG_WIDTH + TIP + 10;
    }

    private float plugY() {
      return y() + (SOCKET_HEIGHT - PLUG_HEIGHT) / 2;
    }

    @Override
    protected void buildContent() {
      heading = MorphCoUI.bold(label(cable), 18, MorphCoUI.INK);
      heading.setWrap(false);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(52, 10, width() - 92, height() - 20);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      setUserObject(selected == null ? Cursors.DEFAULT : Cursors.INTERACT);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      boolean lit = plugged.contains(cable);
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      // The jack: a ring in the color of its cable around the hole.
      CanvasGraphics.fill(batch, cableColor(cable), alpha, x + 10, y + 8, 32, 32);
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 18, y + 16, 16, 16);
      CanvasGraphics.fill(batch, MorphCoUI.TAG, alpha, x + 50, y + 8, width() - 88, 32);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + width() - 32, y + 14, 20, 20);
      CanvasGraphics.fill(
          batch, lit ? MorphCoUI.GO : MorphCoUI.LAMP_OFF, alpha, x + width() - 28, y + 18, 12, 12);
      if (lit) {
        CanvasGraphics.outline(batch, MorphCoUI.GO, alpha, x, y, width(), height(), 3);
      } else if (over || selected != null) {
        CanvasGraphics.outline(
            batch,
            MorphCoUI.SIGNAL,
            alpha * (over ? 1 : .6f),
            x,
            y,
            width(),
            height(),
            over ? 4 : 2);
      }
    }

    @Override
    public void onDragEnter(CanvasDragContext context) {
      onDragOver(context);
    }

    @Override
    public void onDragOver(CanvasDragContext context) {
      over = accepts(context);
    }

    @Override
    public void onDragExit(CanvasDragContext context) {
      over = false;
    }

    private boolean accepts(CanvasDragContext context) {
      return context.draggedNode() instanceof Plug plug
          && !plugged.contains(plug.cable)
          && context.localX() >= 0
          && context.localX() < width()
          && context.localY() >= 0
          && context.localY() < height();
    }

    @Override
    public boolean onNodeDropped(CanvasDragContext context) {
      over = false;
      if (!accepts(context)) return false;
      plug(((Plug) context.draggedNode()).cable, this);
      return true;
    }
  }

  /** A loose cable end; the housing draws its cable from the duct to wherever the plug is. */
  private final class Plug extends CanvasNode {
    private final FuseBox.Cable cable;
    private Label heading;
    private boolean dragging;
    private float anchorX;
    private float anchorY;
    private float restX;
    private float restY;

    private Plug(FuseBox.Cable cable) {
      super("plug-" + cable, PLUG_WIDTH, PLUG_HEIGHT);
      this.cable = cable;
      z(2);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.GRAB);
    }

    // Puts the plug into its socket or back to where it hangs.
    private void settle() {
      clearActions();
      dragging = false;
      if (plugged.contains(cable)) {
        Socket socket = sockets.get(cable);
        position(socket.plugX(), socket.plugY());
      } else {
        position(restX, restY);
      }
    }

    // The plug hits the wrong socket and springs back to where it hangs.
    private void bounce(Socket socket) {
      clearActions();
      addAction(
          Actions.sequence(
              Actions.moveTo(socket.plugX(), socket.plugY(), dragging ? 0 : 0.15f),
              Actions.moveTo(restX, restY, 0.3f, Interpolation.pow2Out)));
      dragging = false;
    }

    @Override
    protected void buildContent() {
      heading = MorphCoUI.bold(label(cable), 16, MorphCoUI.INK);
      heading.setWrap(false);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(12, 6, width() - TIP - 24, height() - 12);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      if (plugged.contains(cable)) setUserObject(Cursors.DEFAULT);
      else setUserObject(dragging ? Cursors.GRABBING : Cursors.GRAB);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float middle = y + height() / 2;
      float body = width() - TIP;
      if (dragging)
        CanvasGraphics.fill(batch, Color.BLACK, alpha * .35f, x + 5, y - 5, body, height());
      // The metal tip goes into the jack.
      CanvasGraphics.fill(batch, MorphCoUI.GHOST, alpha, x + body, middle - 7, TIP, 14);
      MorphCoUI.plate(batch, cableColor(cable), alpha, x, y, body, height());
      boolean chosen = cable == selected;
      CanvasGraphics.fill(
          batch,
          chosen ? MorphCoUI.ORANGE : MorphCoUI.TAG,
          alpha,
          x + 8,
          y + 6,
          body - 16,
          height() - 12);
      if (chosen || dragging)
        CanvasGraphics.outline(batch, MorphCoUI.INK, alpha, x, y, body, height(), 2);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (plugged.contains(cable)) return;
      if (!dragging) {
        clearActions();
        dragging = true;
        selected = null;
      }
      super.onMove(dx, dy);
    }

    // Let go anywhere but on a socket, the plug drops back to where it hangs.
    @Override
    public void onDrop(float worldX, float worldY) {
      if (plugged.contains(cable)) return;
      dragging = false;
      addAction(Actions.moveTo(restX, restY, 0.3f, Interpolation.pow2Out));
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      if (button != 0 || plugged.contains(cable)) return;
      selected = cable == selected ? null : cable;
    }
  }
}
