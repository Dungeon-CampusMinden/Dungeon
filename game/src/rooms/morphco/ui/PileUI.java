package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * The pile of odds and ends in A-12.
 *
 * <p>Junk lies on top of the circumfix parts that were still in the pile when it was opened. A
 * click tosses a piece of junk aside, a drag pulls it away. A click on a part asks the server for
 * it; the server answers in {@link #applyFeedback(DialogFeedbackMessage)}, and the part leaves the
 * pile unless the inventory is full. Keys and pad work like in the waste paper basket of F-04: tab
 * and the left and right arrow keys pick a piece of junk or an uncovered part, space or enter uses
 * it, and the pad's A tosses the junk under the cursor at once.
 */
final class PileUI extends MorphCoConsoleUI implements DialogFeedbackReceiver {

  // Junk from the item art; drawn large, every picture shows up more than once.
  private static final List<String> JUNK =
      List.of(
          "items/tools/hammer.png",
          "items/gloves/steel_gloves.png",
          "items/resource/coffee.png",
          "items/rpg/item_paper.png",
          "items/usb-side-red.png",
          "items/rpg/scissors.png",
          "items/resource/steel.png",
          "images/crumbled-paper.png",
          "items/resource/donut.png");

  private static final float TRAY_WIDTH = 560;
  private static final float TRAY_HEIGHT = 360;
  private static final float RIM = 16;
  private static final float MARGIN = 16;
  private static final float PART_SIZE = 80;
  private static final float TOSS_SECONDS = .5f;
  private static final float WORD_FADE_SECONDS = .15f;

  // Places of the parts relative to the tray's center.
  private static final List<Spot> PARTS =
      List.of(
          new Spot(-170, 70, PART_SIZE),
          new Spot(0, 80, PART_SIZE),
          new Spot(170, 65, PART_SIZE),
          new Spot(-165, -75, PART_SIZE),
          new Spot(5, -70, PART_SIZE),
          new Spot(170, -80, PART_SIZE));

  // Junk from bottom to top; every part lies below two pieces, the last three fill the middle.
  private static final List<Spot> HEAP =
      List.of(
          new Spot(-180, 90, 100),
          new Spot(-150, 50, 95),
          new Spot(10, 100, 100),
          new Spot(-20, 60, 90),
          new Spot(180, 85, 100),
          new Spot(150, 45, 95),
          new Spot(-180, -55, 100),
          new Spot(-140, -95, 95),
          new Spot(25, -50, 100),
          new Spot(-15, -90, 95),
          new Spot(185, -60, 100),
          new Spot(150, -100, 95),
          new Spot(-85, 0, 100),
          new Spot(85, 5, 100),
          new Spot(0, 0, 105));

  private final Tray tray = area().addNode(new Tray());
  private final List<Piece> pieces = new ArrayList<>();
  private float fieldWidth;
  private Piece focused;

  private PileUI(DialogContext context, List<CircumfixPuzzle.Fragment> parts) {
    super(
        context,
        MorphCoText.text("world.a12.pile-title"),
        "",
        MorphCoText.text("world.a12.pile-help"));
    for (int index = 0; index < parts.size(); index++) {
      CircumfixPuzzle.Fragment fragment = parts.get(index);
      String art = "rooms/morphCo/art/fragment_" + fragment.text() + ".png";
      place(new Piece("part" + index, 1, PARTS.get(index), art, fragment));
    }
    for (int index = 0; index < HEAP.size(); index++) {
      place(
          new Piece(
              "junk" + index, index + 2, HEAP.get(index), JUNK.get(index % JUNK.size()), null));
    }
    UIUtils.onUiAction(this, TrashUI.NEXT, () -> focusNext(1));
    UIUtils.onUiAction(this, TrashUI.PREVIOUS, () -> focusNext(-1));
    UIUtils.onUiAction(this, TrashUI.USE, this::useFocused);
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context with {@link MorphCoDialogs#PILE_PARTS}
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    String[] names = context.find(MorphCoDialogs.PILE_PARTS, String[].class).orElse(new String[0]);
    return new PileUI(
        context,
        Stream.of(names).flatMap(name -> CircumfixPuzzle.Fragment.parse(name).stream()).toList());
  }

  private void place(Piece piece) {
    Spot spot = piece.spot;
    float jitter = piece.part == null ? 8 : 4;
    piece.position(
        (TRAY_WIDTH - spot.size()) / 2 + spot.x() + MathUtils.random(-jitter, jitter),
        (TRAY_HEIGHT - spot.size()) / 2 + spot.y() + MathUtils.random(-jitter, jitter));
    area().addNode(piece);
    pieces.add(piece);
  }

  @Override
  protected float arrange(float width, float height) {
    fieldWidth = width;
    float needed = MARGIN + TRAY_HEIGHT + MARGIN;
    float x = Math.round((width - TRAY_WIDTH) / 2);
    float y = -Math.round((Math.max(needed, height) + TRAY_HEIGHT) / 2);
    float dx = x - tray.x();
    float dy = y - tray.y();
    tray.position(x, y);
    // The pieces keep their place on the tray, even while they fly.
    for (Piece piece : pieces) piece.position(piece.x() + dx, piece.y() + dy);
    return needed;
  }

  /**
   * Shows the server's answer to a click on a part. The target names the fragment that leaves the
   * pile: into this player's inventory if successful, otherwise because someone else took it first.
   * Without a target the part stays, since the inventory is full.
   *
   * @param feedback the server's answer
   */
  @Override
  public void applyFeedback(DialogFeedbackMessage feedback) {
    String target = feedback.targetTabKey();
    pieces.stream()
        .filter(piece -> !piece.gone && piece.part != null && piece.part.name().equals(target))
        .min(Comparator.comparing(piece -> !piece.asked))
        .ifPresent(Piece::leave);
    pieces.forEach(piece -> piece.asked = false);
    feedback(
        Game.localization().getCurrentTranslator().translate(feedback.messageKey()),
        feedback.successful());
  }

  // What the keys can pick: the junk from the top, then the parts no junk lies on.
  private List<Piece> choices() {
    List<Piece> choices = new ArrayList<>();
    for (Piece piece : pieces.reversed()) {
      if (piece.gone) continue;
      boolean covered =
          piece.part != null
              && pieces.stream()
                  .anyMatch(junk ->
                      junk.part == null && !junk.gone && junk.bounds().overlaps(piece.bounds()));
      if (!covered) choices.add(piece);
    }
    return choices;
  }

  private void focusNext(int step) {
    List<Piece> choices = choices();
    if (choices.isEmpty()) return;
    int index = choices.indexOf(focused);
    focused =
        index < 0 ? choices.getFirst() : choices.get(Math.floorMod(index + step, choices.size()));
  }

  // After a toss the next choice is picked, so space digs on through the pile.
  private void useFocused() {
    if (focused == null || !choices().contains(focused)) return;
    focused.use();
    if (focused.gone) focused = choices().stream().findFirst().orElse(null);
  }

  // Place of a piece relative to the tray's center, and its size.
  private record Spot(float x, float y, float size) {}

  /** The floor of A-12 under the pile: a steel plate with a hazard stripe. */
  private static final class Tray extends CanvasNode {

    private Tray() {
      super("tray", TRAY_WIDTH, TRAY_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {}

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x(), y(), width(), height());
      CanvasGraphics.fill(
          batch,
          MorphCoUI.INSET,
          alpha,
          x() + RIM,
          y() + RIM,
          width() - 2 * RIM,
          height() - 2 * RIM);
      MorphCoUI.hazard(batch, alpha, x() + 40, y() + height() - 12, width() - 80, 6);
    }
  }

  /** A piece of junk, or a circumfix part that the player can take. */
  private final class Piece extends CanvasNode {
    private final Spot spot;
    private final TextureRegion picture;
    private final CircumfixPuzzle.Fragment part;
    private Table tag;
    private boolean over;
    private boolean dragging;
    private boolean gone;
    private boolean asked;
    // Kept apart from the actor's rotation and scale, which would also turn and scale its hit area.
    private float spin;
    private float zoom = 1;

    private Piece(String id, int z, Spot spot, String art, CircumfixPuzzle.Fragment part) {
      super(id, spot.size(), spot.size());
      this.spot = spot;
      this.part = part;
      picture = new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(art)));
      spin = part == null ? MathUtils.random(360f) : MathUtils.random(-15f, 15f);
      z(z);
      selectable(false);
      deletable(false);
      if (part != null) {
        movable(false);
        setUserObject(Cursors.INTERACT);
        // The listener keeps the canvas from raising the part above the junk on a press.
        addListener(
            new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                use();
              }
            });
      } else {
        setUserObject(Cursors.GRAB);
        addListener(
            new InputListener() {
              // The pad tosses at once; holding junk to pull it aside is for the mouse.
              @Override
              public boolean touchDown(
                  InputEvent event, float x, float y, int pointer, int button) {
                if (GameInput.device() != InputDevice.PAD) return false;
                use();
                return true;
              }
            });
      }
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

    @Override
    protected void buildContent() {
      if (part == null) return;
      tag = MorphCoUI.tag(part.label());
      tag.pack();
      tag.getColor().a = 0;
      tag.setTouchable(Touchable.disabled);
      addActor(tag);
    }

    @Override
    protected void layoutContent() {
      if (tag != null) tag.setPosition((width() - tag.getWidth()) / 2, -tag.getHeight() / 2);
    }

    // A part shows its label while the pointer rests on it or it is picked by key.
    @Override
    public void act(float delta) {
      super.act(delta);
      if (tag == null) return;
      boolean shown = !gone && (over || focused == this);
      float fade = (shown ? delta : -delta) / WORD_FADE_SECONDS;
      tag.getColor().a = MathUtils.clamp(tag.getColor().a + fade, 0, 1);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      Color previous = batch.getColor().cpy();
      // A part fades as it leaves the pile.
      float shown = alpha * getColor().a;
      // The higher the piece flies, the further its shadow falls.
      float lift = 4 + (zoom - 1) * 50;
      batch.setColor(0, 0, 0, shown * .35f);
      draw(batch, x() + lift, y() - lift);
      batch.setColor(1, 1, 1, shown);
      draw(batch, x(), y());
      batch.setColor(previous);
      if (focused == this && !gone)
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

    @Override
    public void onClick(float localX, float localY, int button) {
      if (part == null) use();
    }

    // Junk flies off the tray; a part is asked for, once until the server has answered.
    private void use() {
      if (gone) return;
      if (part == null) {
        toss();
      } else if (!asked) {
        asked = true;
        submit(part.name());
      }
    }

    // Tossed to the nearer side of the tray, the junk lands on the floor next to it.
    private void toss() {
      gone = true;
      clearActions();
      zoom = 1;
      Sounds.playUi(MorphCoSounds.RUSTLE, MathUtils.random(.9f, 1.15f));
      boolean left = centerX() < tray.centerX();
      float size = width();
      float targetX =
          left
              ? MathUtils.random(0, tray.x() - size * .6f)
              : MathUtils.random(tray.x() + TRAY_WIDTH - size * .4f, fieldWidth - size);
      float targetY = MathUtils.random(tray.y(), tray.y() + TRAY_HEIGHT - size);
      float from = spin;
      float turn = MathUtils.random(240f, 480f) * (left ? 1 : -1);
      addAction(
          Actions.parallel(
              Actions.moveBy(targetX - x(), targetY - y(), TOSS_SECONDS, Interpolation.pow2Out),
              new TemporalAction(TOSS_SECONDS) {
                @Override
                protected void update(float percent) {
                  spin = from + turn * Interpolation.pow2Out.apply(percent);
                  // Up through the air and down again: the junk grows and shrinks on the way.
                  zoom = 1 + .35f * MathUtils.sin(percent * MathUtils.PI);
                }
              }));
    }

    // The part rises out of the pile and fades.
    private void leave() {
      gone = true;
      setTouchable(Touchable.disabled);
      addAction(
          Actions.parallel(
              Actions.moveBy(0, 40, TOSS_SECONDS, Interpolation.pow2Out),
              Actions.fadeOut(TOSS_SECONDS)));
    }
  }
}
