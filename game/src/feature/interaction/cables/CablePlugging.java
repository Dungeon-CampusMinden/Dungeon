package feature.interaction.cables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.Cursors;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * The plugging in an opened cable dialog, shared by the {@link CablesUI} and the dialogs a room
 * registers in its own look for {@link feature.hud.dialogs.DialogType.DefaultTypes#CABLES}.
 *
 * <p>Every cable has a {@link Plug} and every socket a {@link Socket} on the dialog's canvas; the
 * dialog subclasses both to draw them. The player drags a plug onto a socket, or clicks the plug
 * and then the socket. The right socket takes the cable, any other one sends it back. Once every
 * cable is in, the wiring goes to the server, which checks it again, see {@link Cables}.
 */
public final class CablePlugging {

  /** What the player did with a plug. */
  public enum Result {
    /** The socket belongs to another cable, the plug springs back. */
    WRONG,
    /** The cable sits in its socket, others still hang loose. */
    PLUGGED,
    /** The last cable went in and the wiring went to the server. */
    CONNECTED,
    /** The player picked up a loose plug or chose it for a click on a socket. */
    PICKED
  }

  /** Default texts of the cable dialog. */
  static final Translation TEXTS = new Translation("dialog.cables_dialog");

  private static final int CABLE_PIECES = 16;

  private final CablePuzzle puzzle;
  private final Consumer<Result> onPlug;
  private final Plug[] plugs;
  private final Socket[] sockets;
  // The socket each cable sits in, -1 while it hangs loose.
  private final int[] wiring;
  // The cable that belongs in each socket.
  private final int[] owners;
  private int selected = -1;

  /**
   * Creates the plugging for a dialog opened by {@link Cables#show}. The plugs and sockets join it
   * when they are created.
   *
   * @param context dialog context with the puzzle
   * @param onPlug shows what the player did with a plug, e.g. as feedback text
   */
  public CablePlugging(DialogContext context, Consumer<Result> onPlug) {
    puzzle = CablePuzzle.from(context);
    this.onPlug = onPlug;
    int count = puzzle.cables().size();
    plugs = new Plug[count];
    sockets = new Socket[count];
    wiring = new int[count];
    owners = new int[count];
    boolean connected = context.find(Cables.CONNECTED, Boolean.class).orElse(false);
    for (int cable = 0; cable < count; cable++) {
      int socket = puzzle.cables().get(cable).socket();
      wiring[cable] = connected ? socket : -1;
      owners[socket] = cable;
    }
  }

  /**
   * Returns the title of a cable dialog.
   *
   * @param context dialog context of {@link Cables#show}
   * @return the translated title, or the default title if the context has none
   */
  public static String title(DialogContext context) {
    return text(context, DialogContextKeys.TITLE, "title");
  }

  /**
   * Returns the help of a cable dialog that tells how to plug the cables.
   *
   * @param context dialog context of {@link Cables#show}
   * @return the translated help, or the default help if the context has none
   */
  public static String help(DialogContext context) {
    return text(context, DialogContextKeys.DESCRIPTION, "help");
  }

  private static String text(DialogContext context, String key, String fallback) {
    String text = context.find(key, String.class).orElse("");
    return text.isBlank() ? TEXTS.text(fallback) : localize(text);
  }

  /**
   * Returns the cables and sockets.
   *
   * @return the puzzle
   */
  public CablePuzzle puzzle() {
    return puzzle;
  }

  /**
   * Returns whether every cable sits in its socket.
   *
   * @return true once the puzzle is solved
   */
  public boolean allPlugged() {
    return Arrays.stream(wiring).allMatch(socket -> socket >= 0);
  }

  /**
   * Draws a cable that sags between two points, as short straight pieces with a dark edge. Draw the
   * cables before the plugs and sockets, so they never cover a label.
   *
   * @param batch the batch to draw with
   * @param color color of the cable
   * @param edge color of its edge
   * @param alpha the inherited alpha
   * @param x1 x where the cable starts
   * @param y1 y where the cable starts
   * @param x2 x of the plug
   * @param y2 y of the plug
   */
  public static void drawCable(
      Batch batch, Color color, Color edge, float alpha, float x1, float y1, float x2, float y2) {
    float sag = Math.min(60, Vector2.dst(x1, y1, x2, y2) / 4);
    float middleX = (x1 + x2) / 2;
    float middleY = Math.min(y1, y2) - sag;
    for (int pass = 0; pass < 2; pass++) {
      Color tint = pass == 0 ? edge : color;
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

  // Labels and texts may be translation keys of the room.
  private static String localize(String text) {
    return Game.localization().getCurrentTranslator().translate(text);
  }

  private boolean plugged(int cable) {
    return wiring[cable] >= 0;
  }

  // Plugs a cable into a socket; the socket of another cable sends it back.
  private void plug(int cable, Socket socket) {
    selected = -1;
    Plug plug = plugs[cable];
    if (!puzzle.fits(cable, socket.index)) {
      plug.bounce(socket);
      Sounds.playUi(CoreSounds.INTERFACE_BUTTON_BACKWARD);
      onPlug.accept(Result.WRONG);
      socket
          .canvas()
          .fireServerEvent(
              Cables.WRONG_PLUG,
              new DialogResponseMessage.IntList(new int[] {cable, socket.index}));
      return;
    }
    wiring[cable] = socket.index;
    plug.settle();
    Sounds.playUi(CoreSounds.SETTINGS_TOGGLE_CLICK);
    if (!allPlugged()) {
      onPlug.accept(Result.PLUGGED);
      return;
    }
    onPlug.accept(Result.CONNECTED);
    socket
        .canvas()
        .fireServerEvent(Cables.CONNECT, new DialogResponseMessage.IntList(wiring.clone()));
  }

  /**
   * A loose cable end. The dialog draws it and, behind all plugs and sockets, its cable from {@link
   * #anchorX()}, {@link #anchorY()} to wherever the plug is; see {@link #drawCable}.
   */
  public abstract static class Plug extends CanvasNode {
    private final CablePlugging plugging;
    private final int cable;
    private boolean dragging;
    private float anchorX;
    private float anchorY;
    private float restX;
    private float restY;

    /**
     * Creates the plug of a cable and adds it to the plugging.
     *
     * @param plugging the plugging of the dialog
     * @param cable index of the cable
     * @param width width of the plug
     * @param height height of the plug
     */
    protected Plug(CablePlugging plugging, int cable, float width, float height) {
      super("plug-" + cable, width, height);
      this.plugging = plugging;
      this.cable = cable;
      plugging.plugs[cable] = this;
      z(2);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.GRAB);
    }

    /**
     * Places the plug: it hangs at its rest position unless it sits in its socket.
     *
     * @param anchorX x where its cable comes out of the duct
     * @param anchorY y where its cable comes out of the duct
     * @param restX x of the corner of the loose plug
     * @param restY y of the corner of the loose plug
     */
    public void hang(float anchorX, float anchorY, float restX, float restY) {
      this.anchorX = anchorX;
      this.anchorY = anchorY;
      this.restX = restX;
      this.restY = restY;
      settle();
    }

    /**
     * Returns where the cable comes out of the duct.
     *
     * @return x of the cable's other end
     */
    public float anchorX() {
      return anchorX;
    }

    /**
     * Returns where the cable comes out of the duct.
     *
     * @return y of the cable's other end
     */
    public float anchorY() {
      return anchorY;
    }

    /**
     * Returns the label of the cable, translated.
     *
     * @return text for the plug
     */
    protected String label() {
      return localize(plugging.puzzle.cables().get(cable).label());
    }

    /**
     * Returns the color of the cable.
     *
     * @return color of cable and plug
     */
    public Color cableColor() {
      return plugging.puzzle.cables().get(cable).color();
    }

    /**
     * Returns whether the player drags the plug.
     *
     * @return true while dragged
     */
    protected boolean dragging() {
      return dragging;
    }

    /**
     * Returns whether the player clicked the plug and the next click on a socket plugs it in.
     *
     * @return true while chosen
     */
    protected boolean chosen() {
      return plugging.selected == cable;
    }

    // Puts the plug into its socket or back to where it hangs.
    private void settle() {
      clearActions();
      dragging = false;
      if (plugging.plugged(cable)) {
        Socket socket = plugging.sockets[plugging.wiring[cable]];
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
    public void act(float delta) {
      super.act(delta);
      if (plugging.plugged(cable)) setUserObject(Cursors.DEFAULT);
      else setUserObject(dragging ? Cursors.GRABBING : Cursors.GRAB);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (plugging.plugged(cable)) return;
      if (!dragging) {
        clearActions();
        dragging = true;
        plugging.selected = -1;
        plugging.onPlug.accept(Result.PICKED);
      }
      super.onMove(dx, dy);
    }

    // Let go anywhere but on a socket, the plug drops back to where it hangs.
    @Override
    public void onDrop(float worldX, float worldY) {
      if (plugging.plugged(cable)) return;
      dragging = false;
      addAction(Actions.moveTo(restX, restY, 0.3f, Interpolation.pow2Out));
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      if (button != 0 || plugging.plugged(cable)) return;
      plugging.selected = chosen() ? -1 : cable;
      if (chosen()) plugging.onPlug.accept(Result.PICKED);
    }
  }

  /** A socket; it takes the plug of its own cable, dropped onto it or chosen before a click. */
  public abstract static class Socket extends CanvasNode {
    private final CablePlugging plugging;
    private final int index;
    private boolean over;

    /**
     * Creates a socket and adds it to the plugging.
     *
     * @param plugging the plugging of the dialog
     * @param index index of the socket
     * @param width width of the socket
     * @param height height of the socket
     */
    protected Socket(CablePlugging plugging, int index, float width, float height) {
      super("socket-" + index, width, height);
      this.plugging = plugging;
      this.index = index;
      plugging.sockets[index] = this;
      z(1);
      movable(false);
      selectable(false);
      deletable(false);
      // Sockets handle their clicks themselves: the canvas would raise them above the plugs.
      addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (plugging.selected >= 0) plugging.plug(plugging.selected, Socket.this);
            }
          });
    }

    /**
     * Returns where a plug sits whose tip is in this socket.
     *
     * @return x of the corner of the plug
     */
    protected abstract float plugX();

    /**
     * Returns where a plug sits whose tip is in this socket.
     *
     * @return y of the corner of the plug
     */
    protected abstract float plugY();

    /**
     * Returns the label of the socket, translated.
     *
     * @return text for the socket
     */
    protected String label() {
      return localize(plugging.puzzle.sockets().get(index));
    }

    /**
     * Returns the color of the cable that belongs in this socket.
     *
     * @return color of its cable
     */
    protected Color cableColor() {
      return plugging.puzzle.cables().get(plugging.owners[index]).color();
    }

    /**
     * Returns whether the socket holds its cable.
     *
     * @return true once its cable is plugged in
     */
    protected boolean lit() {
      return plugging.plugged(plugging.owners[index]);
    }

    /**
     * Returns whether a plug is dragged over the socket that it would take.
     *
     * @return true while a loose plug hovers over it
     */
    protected boolean over() {
      return over;
    }

    /**
     * Returns whether the player chose a plug, so a click on the socket plugs it in.
     *
     * @return true while a plug is chosen
     */
    protected boolean waiting() {
      return plugging.selected >= 0;
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      setUserObject(waiting() ? Cursors.INTERACT : Cursors.DEFAULT);
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
          && !plugging.plugged(plug.cable)
          && context.localX() >= 0
          && context.localX() < width()
          && context.localY() >= 0
          && context.localY() < height();
    }

    @Override
    public boolean onNodeDropped(CanvasDragContext context) {
      over = false;
      if (!accepts(context)) return false;
      plugging.plug(((Plug) context.draggedNode()).cable, this);
      return true;
    }
  }
}
