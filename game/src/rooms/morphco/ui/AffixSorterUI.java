package rooms.morphco.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.cardreader.Pace;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.puzzles.LeverPull;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.util.MorphCoText;

/**
 * Close-up of the affix sorter A-12: display, maintenance hatch, hatch lock and lever.
 *
 * <p>Every part of the sorter in the world opens this view, and the part the player used blinks for
 * a moment. The keypad unlocks the hatch, the hatch leads to the affixes and then to the input
 * shafts. Once the shafts are filled, the lever restarts the sorter if it is pulled down at an even
 * pace; before that it is stuck. Without a mouse the player holds space or the down arrow key while
 * the lever runs down on its own and lets go at the bottom. The view rates every input right away
 * with {@link MorphemeQuestions} and {@link LeverPull#PACE}; the server checks it again.
 */
final class AffixSorterUI extends MorphCoConsoleUI {

  /** Progress of the sorter as far as the close-up shows it, with the display's two lines. */
  private enum Progress {
    LOCKED("world.a12.display-error", MorphCoUI.STOP, "world.a12.display-locked"),
    UNLOCKED("world.a12.display-error", MorphCoUI.STOP, "world.a12.display-unlocked"),
    SORTED("world.a12.display-error", MorphCoUI.STOP, "world.a12.display-sorted"),
    FILLED("world.a12.display-restart", MorphCoUI.SIGNAL, "world.a12.display-filled"),
    REPAIRED("world.a12.display-ok", MorphCoUI.GO, "world.a12.machine-repaired");

    private final String status;
    private final Color color;
    private final String message;

    Progress(String status, Color color, String message) {
      this.status = status;
      this.color = color;
      this.message = message;
    }
  }

  private static final float WIDTH = 940;
  private static final float HEIGHT = 440;
  private static final float MARGIN = 16;
  private static final float BOTTOM = -MARGIN - HEIGHT;

  // Parts of the sorter, relative to the housing's bottom left corner.
  private static final Rectangle DISPLAY = new Rectangle(24, 298, 892, 112);
  private static final Rectangle LEVER = new Rectangle(28, 20, 100, 264);
  private static final Rectangle HATCH = new Rectangle(156, 20, 336, 264);
  private static final Rectangle STICKER = new Rectangle(512, 20, 200, 264);
  private static final Rectangle KEYPAD =
      new Rectangle(728, 27, KeypadNode.WIDTH, KeypadNode.HEIGHT);

  // The handle runs down the slot from LEVER_TOP to LEVER_DOWN.
  private static final float HANDLE_WIDTH = 84;
  private static final float HANDLE_HEIGHT = 40;
  private static final float LEVER_TOP = BOTTOM + 190;
  private static final float LEVER_DOWN = LEVER_TOP - 160;

  // The hatch's door, relative to the hatch, and the leaf that stands aside once it is open.
  private static final float DOOR = 16;
  private static final float DOOR_HEIGHT = HATCH.height - 64;
  private static final float LEAF = 20;
  private static final float SHAFT_WIDTH =
      (HATCH.width - 2 * DOOR - LEAF - 12 * (AffixPuzzle.Shaft.values().length + 1))
          / AffixPuzzle.Shaft.values().length;

  // Jammed words behind the unlocked hatch, relative to the door: x, y and width.
  private static final float[][] JAMMED = {
    {36, 10, 96}, {120, 14, 84}, {200, 8, 92}, {52, 34, 70},
    {116, 40, 110}, {226, 32, 64}, {80, 60, 88}, {170, 66, 76}
  };

  /** A stuck lever gives way only this far. */
  private static final float STUCK_TRAVEL = 6;

  /** Without a mouse the lever needs this long to the bottom, so letting go there fits. */
  private static final float KEY_RUN_SECONDS = 0.9f;

  /** The part the player used in the world blinks this long. */
  private static final long HIGHLIGHT_MILLIS = 3000;

  private static final Color KNOB = Color.valueOf("b8352c");
  private static final Color KNOB_OVER = Color.valueOf("e0574a");
  private static final Color STEEL_OVER = MorphCoUI.STEEL.cpy().lerp(Color.WHITE, .18f);

