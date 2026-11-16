package rooms.morphco.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
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
 * already unfolded. Without a mouse, tab and the arrow keys pick a slip or the uncovered sheet, and
 * space or enter tosses or unfolds it. With the pad, A tosses the slip under the cursor at once.
 */
final class TrashUI extends MorphCoConsoleUI {

  /** Rejects on the slips; German in every language, like the words on the conveyor belt. */
  private static final List<String> REJECTS = List.of("verunter", "größsts", "am vermutlichsten");

  private static final float BASKET_WIDTH = 560;
  private static final float BASKET_HEIGHT = 360;
  private static final float RIM = 16;
  private static final float MARGIN = 16;
  private static final float FOLDED_WIDTH = 200;
  private static final float FOLDED_HEIGHT = 110;
  private static final float SHEET_WIDTH = 580;
  private static final float SHEET_HEIGHT = 270;
  private static final float BAND = 36;
  private static final float TOSS_SECONDS = .5f;
  private static final float UNFOLD_SECONDS = .45f;
  private static final float WORD_FADE_SECONDS = .15f;

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

  private final TextureRegion crumpled =
      new TextureRegion(
          TextureMap.instance().textureAt(new SimpleIPath("images/crumbled-paper.png")));
  private final Basket basket = area().addNode(new Basket());
  private final Sheet sheet = area().addNode(new Sheet());
  private final List<Slip> slips = new ArrayList<>();
  private final Set<String> seen = new HashSet<>();
  private float fieldWidth;
  private CanvasNode focused;

