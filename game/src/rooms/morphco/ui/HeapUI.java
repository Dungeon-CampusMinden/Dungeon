package rooms.morphco.ui;

import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import engine.input.ActionId;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.input.UiActions;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.util.MorphCoSounds;

/**
 * A heap to dig through: the waste paper basket in F-04 and the pile in A-12.
 *
 * <p>Loose pieces lie on a bed in the middle of the canvas. A click tosses a piece aside, a drag
 * pulls it away. Without a mouse, tab and the left and right arrow keys pick one of the {@link
 * #choices()}, and space or enter uses it. With the pad, A tosses the piece under the cursor at
 * once.
 */
abstract class HeapUI extends MorphCoConsoleUI {

  /** Picks the next choice without a mouse. */
  static final ActionId NEXT =
      new ActionId("morphco", "trash_next", key(Keys.TAB), key(Keys.RIGHT));

  /** Picks the previous choice without a mouse. */
  static final ActionId PREVIOUS = new ActionId("morphco", "trash_previous", key(Keys.LEFT));

  /** Uses the picked choice without a mouse. */
  static final ActionId USE =
      new ActionId("morphco", "trash_use", key(Keys.SPACE), key(Keys.ENTER));

  /**
   * Input of the heaps: everything a plain dialog takes, so the pad cursor works, and the keys that
   * pick and use a piece. Left and right take only the pad, since the arrow keys pick a piece.
   */
  static final ContextSpec INPUT =
      UiActions.modal(
              "morphco_trash",
              UiActions.NAV_UP,
              UiActions.NAV_DOWN,
              UiActions.CLICK,
              NEXT,
              PREVIOUS,
              USE)
          .padOnly(UiActions.NAV_LEFT, UiActions.NAV_RIGHT);

  /** Width of the bed the heap lies on. */
  static final float BED_WIDTH = 560;

  /** Height of the bed the heap lies on. */
  static final float BED_HEIGHT = 360;

  /** Width of the bed's steel rim. */
  static final float RIM = 16;

  /** Seconds a tossed piece flies. */
  static final float TOSS_SECONDS = .5f;

  /** Seconds a word on a piece takes to fade in or out. */
  static final float WORD_FADE_SECONDS = .15f;

  private static final float MARGIN = 16;

  private final CanvasNode bed;
  private final List<CanvasNode> lying = new ArrayList<>();
  private float fieldWidth;
  private CanvasNode focused;

  /**
   * Creates the dialog with the bed, but nothing on it yet.
   *
   * @param context dialog context of the server dialog
   * @param title localized title
   * @param instruction localized instruction
   * @param help localized help line, may be empty
   * @param bed the bed, {@link #BED_WIDTH} by {@link #BED_HEIGHT}
   */
  HeapUI(DialogContext context, String title, String instruction, String help, CanvasNode bed) {
    super(context, title, instruction, help);
    this.bed = area().addNode(bed);
    UIUtils.onUiAction(this, NEXT, () -> focusNext(1));
    UIUtils.onUiAction(this, PREVIOUS, () -> focusNext(-1));
    UIUtils.onUiAction(this, USE, this::useFocused);
  }

  /**
   * Returns what the keys can pick, from the first pick on.
   *
   * @return pieces and other nodes that can be used now
   */
  protected abstract List<CanvasNode> choices();

  /**
   * Uses a choice that is not a {@link TossNode}. The keys pick nothing afterwards.
   *
   * @param choice the picked node
   */
  protected void open(CanvasNode choice) {}

  /**
   * Puts a node onto the bed; it keeps its place on the bed when the canvas changes its size.
   *
   * @param node node in bed coordinates, see {@link #place(CanvasNode, Spot, float)}
   * @param <T> type of the node
   * @return the node
   */
  protected final <T extends CanvasNode> T lay(T node) {
    area().addNode(node);
    lying.add(node);
    return node;
  }

  /**
   * Places a node at a spot, shifted by a random amount so no two heaps look alike.
   *
   * @param node the node
   * @param spot place relative to the bed's center
   * @param jitter largest shift in each direction
   */
  protected static void place(CanvasNode node, Spot spot, float jitter) {
    node.position(
        (BED_WIDTH - spot.size()) / 2 + spot.x() + MathUtils.random(-jitter, jitter),
        (BED_HEIGHT - spot.size()) / 2 + spot.y() + MathUtils.random(-jitter, jitter));
  }

  /**
   * Returns the bed.
   *
   * @return the bed node
   */
  protected final CanvasNode bed() {
    return bed;
  }

  /**
   * Tells whether the keys picked a node.
   *
   * @param node the node
   * @return true if it is the current pick
   */
  protected final boolean isFocused(CanvasNode node) {
    return focused == node;
  }

  @Override
  protected final float arrange(float width, float height) {
    fieldWidth = width;
    float needed = MARGIN + BED_HEIGHT + MARGIN;
    float x = Math.round((width - BED_WIDTH) / 2);
    float y = -Math.round((Math.max(needed, height) + BED_HEIGHT) / 2);
    float dx = x - bed.x();
    float dy = y - bed.y();
    bed.position(x, y);
    // The pieces keep their place on the bed, even while they fly.
    for (CanvasNode node : lying) node.position(node.x() + dx, node.y() + dy);
    return needed;
  }