  private final Housing housing = area().addNode(new Housing());
  private final Hatch hatch = area().addNode(new Hatch());
  private final PanelNode sticker =
      area().addNode(new PanelNode("sticker", PanelNode.Look.PAPER, 12));
  private final KeypadNode keypad = area().addNode(new KeypadNode("keypad", 3, this::unlock));
  private final Lever lever = area().addNode(new Lever());
  private final Rectangle highlight;
  private final long openedAt = TimeUtils.millis();
  private Progress progress;
  private long startedAt = -1;
  private long arrivedAt = -1;
  private boolean keyRun;
  private float pull;

  private AffixSorterUI(DialogContext context) {
    super(context, MorphCoText.text("world.titles.a12"), "", MorphCoText.text("ui.sorter.help"));
    progress = progressOf(context);
    highlight =
        switch (context.find(MorphCoDialogs.A12_PART, String.class).orElse("")) {
          case MorphCoDialogs.A12_LOCK -> KEYPAD;
          case MorphCoDialogs.A12_HATCH -> HATCH;
          case MorphCoDialogs.A12_LEVER -> LEVER;
          case MorphCoDialogs.A12_DISPLAY -> DISPLAY;
          default -> null;
        };
    // Below the strip of tape.
    sticker.content().padTop(8).defaults().left().growX();
    sticker
        .content()
        .add(MorphCoUI.bold(MorphCoText.text("world.a12.lock-title"), 17, MorphCoUI.INK))
        .padBottom(8)
        .row();
    sticker
        .content()
        .add(MorphCoUI.label(MorphCoText.text("world.a12.lock-question"), 15, MorphCoUI.INK));
    if (progress != Progress.LOCKED) openLock();
    showProgress();
    addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (!leverKey(keycode) || startedAt >= 0) return false;
            if (progress.compareTo(Progress.FILLED) < 0) {
              release(0);
              return true;
            }
            lever.clearActions();
            lever.setY(LEVER_TOP);
            startPull();
            keyRun = true;
            return true;
          }

          @Override
          public boolean keyUp(InputEvent event, int keycode) {
            if (!leverKey(keycode) || !keyRun) return false;
            release(TimeUtils.timeSinceMillis(startedAt));
            return true;
          }

