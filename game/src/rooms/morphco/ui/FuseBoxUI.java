package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.cables.CablePlugging;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.util.MorphCoText;

/**
 * The opened fuse box in the control room, the Morph & Co. look of the cable dialog.
 *
 * <p>Loose cable ends hang out of the duct on the left, the sockets sit on the right in another
 * order. A cable end and its socket share label and color. Title and help come from the dialog
 * context, the line below the title is the fuse box's own. The plugging comes from the engine's
 * {@link CablePlugging}; once every cable is in, the lamps of the box come on.
 */
final class FuseBoxUI extends MorphCoConsoleUI {

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

  private final Housing housing = area().addNode(new Housing());
  private final CablePlugging plugging;
  private final List<Plug> plugs = new ArrayList<>();
  private final List<Socket> sockets = new ArrayList<>();

  private FuseBoxUI(DialogContext context) {
    super(
        context,
        CablePlugging.title(context),
        MorphCoText.text("world.control.fuse-box"),
        CablePlugging.help(context));
    plugging = new CablePlugging(context, this::plugged);
    for (int index = 0; index < plugging.puzzle().sockets().size(); index++) {
      sockets.add(area().addNode(new Socket(index)));
      plugs.add(area().addNode(new Plug(index)));
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

  private void plugged(CablePlugging.Result result) {
    switch (result) {
      case WRONG -> feedback(MorphCoText.text("world.control.fuse-box-wrong"), false);
      case PLUGGED -> feedback("", true);
      case CONNECTED -> feedback(MorphCoText.text("world.control.fuse-box-done"), true);
      case PICKED -> {
        // The display keeps its message until the next plug.
      }
    }
  }

  private static float rowCenter(int row) {
    return -MARGIN - TOP - row * ROW - ROW / 2;
  }

  @Override
  protected float arrange(float width, float height) {
    float boxWidth = Math.min(BOX_WIDTH, width);
    float left = Math.round((width - boxWidth) / 2);
    float boxHeight = TOP + sockets.size() * ROW + BOTTOM;
    housing.size(boxWidth, boxHeight).position(left, -MARGIN - boxHeight);
    for (int row = 0; row < sockets.size(); row++) {
      sockets
          .get(row)
          .position(left + boxWidth - 20 - SOCKET_WIDTH, rowCenter(row) - SOCKET_HEIGHT / 2);
    }
    for (int row = 0; row < plugs.size(); row++) {
      plugs
          .get(row)
          .hang(
              left + DUCT_X + DUCT_WIDTH,
              rowCenter(row),
              left + REST_X,
              rowCenter(row) - PLUG_HEIGHT / 2);
    }
    return MARGIN + boxHeight + MARGIN;
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
      Color lamp = plugging.allPlugged() ? MorphCoUI.GO : MorphCoUI.LAMP_OFF;
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
      for (Plug plug : plugs) {
        CablePlugging.drawCable(
            batch,
            plug.cableColor(),
            MorphCoUI.OUTLINE,
            alpha,
            plug.anchorX(),
            plug.anchorY(),
            plug.x(),
            plug.y() + PLUG_HEIGHT / 2);
      }
    }
  }

  /** A socket in the box, labelled with the part of the factory it supplies. */
  private final class Socket extends CablePlugging.Socket {
    private Label heading;

    private Socket(int index) {
      super(plugging, index, SOCKET_WIDTH, SOCKET_HEIGHT);
    }

    @Override
    protected float plugX() {
      return x() - PLUG_WIDTH + TIP + 10;
    }

    @Override
    protected float plugY() {
      return y() + (SOCKET_HEIGHT - PLUG_HEIGHT) / 2;
    }

    @Override
    protected void buildContent() {
      heading = MorphCoUI.bold(label(), 18, MorphCoUI.INK);
      heading.setWrap(false);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(52, 10, width() - 92, height() - 20);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      boolean lit = lit();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      // The jack: a ring in the color of its cable around the hole.
      CanvasGraphics.fill(batch, cableColor(), alpha, x + 10, y + 8, 32, 32);
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 18, y + 16, 16, 16);
      CanvasGraphics.fill(batch, MorphCoUI.TAG, alpha, x + 50, y + 8, width() - 88, 32);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + width() - 32, y + 14, 20, 20);
      CanvasGraphics.fill(
          batch, lit ? MorphCoUI.GO : MorphCoUI.LAMP_OFF, alpha, x + width() - 28, y + 18, 12, 12);
      if (lit) {
        CanvasGraphics.outline(batch, MorphCoUI.GO, alpha, x, y, width(), height(), 3);
      } else if (over() || waiting()) {
        CanvasGraphics.outline(
            batch,
            MorphCoUI.SIGNAL,
            alpha * (over() ? 1 : .6f),
            x,
            y,
            width(),
            height(),
            over() ? 4 : 2);
      }
    }
  }

  /** A loose cable end; the housing draws its cable from the duct to wherever the plug is. */
  private final class Plug extends CablePlugging.Plug {
    private Label heading;

    private Plug(int cable) {
      super(plugging, cable, PLUG_WIDTH, PLUG_HEIGHT);
    }

    @Override
    protected void buildContent() {
      heading = MorphCoUI.bold(label(), 16, MorphCoUI.INK);
      heading.setWrap(false);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(12, 6, width() - TIP - 24, height() - 12);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float middle = y + height() / 2;
      float body = width() - TIP;
      if (dragging())
        CanvasGraphics.fill(batch, Color.BLACK, alpha * .35f, x + 5, y - 5, body, height());
      // The metal tip goes into the jack.
      CanvasGraphics.fill(batch, MorphCoUI.GHOST, alpha, x + body, middle - 7, TIP, 14);
      MorphCoUI.plate(batch, cableColor(), alpha, x, y, body, height());
      boolean chosen = chosen();
      CanvasGraphics.fill(
          batch,
          chosen ? MorphCoUI.ORANGE : MorphCoUI.TAG,
          alpha,
          x + 8,
          y + 6,
          body - 16,
          height() - 12);
      if (chosen || dragging())
        CanvasGraphics.outline(batch, MorphCoUI.INK, alpha, x, y, body, height(), 2);
    }
  }
}
