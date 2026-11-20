package rooms.morphco.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import rooms.morphco.puzzles.CircumfixPuzzle;
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
final class PileUI extends HeapUI implements DialogFeedbackReceiver {

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

  private static final float PART_SIZE = 80;

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

  private final List<Piece> pieces = new ArrayList<>();

  private PileUI(DialogContext context, List<CircumfixPuzzle.Fragment> parts) {
    super(
        context,
        MorphCoText.text("world.a12.pile-title"),
        MorphCoText.text("world.a12.pile-instruction"),
        MorphCoText.text("world.a12.pile-help"),
        new Tray());
    for (int index = 0; index < parts.size(); index++) {
      CircumfixPuzzle.Fragment fragment = parts.get(index);
      String art = "rooms/morphCo/art/fragment_" + fragment.text() + ".png";
      Piece piece = lay(new Piece("part" + index, 1, PART_SIZE, art, fragment));
      place(piece, PARTS.get(index), 4);
      pieces.add(piece);
    }
    for (int index = 0; index < HEAP.size(); index++) {
      Spot spot = HEAP.get(index);
      Piece piece =
          lay(
              new Piece(
                  "junk" + index, index + 2, spot.size(), JUNK.get(index % JUNK.size()), null));
      place(piece, spot, 8);
      pieces.add(piece);
    }
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
        .filter(piece -> !piece.gone() && piece.part != null && piece.part.name().equals(target))
        .min(Comparator.comparing(piece -> !piece.asked))
        .ifPresent(Piece::leave);
    pieces.forEach(piece -> piece.asked = false);
    feedback(
        Game.localization().getCurrentTranslator().translate(feedback.messageKey()),
        feedback.successful());
  }

  // The junk from the top, then the parts no junk lies on.
  @Override
  protected List<CanvasNode> choices() {
    List<CanvasNode> choices = new ArrayList<>();
    for (Piece piece : pieces.reversed()) {
      if (piece.gone()) continue;
      boolean covered =
          piece.part != null
              && pieces.stream()
                  .anyMatch(junk ->
                      junk.part == null && !junk.gone() && junk.bounds().overlaps(piece.bounds()));
      if (!covered) choices.add(piece);
    }
    return choices;
  }

  /** The floor of A-12 under the pile: a steel plate with a hazard stripe. */
  private static final class Tray extends CanvasNode {

    private Tray() {
      super("tray", BED_WIDTH, BED_HEIGHT);
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
  private final class Piece extends TossNode {
    private final CircumfixPuzzle.Fragment part;
    private Table tag;
    private boolean asked;

    private Piece(String id, int z, float size, String art, CircumfixPuzzle.Fragment part) {
      super(id, size, z, art);
      this.part = part;
      if (part != null) {
        spin(MathUtils.random(-15f, 15f));
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
        grabbable();
      }
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
      boolean shown = !gone() && (over() || isFocused(this));
      float fade = (shown ? delta : -delta) / WORD_FADE_SECONDS;
      tag.getColor().a = MathUtils.clamp(tag.getColor().a + fade, 0, 1);
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      if (part == null) use();
    }

    // Junk flies off the tray; a part is asked for, once until the server has answered.
    @Override
    void use() {
      if (gone()) return;
      if (part == null) {
        toss();
      } else if (!asked) {
        asked = true;
        submit(part.name());
      }
    }

    // The part rises out of the pile and fades.
    private void leave() {
      leaveHeap();
      setTouchable(Touchable.disabled);
      addAction(
          Actions.parallel(
              Actions.moveBy(0, 40, TOSS_SECONDS, Interpolation.pow2Out),
              Actions.fadeOut(TOSS_SECONDS)));
    }
  }
}