          @Override
          public boolean keyTyped(InputEvent event, char character) {
            return keypad.type(character);
          }
        });
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new AffixSorterUI(context);
  }

  private static Progress progressOf(DialogContext context) {
    if (flag(context, MorphCoDialogs.A12_OK)) return Progress.REPAIRED;
    if (flag(context, MorphCoDialogs.SHAFTS_OK)) return Progress.FILLED;
    if (flag(context, MorphCoDialogs.AFFIXES_OK)) return Progress.SORTED;
    if (flag(context, MorphCoDialogs.HATCH_OK)) return Progress.UNLOCKED;
    return Progress.LOCKED;
  }

  private static boolean flag(DialogContext context, String key) {
    return context.find(key, Boolean.class).orElse(false);
  }

  private static float shaftX(int index) {
    return DOOR + LEAF + 12 + index * (SHAFT_WIDTH + 12);
  }

  private static boolean leverKey(int keycode) {
    return keycode == Input.Keys.SPACE || keycode == Input.Keys.DOWN;
  }

  @Override
  protected float arrange(float width, float height) {
    float left = Math.max(0, Math.round((width - WIDTH) / 2));
    housing.position(left, BOTTOM);
    hatch.position(left + HATCH.x, BOTTOM + HATCH.y);
    sticker.size(STICKER.width, STICKER.height).position(left + STICKER.x, BOTTOM + STICKER.y);
    keypad.position(left + KEYPAD.x, BOTTOM + KEYPAD.y);
    lever.clearActions();
    lever.position(left + LEVER.x + (LEVER.width - HANDLE_WIDTH) / 2, lever.getY());
    return MARGIN + HEIGHT + MARGIN;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // Space and the arrow key belong to the lever, the digits to the keypad, not to the hero.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (keyRun) {
      float step = (LEVER_TOP - LEVER_DOWN) / KEY_RUN_SECONDS * delta;
      lever.setY(Math.max(LEVER_DOWN, lever.getY() - step));
    }
  }

  private void showProgress() {
    housing.status(MorphCoText.text(progress.status), progress.color);
    housing.message(MorphCoText.text(progress.message), MorphCoUI.TEXT);
    hatch.refresh();
  }

  private void unlock(String digits) {
    submit(MorphCoDialogs.A12Answer.encode(MorphCoDialogs.A12_LOCK, digits));
    if (MorphemeQuestions.hatchAnswer(digits)) {
      progress = Progress.UNLOCKED;
      openLock();
      showProgress();
      feedback(MorphCoText.text("world.a12.lock-correct"), true);
      return;
    }
    boolean almost = MorphemeQuestions.hatchAlmost(digits);
    keypad.show(
        MorphCoText.text(almost ? "world.a12.keypad-almost" : "world.a12.keypad-wrong"),
        MorphCoUI.STOP);
    feedback(MorphCoText.text(almost ? "world.a12.lock-almost" : "world.a12.lock-wrong"), false);
  }

  private void openLock() {
    keypad.show(MorphCoText.text("world.a12.keypad-open"), MorphCoUI.GO);
    keypad.enabled(false);
  }

  private void openHatch() {
    submit(MorphCoDialogs.A12Answer.encode(MorphCoDialogs.A12_HATCH, ""));
    switch (progress) {
      case LOCKED -> feedback(MorphCoText.text("world.a12.hatch-locked"), false);
      case FILLED, REPAIRED -> feedback(MorphCoText.text("world.a12.hatch-done"), true);
      default -> {
        // The server closes the close-up and opens the affixes or the input shafts.
      }
    }
  }

  private void startPull() {
    startedAt = TimeUtils.millis();
    if (progress == Progress.FILLED) showProgress();
  }

  private void release(long millis) {
    String answer = Pace.encode(millis, lever.getY() <= LEVER_DOWN);
    startedAt = -1;
    arrivedAt = -1;
    keyRun = false;
    pull = 0;
    submit(MorphCoDialogs.A12Answer.encode(MorphCoDialogs.A12_LEVER, answer));
    switch (progress) {
      case FILLED -> rate(LeverPull.PACE.rate(answer));
      case REPAIRED -> {
        feedback(MorphCoText.text("world.a12.lever-repaired"), true);
        lever.back();
      }
      default -> {
        feedback(MorphCoText.text("world.a12.lever"), false);
        lever.jerk();
      }
    }
  }

  private void rate(Pace.Result result) {
    boolean accepted = result == Pace.Result.ACCEPTED;
    if (accepted) {
      progress = Progress.REPAIRED;
      showProgress();
    }
    housing.message(
        MorphCoText.text(
            switch (result) {
              case ACCEPTED -> "world.a12.lever-accepted";
              case TOO_FAST -> "world.a12.lever-too-fast";
              case TOO_SLOW -> "world.a12.lever-too-slow";
              case INCOMPLETE -> "world.a12.lever-incomplete";
            }),
        accepted ? MorphCoUI.GO : MorphCoUI.STOP);
    lever.back();
  }

  /** Housing with the display and the slot of the lever; it takes no input. */
  private final class Housing extends CanvasNode {
    private Label status;
    private Label message;

    private Housing() {
      super("housing", WIDTH, HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    void status(String text, Color color) {
      ensureContentBuilt();
      status.setText(text);
      status.setColor(color);
    }

    void message(String text, Color color) {
      ensureContentBuilt();
      message.setText(text);
      message.setColor(color);
    }

    @Override
    protected void buildContent() {
      status = MorphCoUI.display("", 32, Color.WHITE);
      status.setBounds(DISPLAY.x + 20, DISPLAY.y + DISPLAY.height - 54, DISPLAY.width - 40, 40);
      addActor(status);
      message = MorphCoUI.label("", 19, Color.WHITE);
      message.setBounds(DISPLAY.x + 20, DISPLAY.y + 10, DISPLAY.width - 40, 50);
      addActor(message);
      Table name = MorphCoUI.tag(MorphCoText.text("world.a12.lever-title"));
      name.pack();
      name.setPosition(
          LEVER.x + (LEVER.width - name.getWidth()) / 2,
          LEVER.y + LEVER.height - 8 - name.getHeight());
      addActor(name);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      MorphCoUI.hazard(batch, alpha, x + 16, y + height() - 18, width() - 32, 8);
      MorphCoUI.screen(batch, alpha, x + DISPLAY.x, y + DISPLAY.y, DISPLAY.width, DISPLAY.height);
      MorphCoUI.plate(
          batch, MorphCoUI.STEEL_DARK, alpha, x + LEVER.x, y + LEVER.y, LEVER.width, LEVER.height);
      float slot = x + LEVER.x + LEVER.width / 2;
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, slot - 8, y + LEVER.y + 8, 16, 204);
      // An arrow next to the slot shows which way the lever goes.
      float arrow = x + LEVER.x + LEVER.width + 14;
      CanvasGraphics.fill(batch, MorphCoUI.SIGNAL, alpha, arrow - 2, y + 80, 4, 130);
      for (int row = 0; row < 8; row++) {
        CanvasGraphics.fill(
            batch, MorphCoUI.SIGNAL, alpha, arrow - 8 + row, y + 80 - row * 2, 16 - row * 2, 2);
      }
      long shown = TimeUtils.timeSinceMillis(openedAt);
      if (highlight != null && shown < HIGHLIGHT_MILLIS && shown / 250 % 2 == 0) {
        CanvasGraphics.outline(
            batch,
            MorphCoUI.SIGNAL,
            alpha,
            x + highlight.x - 6,
            y + highlight.y - 6,
            highlight.width + 12,
            highlight.height + 12,
            4);
      }
    }
  }

  /** The maintenance hatch; behind it the jammed affixes and, once they are sorted, the shafts. */
  private final class Hatch extends CanvasNode {
    private final ClickListener click =
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openHatch();
          }
        };
    private final List<Actor> shafts = new ArrayList<>();

    private Hatch() {
      super("hatch", HATCH.width, HATCH.height);
      movable(false);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.INTERACT);
      addListener(click);
    }

    void refresh() {
      ensureContentBuilt();
      boolean sorted = progress.compareTo(Progress.SORTED) >= 0;
      shafts.forEach(actor -> actor.setVisible(sorted));
    }

    @Override
    protected void buildContent() {
      Table name = MorphCoUI.tag(MorphCoText.text("world.a12.hatch-title"));
      name.pack();
      name.setPosition(DOOR, height() - 8 - name.getHeight());
      addActor(name);
      Table tag = MorphCoUI.tag(MorphCoText.text("ui.sorter.shafts"));
      tag.pack();
      tag.setPosition((width() - tag.getWidth()) / 2, 24);
      shafts.add(tag);
      for (AffixPuzzle.Shaft shaft : AffixPuzzle.Shaft.values()) {
        Label label =
            MorphCoUI.bold(MorphCoText.text("ui.affixes." + shaft.name()), 15, MorphCoUI.TAG);
        label.setWrap(false);
        label.setAlignment(Align.center);
        label.setBounds(shaftX(shaft.ordinal()), DOOR + DOOR_HEIGHT - 30, SHAFT_WIDTH, 22);
        shafts.add(label);
      }
      shafts.forEach(this::addActor);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float doorWidth = width() - 2 * DOOR;
      Color face = click.isOver() ? STEEL_OVER : MorphCoUI.STEEL;
      MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x, y, width(), height());
      if (progress == Progress.LOCKED) {
        MorphCoUI.plate(batch, face, alpha, x + DOOR, y + DOOR, doorWidth, DOOR_HEIGHT);
        float handleX = x + DOOR + doorWidth / 2 - 60;
        float handleY = y + DOOR + DOOR_HEIGHT / 2 - 8;
        CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, handleX, handleY, 120, 16);
        CanvasGraphics.fill(batch, MorphCoUI.GHOST, alpha, handleX + 4, handleY + 10, 112, 3);
      } else {
        // Open: the door leaf stands aside, behind it the jammed affixes or the input shafts.
        CanvasGraphics.fill(
            batch, MorphCoUI.INSET, alpha, x + DOOR, y + DOOR, doorWidth, DOOR_HEIGHT);
        MorphCoUI.plate(batch, face, alpha, x + DOOR, y + DOOR, LEAF, DOOR_HEIGHT);
        if (progress == Progress.UNLOCKED) drawJammed(batch, alpha, x + DOOR, y + DOOR);
        else drawShafts(batch, alpha, x, y);
      }
      Color lamp = progress == Progress.LOCKED ? MorphCoUI.STOP : MorphCoUI.GO;
      CanvasGraphics.fill(
          batch, MorphCoUI.INSET, alpha, x + width() - 38, y + height() - 38, 22, 22);
      CanvasGraphics.fill(batch, lamp, alpha, x + width() - 34, y + height() - 34, 14, 14);
      if (click.isOver()) {
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x, y, width(), height(), 3);
      }
    }

    private void drawJammed(Batch batch, float alpha, float doorX, float doorY) {
      for (float[] piece : JAMMED) {
        float pieceX = doorX + piece[0];
        float pieceY = doorY + piece[1];
        CanvasGraphics.fill(batch, MorphCoUI.TAG, alpha, pieceX, pieceY, piece[2], 22);
        CanvasGraphics.outline(batch, MorphCoUI.GHOST, alpha, pieceX, pieceY, piece[2], 22, 2);
      }
    }

    private void drawShafts(Batch batch, float alpha, float x, float y) {
      for (int index = 0; index < AffixPuzzle.Shaft.values().length; index++) {
        float shaftX = x + shaftX(index);
        CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, shaftX, y + 68, SHAFT_WIDTH, 112);
        CanvasGraphics.fill(
            batch, MorphCoUI.FRAME, alpha, shaftX + 8, y + 76, SHAFT_WIDTH - 16, 96);
        if (progress.compareTo(Progress.FILLED) >= 0) {
          CanvasGraphics.fill(
              batch, MorphCoUI.TAG, alpha, shaftX + SHAFT_WIDTH / 2 - 18, y + 84, 36, 18);
        }
      }
    }
  }

  /** The handle of the lever; it only slides down the slot. */
  private final class Lever extends CanvasNode {
    private boolean over;

    private Lever() {
      super("lever", HANDLE_WIDTH, HANDLE_HEIGHT);
      position(0, LEVER_TOP);
      z(1);
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
          });
    }

    @Override
    public void onMove(float dx, float dy) {
      if (keyRun) return;
      if (startedAt < 0) startPull();
      clearActions();
      pull -= dy;
      float travel =
          progress.compareTo(Progress.FILLED) < 0 ? STUCK_TRAVEL : LEVER_TOP - LEVER_DOWN;
      setY(LEVER_TOP - Math.clamp(pull, 0, travel));
      if (getY() <= LEVER_DOWN && arrivedAt < 0) arrivedAt = TimeUtils.millis();
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      if (startedAt < 0 || keyRun) return;
      release((arrivedAt < 0 ? TimeUtils.millis() : arrivedAt) - startedAt);
    }

    // A click is a pull that never got going.
    @Override
    public void onClick(float localX, float localY, int button) {
      if (startedAt < 0) release(0);
    }

    void back() {
      clearActions();
      addAction(Actions.moveTo(getX(), LEVER_TOP, 0.25f, Interpolation.pow2Out));
    }

    void jerk() {
      clearActions();
      addAction(
          Actions.repeat(
              3,
              Actions.sequence(
                  Actions.moveTo(getX(), LEVER_TOP - STUCK_TRAVEL, 0.05f),
                  Actions.moveTo(getX(), LEVER_TOP, 0.05f))));
    }

    @Override
    protected void buildContent() {
      // Drawn entirely in drawBackground.
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      float knob = x + width() / 2 - 18;
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y + 12, width(), 16);
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, knob - 2, y, 40, 40);
      CanvasGraphics.fill(batch, over ? KNOB_OVER : KNOB, alpha, knob, y + 2, 36, 36);
      CanvasGraphics.fill(batch, Color.WHITE, alpha * .45f, knob + 6, y + 26, 12, 6);
    }
  }
}