  private TrashUI(DialogContext context, boolean found) {
    super(
        context,
        MorphCoText.text("world.f04.trash-title"),
        MorphCoText.text("world.f04.trash-instruction"),
        found ? "" : MorphCoText.text("world.f04.trash-help"));
    for (int index = 0; index < PILE.size(); index++) {
      Spot spot = PILE.get(index);
      // Every slip carries a reject; the three on top carry three different ones.
      Slip slip = area().addNode(new Slip(index, spot.size(), REJECTS.get(index % REJECTS.size())));
      float jitterX = MathUtils.random(-8f, 8f);
      float jitterY = MathUtils.random(-8f, 8f);
      slip.position(
          (BASKET_WIDTH - spot.size()) / 2 + spot.x() + jitterX,
          (BASKET_HEIGHT - spot.size()) / 2 + spot.y() + jitterY);
      slips.add(slip);
    }
    if (found) {
      // Nothing left to dig for: the report lies open and the slips stay where they are.
      slips.forEach(slip -> slip.setTouchable(Touchable.disabled));
      sheet.lieOpen();
      seen.addAll(REJECTS);
      showRejects();
    } else {
      sheet.position((BASKET_WIDTH - FOLDED_WIDTH) / 2, (BASKET_HEIGHT - FOLDED_HEIGHT) / 2);
    }
    addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            switch (keycode) {
              case Input.Keys.TAB, Input.Keys.RIGHT -> focusNext(1);
              case Input.Keys.LEFT -> focusNext(-1);
              case Input.Keys.SPACE, Input.Keys.ENTER -> useFocused();
              default -> {
                return false;
              }
            }
            return true;
          }
        });
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

  @Override
  protected float arrange(float width, float height) {
    fieldWidth = width;
    float needed = MARGIN + BASKET_HEIGHT + MARGIN;
    float x = Math.round((width - BASKET_WIDTH) / 2);
    float y = -Math.round((Math.max(needed, height) + BASKET_HEIGHT) / 2);
    float dx = x - basket.x();
    float dy = y - basket.y();
    basket.position(x, y);
    // Slips and sheet keep their place in the basket, even while they fly.
    sheet.position(sheet.x() + dx, sheet.y() + dy);
    for (Slip slip : slips) slip.position(slip.x() + dx, slip.y() + dy);
    return needed;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // Tab, the arrow keys and space belong to the basket, not to the hero behind it.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  private void showRejects() {
    String words = REJECTS.stream().filter(seen::contains).collect(Collectors.joining(", "));
    feedback(MorphCoText.text("world.f04.trash-rejects", words), true);
  }

  // What the keys can pick: the sheet once the top slips are tossed, then the slips from the top.
  private List<CanvasNode> choices() {
    List<CanvasNode> choices = new ArrayList<>();
    boolean covered =
        slips.subList(PILE.size() - COVER, PILE.size()).stream().anyMatch(slip -> !slip.tossed);
    if (!sheet.open && !covered) choices.add(sheet);
    for (Slip slip : slips.reversed()) {
      if (!slip.tossed && slip.isTouchable()) choices.add(slip);
    }
    return choices;
  }

  private void focusNext(int step) {
    List<CanvasNode> choices = choices();
    if (choices.isEmpty()) return;
    int index = choices.indexOf(focused);
    focused =
        index < 0 ? choices.getFirst() : choices.get(Math.floorMod(index + step, choices.size()));
  }

  // After a toss the next choice is picked, so space digs on through the basket.
  private void useFocused() {
    if (!choices().contains(focused)) return;
    if (focused == sheet) {
      sheet.unfold();
      focused = null;
    } else {
      ((Slip) focused).toss();
      focused = choices().stream().findFirst().orElse(null);
    }
  }

  // Place of a slip relative to the basket's center, and its size.
  private record Spot(float x, float y, float size) {}

  /** The basket seen from above: a steel rim around a wire mesh. */
  private static final class Basket extends CanvasNode {

    private Basket() {
      super("basket", BASKET_WIDTH, BASKET_HEIGHT);
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
  private final class Slip extends CanvasNode {
    private final String reject;
    private final Color tint = Color.WHITE.cpy().lerp(Color.BLACK, MathUtils.random(.25f));
    private Table tag;
    private boolean over;
    private boolean dragging;
    private boolean tossed;
    // Kept apart from the actor's rotation and scale, which would also turn and scale its hit area.
    private float spin = MathUtils.random(360f);
    private float zoom = 1;

    private Slip(int index, float size, String reject) {
      super("slip" + index, size, size);
      this.reject = reject;
      z(index + 2);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.GRAB);
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

            // The pad tosses at once; holding a slip to pull it aside is for the mouse.
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (GameInput.device() != InputDevice.PAD) return false;
              toss();
              return true;
            }
          });
    }

    @Override
    protected void buildContent() {
      tag = MorphCoUI.tag(reject);
      tag.pack();
      tag.getColor().a = 0;
      // The word must not take the pointer away from the slip below it.
      tag.setTouchable(Touchable.disabled);
      addActor(tag);
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
      boolean shown = over || dragging || picked();
      float fade = (shown ? delta : -delta) / WORD_FADE_SECONDS;
      tag.getColor().a = MathUtils.clamp(tag.getColor().a + fade, 0, 1);
      if (shown && seen.add(reject)) showRejects();
    }

    private boolean picked() {
      return focused == this && !tossed;
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      Color previous = batch.getColor().cpy();
      // The higher the slip flies, the further its shadow falls.
      float lift = 4 + (zoom - 1) * 50;
      batch.setColor(0, 0, 0, alpha * .35f);
      drawPaper(batch, x() + lift, y() - lift);
      batch.setColor(tint.r, tint.g, tint.b, alpha);
      drawPaper(batch, x(), y());
      batch.setColor(previous);
      if (picked())
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x(), y(), width(), height(), 3);
    }

    private void drawPaper(Batch batch, float x, float y) {
      float half = width() / 2;
      batch.draw(crumpled, x, y, half, half, width(), height(), zoom, zoom, spin);
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
      toss();
    }

    // Tossed to the nearer side of the basket, the slip lands on the floor next to it.
    private void toss() {
      tossed = true;
      clearActions();
      zoom = 1;
      Sounds.playUi(MorphCoSounds.RUSTLE, MathUtils.random(.9f, 1.15f));
      boolean left = centerX() < basket.centerX();
      float size = width();
      float targetX =
          left
              ? MathUtils.random(0, basket.x() - size * .6f)
              : MathUtils.random(basket.x() + BASKET_WIDTH - size * .4f, fieldWidth - size);
      float targetY = MathUtils.random(basket.y(), basket.y() + BASKET_HEIGHT - size);
      float from = spin;
      float turn = MathUtils.random(240f, 480f) * (left ? 1 : -1);
      addAction(
          Actions.parallel(
              Actions.moveBy(targetX - x(), targetY - y(), TOSS_SECONDS, Interpolation.pow2Out),
              new TemporalAction(TOSS_SECONDS) {
                @Override
                protected void update(float percent) {
                  spin = from + turn * Interpolation.pow2Out.apply(percent);
                  // Up through the air and down again: the slip grows and shrinks on the way.
                  zoom = 1 + .35f * MathUtils.sin(percent * MathUtils.PI);
                }
              }));
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
          basket.x() + (BASKET_WIDTH - SHEET_WIDTH) / 2,
          basket.y() + (BASKET_HEIGHT - SHEET_HEIGHT) / 2);
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
      if (focused == this && !open) {
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x, y, width, height, 3);
      } else {
        CanvasGraphics.outline(batch, MorphCoUI.GHOST, alpha, x, y, width, height, 1);
      }
    }
  }
}
