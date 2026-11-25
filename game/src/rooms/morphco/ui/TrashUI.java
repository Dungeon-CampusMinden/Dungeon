package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
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
import engine.sound.Sounds;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * The waste paper basket in F-04.
 *
 * <p>Crumpled production slips fill the basket. Each one is a reject of the inflection machine and
 * shows its faulty word form while the pointer rests on it or it is dragged. A click tosses a slip
 * out, a drag pulls it aside. Below the top slips lies a folded sheet; a click unfolds the defect
 * report and tells the server that it has been found. Once found, the basket opens with the report
 * already unfolded. Without a mouse, tab and the left and right arrow keys pick a slip or the
 * uncovered sheet, and space or enter tosses or unfolds it. With the pad, A tosses the slip under
 * the cursor at once.
 */
final class TrashUI extends HeapUI {

  /** Rejects on the slips; German in every language, like the words on the conveyor belt. */
  private static final List<String> REJECTS = List.of("verunter", "größsts", "am vermutlichsten");

  private static final float FOLDED_WIDTH = 200;
  private static final float FOLDED_HEIGHT = 110;
  private static final float SHEET_WIDTH = 580;
  private static final float SHEET_HEIGHT = 270;
  private static final float BAND = 36;
  private static final float UNFOLD_SECONDS = .45f;

  /** The top slips that cover the folded sheet. */
  private static final int COVER = 3;

  // Slips in the basket from bottom to top; the last three cover the folded sheet.
  private static final List<Spot> PILE =
      List.of(
          new Spot(-170, 95, 130),
          new Spot(0, 95, 135),
          new Spot(170, 90, 125),
          new Spot(-165, -5, 135),
          new Spot(165, 5, 130),
          new Spot(-170, -95, 125),
          new Spot(5, -95, 135),
          new Spot(170, -100, 130),
          new Spot(0, 0, 140),
          new Spot(-60, 0, 130),
          new Spot(60, 0, 130));

  private final Sheet sheet = lay(new Sheet());
  private final List<Slip> slips = new ArrayList<>();
  private final Set<String> seen = new HashSet<>();

