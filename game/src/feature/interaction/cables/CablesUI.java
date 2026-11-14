package feature.interaction.cables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * The default dialog of a cable puzzle: the cables hang out of a duct on the left, the sockets sit
 * on the right. Plugs and sockets show their labels in black on light tags; a socket that holds its
 * cable lights up. The plugging comes from {@link CablePlugging}.
 *
 * <p>A room shows the puzzle in its own look by registering another builder for {@link
 * feature.hud.dialogs.DialogType.DefaultTypes#CABLES}.
 */
public final class CablesUI extends CanvasUI {

  private static final Color BOARD = Color.valueOf("e9e4d8");
  private static final Color RIM = Color.valueOf("4a5262");
  private static final Color INK = Color.valueOf("1b1f27");
  private static final Color TAG = Color.valueOf("fbf8f0");
  private static final Color SOCKET = Color.valueOf("cfc8b8");
  private static final Color TIP_METAL = Color.valueOf("9aa1ab");
  private static final Color CHOSEN = Color.valueOf("f0b43c");
  private static final Color LAMP_OFF = Color.valueOf("8a8478");
  private static final Color GOOD = Color.valueOf("3fae5a");
  private static final Color BAD = Color.valueOf("d9534f");
  private static final float WIDTH = 860;
  private static final float TOP = 80;
  private static final float ROW = 54;
  private static final float BOTTOM = 56;
  private static final float DUCT_X = 24;
  private static final float DUCT_WIDTH = 20;
  private static final float REST_X = 90;
  private static final float PLUG_WIDTH = 230;
  private static final float PLUG_HEIGHT = 38;
  private static final float TIP = 16;
  private static final float SOCKET_WIDTH = 300;
  private static final float SOCKET_HEIGHT = 46;

  private final Board board;
  private final CablePlugging plugging;
  private final List<Plug> plugs = new ArrayList<>();

  private CablesUI(DialogContext context, String title, String help) {
    super(
        context.dialogType().type(),
        new CanvasLayout(
            title,
            WIDTH,
            600,
            new CanvasOptions()
                .grid(32, false)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false)
                .panButton(-1)
                .zoom(1, 1),
            false),
        CanvasSnapshot.empty(),
        context.dialogId(),
        List.of());
    plugging = new CablePlugging(context, this::plugged);
    int count = plugging.puzzle().cables().size();
    float height = TOP + count * ROW + BOTTOM;
    board = area().addNode(new Board(help));
    board.size(WIDTH, height).position(0, -height);
    for (int index = 0; index < count; index++) {
      area()
          .addNode(new Socket(index))
          .position(WIDTH - DUCT_X - SOCKET_WIDTH, rowCenter(index) - SOCKET_HEIGHT / 2);
    }
    // After the sockets, since a plugged-in plug sits in its socket.
    for (int cable = 0; cable < count; cable++) {
      Plug plug = area().addNode(new Plug(cable));
      plug.hang(DUCT_X + DUCT_WIDTH, rowCenter(cable), REST_X, rowCenter(cable) - PLUG_HEIGHT / 2);
      plugs.add(plug);
    }
    if (plugging.allPlugged()) board.feedback(CablePlugging.TEXTS.text("done"), true);
    // The canvas keeps the board in the middle when the window changes its size.
    area().resetView();
  }

  /**
   * Builds the dialog of a cable puzzle.
   *
   * @param context the dialog context of {@link Cables#show}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new CablesUI(context, CablePlugging.title(context), CablePlugging.help(context));
  }

  private static float rowCenter(int row) {
    return -TOP - row * ROW - ROW / 2;
  }

  private static Label inkLabel(String text, int size, boolean bold) {
    Label label =
        Scene2dElementFactory.createLabel(
            text,
            FontSpec.of(
                bold ? Scene2dElementFactory.FONT_PATH_BOLD : Scene2dElementFactory.FONT_PATH,
                size,
                INK));
    label.setTouchable(Touchable.disabled);
    return label;
  }

  private void plugged(CablePlugging.Result result) {
    switch (result) {
      case WRONG -> board.feedback(CablePlugging.TEXTS.text("wrong"), false);
      case PLUGGED, PICKED -> board.feedback("", true);
      case CONNECTED -> board.feedback(CablePlugging.TEXTS.text("done"), true);
    }
  }

  /** Asks the server to close the dialog, like the close shortcut does. */
  @Override
  public void requestClose() {
    area().fireServerEvent(DialogContextKeys.ON_CLOSE);
  }

  // Every opening starts with the puzzle as the server sent it; nothing goes to the canvas store.
  @Override
  public void flush() {}

  /** The panel behind plugs and sockets: the help on top, the duct, the cables and the feedback. */
  private final class Board extends CanvasNode {
    private final String help;
    private Label helpLabel;
    private Label feedbackLabel;
    private String feedback = "";
    private boolean good;

    private Board(String help) {
      super("board", WIDTH, 400);
      this.help = help;
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    private void feedback(String text, boolean good) {
      feedback = text;
      this.good = good;
      if (feedbackLabel != null) feedbackLabel.setText(text);
    }

    @Override
    protected void buildContent() {
      helpLabel = inkLabel(help, 18, false);
      helpLabel.setWrap(true);
      helpLabel.setAlignment(Align.topLeft);
      addActor(helpLabel);
      feedbackLabel = inkLabel(feedback, 18, true);
      feedbackLabel.setAlignment(Align.center);
      addActor(feedbackLabel);
    }

    @Override
    protected void layoutContent() {
      helpLabel.setBounds(DUCT_X, height() - TOP + 10, width() - 2 * DUCT_X, TOP - 24);
      feedbackLabel.setBounds(DUCT_X, 10, width() - 2 * DUCT_X, BOTTOM - 20);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      CanvasGraphics.fill(batch, BOARD, alpha, x, y, width(), height());
      CanvasGraphics.outline(batch, RIM, alpha, x, y, width(), height(), 3);
      CanvasGraphics.fill(
          batch, RIM, alpha, x + DUCT_X, y + BOTTOM, DUCT_WIDTH, height() - TOP - BOTTOM);
      if (!feedback.isEmpty()) {
        Color tint = good ? GOOD : BAD;
        CanvasGraphics.fill(
            batch, tint, alpha * .35f, x + DUCT_X, y + 10, width() - 2 * DUCT_X, BOTTOM - 20);
      }
      // The cables run behind all plugs and sockets, so they never cover a label.
      for (Plug plug : plugs) {
        CablePlugging.drawCable(
            batch,
            plug.cableColor(),
            INK,
            alpha,
            plug.anchorX(),
            plug.anchorY(),
            plug.x(),
            plug.y() + PLUG_HEIGHT / 2);
      }
    }
  }

  /** A socket with a jack, its label and a lamp that lights up once its cable is in. */
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
      heading = inkLabel(label(), 16, true);
      heading.setWrap(true);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(54, 6, width() - 92, height() - 12);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      boolean lit = lit();
      CanvasGraphics.fill(batch, SOCKET, alpha, x, y, width(), height());
      // The jack takes the color of the cable once the cable is in, so it gives nothing away.
      CanvasGraphics.fill(batch, lit ? cableColor() : RIM, alpha, x + 10, y + 7, 32, 32);
      CanvasGraphics.fill(batch, INK, alpha, x + 18, y + 15, 16, 16);
      CanvasGraphics.fill(batch, TAG, alpha, x + 50, y + 6, width() - 84, height() - 12);
      CanvasGraphics.fill(batch, lit ? GOOD : LAMP_OFF, alpha, x + width() - 26, y + 17, 12, 12);
      if (lit) {
        CanvasGraphics.outline(batch, GOOD, alpha, x, y, width(), height(), 3);
      } else if (over() || waiting()) {
        CanvasGraphics.outline(batch, CHOSEN, alpha, x, y, width(), height(), over() ? 4 : 2);
      } else {
        CanvasGraphics.outline(batch, RIM, alpha, x, y, width(), height(), 1);
      }
    }
  }

  /** A loose cable end in the color of its cable, with its label on a tag. */
  private final class Plug extends CablePlugging.Plug {
    private Label heading;

    private Plug(int cable) {
      super(plugging, cable, PLUG_WIDTH, PLUG_HEIGHT);
    }

    @Override
    protected void buildContent() {
      heading = inkLabel(label(), 16, true);
      heading.setAlignment(Align.center);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(10, 6, width() - TIP - 20, height() - 12);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float body = width() - TIP;
      if (dragging()) {
        CanvasGraphics.fill(batch, Color.BLACK, alpha * .3f, x + 5, y - 5, body, height());
      }
      CanvasGraphics.fill(batch, TIP_METAL, alpha, x + body, y + height() / 2 - 6, TIP, 12);
      CanvasGraphics.fill(batch, cableColor(), alpha, x, y, body, height());
      CanvasGraphics.fill(
          batch, chosen() ? CHOSEN : TAG, alpha, x + 6, y + 5, body - 12, height() - 10);
      CanvasGraphics.outline(
          batch, INK, alpha, x, y, body, height(), chosen() || dragging() ? 2 : 1);
    }
  }
}