  private void focusNext(int step) {
    List<CanvasNode> choices = choices();
    if (choices.isEmpty()) return;
    int index = choices.indexOf(focused);
    focused =
        index < 0 ? choices.getFirst() : choices.get(Math.floorMod(index + step, choices.size()));
  }

  // After a toss the next choice is picked, so space digs on through the heap.
  private void useFocused() {
    if (!choices().contains(focused)) return;
    if (focused instanceof TossNode piece) {
      piece.use();
      if (piece.gone()) focused = choices().stream().findFirst().orElse(null);
    } else {
      open(focused);
      focused = null;
    }
  }

  /**
   * Place of a piece relative to the bed's center, and its size.
   *
   * @param x horizontal offset of the piece's center
   * @param y vertical offset of the piece's center
   * @param size width and height
   */
  record Spot(float x, float y, float size) {}

  /** A loose piece of the heap that can be tossed off the bed or pulled aside. */
  abstract class TossNode extends CanvasNode {
    private final TextureRegion picture;

    private boolean over;
    private boolean dragging;
    private boolean gone;
    // Kept apart from the actor's rotation and scale, which would also turn and scale its hit area.
    private float spin = MathUtils.random(360f);
    private float zoom = 1;

    /**
     * Creates a piece; it lies above the pieces with a lower z.
     *
     * @param id node ID
     * @param size width and height
     * @param z stacking order
     * @param art path of the picture
     */
    TossNode(String id, float size, int z, String art) {
      super(id, size, size);
      picture = new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(art)));
      z(z);
      selectable(false);
      deletable(false);
      addListener(
          new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              over = true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              over = false;
            }
          });
    }

    /** Lets the mouse grab the piece and the pad use it at once. */
    final void grabbable() {
      setUserObject(Cursors.GRAB);
      addListener(
          new InputListener() {
            // The pad uses the piece at once; holding it to pull it aside is for the mouse.
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (GameInput.device() != InputDevice.PAD) return false;
              use();
              return true;
            }
          });
    }

    /** What a click, the pad or the keys do with the piece. */
    abstract void use();

    /**
     * Tells whether the pointer rests on the piece.
     *
     * @return true while the pointer is over it
     */
    final boolean over() {
      return over;
    }

    /**
     * Tells whether the mouse drags the piece.
     *
     * @return true while it is dragged
     */
    final boolean dragging() {
      return dragging;
    }

    /**
     * Tells whether the piece has left the heap.
     *
     * @return true once it was tossed or taken
     */
    final boolean gone() {
      return gone;
    }

    /** Takes the piece out of the heap; the keys no longer pick it. */
    final void leaveHeap() {
      gone = true;
    }

    /**
     * Turns the piece.
     *
     * @param degrees angle of the picture
     */
    final void spin(float degrees) {
      spin = degrees;
    }

    /**
     * Tells whether the keys picked the piece while it is still in the heap.
     *
     * @return true if picked
     */
    final boolean picked() {
      return isFocused(this) && !gone;
    }

    /**
     * Returns the color the picture is drawn in.
     *
     * @return tint of the picture
     */
    Color tint() {
      return Color.WHITE;
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      Color previous = batch.getColor().cpy();
      // A piece fades as it leaves the heap.
      float shown = alpha * getColor().a;
      // The higher the piece flies, the further its shadow falls.
      float lift = 4 + (zoom - 1) * 50;
      batch.setColor(0, 0, 0, shown * .35f);
      draw(batch, x() + lift, y() - lift);
      Color tint = tint();
      batch.setColor(tint.r, tint.g, tint.b, shown);
      draw(batch, x(), y());
      batch.setColor(previous);
      if (picked())
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x(), y(), width(), height(), 3);
    }

    private void draw(Batch batch, float x, float y) {
      float half = width() / 2;
      batch.draw(picture, x, y, half, half, width(), height(), zoom, zoom, spin);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (!dragging) {
        dragging = true;
        clearActions();
        zoom = 1.1f;
        Sounds.playUi(MorphCoSounds.RUSTLE, MathUtils.random(.9f, 1.15f));
      }
      super.onMove(dx, dy);
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      dragging = false;
      zoom = 1;
    }

    /** Tossed to the nearer side of the bed, the piece lands on the floor next to it. */
    final void toss() {
      leaveHeap();
      clearActions();
      zoom = 1;
      Sounds.playUi(MorphCoSounds.RUSTLE, MathUtils.random(.9f, 1.15f));
      boolean left = centerX() < bed.centerX();
      float size = width();
      float targetX =
          left
              ? MathUtils.random(0, bed.x() - size * .6f)
              : MathUtils.random(bed.x() + BED_WIDTH - size * .4f, fieldWidth - size);
      float targetY = MathUtils.random(bed.y(), bed.y() + BED_HEIGHT - size);
      float from = spin;
      float turn = MathUtils.random(240f, 480f) * (left ? 1 : -1);
      addAction(
          Actions.parallel(
              Actions.moveBy(targetX - x(), targetY - y(), TOSS_SECONDS, Interpolation.pow2Out),
              new TemporalAction(TOSS_SECONDS) {
                @Override
                protected void update(float percent) {
                  spin = from + turn * Interpolation.pow2Out.apply(percent);
                  // Up through the air and down again: the piece grows and shrinks on the way.
                  zoom = 1 + .35f * MathUtils.sin(percent * MathUtils.PI);
                }
              }));
    }
  }
}