  private TrashUI(DialogContext context, boolean found) {
    super(
        context,
        MorphCoText.text("world.f04.trash-title"),
        MorphCoText.text("world.f04.trash-instruction"),
        MorphCoText.text("world.f04.trash-help"),
        new Basket());
    for (int index = 0; index < PILE.size(); index++) {
      Spot spot = PILE.get(index);
      // Every slip carries a reject; the three on top carry three different ones.
      Slip slip = lay(new Slip(index, spot.size(), REJECTS.get(index % REJECTS.size())));
      place(slip, spot, 8);
      slips.add(slip);
    }
    if (found) {
      // Nothing left to dig for: the report lies open and the slips stay where they are.
      slips.forEach(slip -> slip.setTouchable(Touchable.disabled));
      sheet.lieOpen();
      seen.addAll(REJECTS);
      showRejects();
    } else {
      sheet.position((BED_WIDTH - FOLDED_WIDTH) / 2, (BED_HEIGHT - FOLDED_HEIGHT) / 2);
    }
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context with {@link MorphCoDialogs#COMPLAINT_FOUND}
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new TrashUI(
        context, context.find(MorphCoDialogs.COMPLAINT_FOUND, Boolean.class).orElse(false));
  }

  private void showRejects() {
    String words = REJECTS.stream().filter(seen::contains).collect(Collectors.joining(", "));
    feedback(MorphCoText.text("world.f04.trash-rejects", words), true);
  }

  // The sheet once no top slip lies on it, then the slips from the top.
  @Override
  protected List<CanvasNode> choices() {
    List<CanvasNode> choices = new ArrayList<>();
    boolean covered =
        slips.subList(PILE.size() - COVER, PILE.size()).stream()
            .anyMatch(slip -> !slip.gone() && slip.bounds().overlaps(sheet.bounds()));
    if (!sheet.open && !covered) choices.add(sheet);
    for (Slip slip : slips.reversed()) {
      if (!slip.gone() && slip.isTouchable()) choices.add(slip);
    }
    return choices;
  }

  @Override
  protected void open(CanvasNode choice) {
    sheet.unfold();
  }

  /** The basket seen from above: a steel rim around a wire mesh. */
  private static final class Basket extends CanvasNode {

    private Basket() {
      super("basket", BED_WIDTH, BED_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {}

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float inner = width() - 2 * RIM;
      float depth = height() - 2 * RIM;
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + RIM, y + RIM, inner, depth);
      for (float line = x + RIM + 18; line < x + RIM + inner; line += 26)
        CanvasGraphics.fill(batch, MorphCoUI.GROOVE, alpha, line, y + RIM, 2, depth);
      for (float line = y + RIM + 18; line < y + RIM + depth; line += 26)
        CanvasGraphics.fill(batch, MorphCoUI.GROOVE, alpha, x + RIM, line, inner, 2);
      // The rim casts a shadow into the basket.
      CanvasGraphics.fill(
          batch, Color.BLACK, alpha * .35f, x + RIM, y + RIM + depth - 14, inner, 14);
      MorphCoUI.hazard(batch, alpha, x + 40, y + height() - 12, width() - 80, 6);
    }
  }

  /** A crumpled production slip with a reject of the machine on it. */
  private final class Slip extends TossNode {
    private final String reject;
    private final Color tint = Color.WHITE.cpy().lerp(Color.BLACK, MathUtils.random(.25f));
    private Table tag;

    private Slip(int index, float size, String reject) {
      super("slip" + index, size, index + 2, "images/crumbled-paper.png");
      this.reject = reject;
      grabbable();
    }

    @Override
    protected void buildContent() {
      tag = addTag(reject);
    }

    @Override
    protected void layoutContent() {
      tag.setPosition((width() - tag.getWidth()) / 2, (height() - tag.getHeight()) / 2);
    }

    // The word shows while the pointer rests on the slip, while it is dragged or picked by key.
    @Override
    public void act(float delta) {
      super.act(delta);
      if (tag == null) return;
      boolean shown = over() || dragging() || picked();
      fadeTag(tag, shown, delta);
      if (shown && seen.add(reject)) showRejects();
    }

    @Override
    Color tint() {
      return tint;
    }

    @Override
    void use() {
      toss();
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      toss();
    }
  }

  /** The defect report, folded at the bottom of the basket until the player opens it. */
  private final class Sheet extends CanvasNode {
    private final Table content = new Table();
    private boolean open;

    private Sheet() {
      super("sheet", FOLDED_WIDTH, FOLDED_HEIGHT);
      z(1);
      movable(false);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.INTERACT);
      // The listener keeps the canvas from raising the sheet above the slips on a press.
      addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (!open) unfold();
            }
          });
    }

    // The open sheet lies on top, in the middle of the basket.
    private void lieOpen() {
      open = true;
      setUserObject(Cursors.DEFAULT);
      size(SHEET_WIDTH, SHEET_HEIGHT);
      position(
          bed().x() + (BED_WIDTH - SHEET_WIDTH) / 2, bed().y() + (BED_HEIGHT - SHEET_HEIGHT) / 2);
      area().bringToFront(this);
    }

    // Grows from the folded slip to where lieOpen() puts the sheet, then the text appears.
    private void unfold() {
      submit("complaint");
      Sounds.playUi(MorphCoSounds.RUSTLE, .75f);
      float foldedX = x();
      float foldedY = y();
      lieOpen();
      float openX = x();
      float openY = y();
      size(FOLDED_WIDTH, FOLDED_HEIGHT);
      position(foldedX, foldedY);
      addAction(
          Actions.sequence(
              Actions.parallel(
                  Actions.sizeTo(SHEET_WIDTH, SHEET_HEIGHT, UNFOLD_SECONDS, Interpolation.pow2Out),
                  Actions.moveBy(
                      openX - foldedX, openY - foldedY, UNFOLD_SECONDS, Interpolation.pow2Out)),
              Actions.run(() -> content.addAction(Actions.fadeIn(.25f)))));
    }

    @Override
    protected void buildContent() {
      content.top().left().pad(9, 30, 24, 30);
      content.defaults().left().growX();
      content.add(MorphCoUI.bold("MORPH & CO. KG", 16, MorphCoUI.INK)).padBottom(18).row();
      content
          .add(MorphCoUI.bold(MorphCoText.text("world.f04.trash-report-title"), 26, MorphCoUI.INK))
          .padBottom(22)
          .row();
      content.add(MorphCoUI.label(MorphCoText.text("world.f04.trash-report"), 21, MorphCoUI.INK));
      content.getColor().a = open ? 1 : 0;
      addActor(content);
    }

    @Override
    protected void layoutContent() {
      content.setBounds(0, 0, width(), height());
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float width = width();
      float height = height();
      CanvasGraphics.fill(batch, Color.BLACK, alpha * .35f, x + 6, y - 6, width, height);
      CanvasGraphics.fill(batch, MorphCoUI.PAPER, alpha, x, y, width, height);
      if (open) {
        CanvasGraphics.fill(batch, MorphCoUI.SIGNAL, alpha, x, y + height - BAND, width, BAND);
        // The creases of the two folds.
        CanvasGraphics.fill(batch, MorphCoUI.PAPER_CELL, alpha, x, y + height / 3, width, 2);
        CanvasGraphics.fill(batch, MorphCoUI.PAPER_CELL, alpha, x, y + height * 2 / 3, width, 2);
      } else {
        // The folded flap over the lower part, a strip of the letterhead and print shining through.
        float flap = height * .45f;
        CanvasGraphics.fill(batch, MorphCoUI.PAPER_CELL, alpha, x, y, width, flap);
        CanvasGraphics.fill(batch, MorphCoUI.GHOST, alpha, x, y + flap, width, 2);
        CanvasGraphics.fill(batch, MorphCoUI.SIGNAL, alpha, x, y + height - 10, width, 10);
        for (int line = 0; line < 3; line++) {
          CanvasGraphics.fill(
              batch,
              MorphCoUI.GHOST,
              alpha * .6f,
              x + 16,
              y + height - 26 - line * 11,
              width * (.7f - line * .15f),
              4);
        }
      }
      if (isFocused(this) && !open) {
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x, y, width, height, 3);
      } else {
        CanvasGraphics.outline(batch, MorphCoUI.GHOST, alpha, x, y, width, height, 1);
      }
    }
  }
}
