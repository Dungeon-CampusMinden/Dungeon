package rooms.morphco.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.morphco.puzzles.CardSwipe;
import rooms.morphco.util.MorphCoText;

/**
 * The card reader next to the control room door.
 *
 * <p>The player drags the security card through the slot; what counts is the time until the card
 * reaches the end. Without a mouse the player holds space or the right arrow key while the card
 * runs through on its own; then the time until the key is let go counts. The reader's display rates
 * the swipe with {@link CardSwipe} right away; the server rates it again and opens the door.
 */
final class CardReaderUI extends MorphCoConsoleUI {

  private static final Color LED_OFF = Color.valueOf("4a2622");

  private static final float READER_WIDTH = 840;
  private static final float READER_HEIGHT = 220;
  private static final float CARD_WIDTH = 250;
  private static final float CARD_HEIGHT = 110;
  private static final float START_X = 20;
  private static final float END_X = READER_WIDTH - 20 - CARD_WIDTH;
  private static final float MARGIN = 16;

  // The display on the reader, relative to the reader's bottom left corner.
  private static final float LCD_X = 24;
  private static final float LCD_Y = 40;
  private static final float LCD_WIDTH = 592;
  private static final float LCD_HEIGHT = 156;
  private static final float LCD_PAD = 20;

  /** Without a mouse the card needs this long for the slot, so letting go at the end fits. */
  private static final float KEY_RUN_SECONDS = 0.45f;

  private final Reader reader = area().addNode(new Reader());
  private final Card card = area().addNode(new Card());
  private float left;
  private long startedAt = -1;
  private long arrivedAt = -1;
  private boolean keyRun;
  private float pull;

  private CardReaderUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.entrance.swipe-title"),
        "",
        MorphCoText.text("world.entrance.swipe-help"));
    addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (!swipeKey(keycode) || !card.movable() || startedAt >= 0) return false;
            card.clearActions();
            card.setX(left + START_X);
            reader.reset();
            keyRun = true;
            startedAt = TimeUtils.millis();
            return true;
          }

          @Override
          public boolean keyUp(InputEvent event, int keycode) {
            if (!swipeKey(keycode) || !keyRun) return false;
            release(TimeUtils.timeSinceMillis(startedAt));
            return true;
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
    return new CardReaderUI(context);
  }

  private static boolean swipeKey(int keycode) {
    return keycode == Input.Keys.SPACE || keycode == Input.Keys.RIGHT;
  }

  @Override
  protected float arrange(float width, float height) {
    // The card keeps its place in the slot when the reader moves to the new center.
    float inSlot = card.getX() - left;
    left = Math.max(0, Math.round((width - READER_WIDTH) / 2));
    reader.position(left, -MARGIN - READER_HEIGHT);
    card.clearActions();
    card.position(left + inSlot, -MARGIN - READER_HEIGHT - CARD_HEIGHT);
    return MARGIN + READER_HEIGHT + CARD_HEIGHT + MARGIN;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // Space and the arrow key belong to the reader, not to the hero behind it.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (keyRun) {
      float step = (END_X - START_X) / KEY_RUN_SECONDS * delta;
      card.setX(Math.min(left + END_X, card.getX() + step));
    }
  }

  private void release(long millis) {
    String swipe = CardSwipe.encode(millis, card.getX() >= left + END_X);
    startedAt = -1;
    arrivedAt = -1;
    keyRun = false;
    pull = 0;
    CardSwipe.Result result = CardSwipe.rate(swipe);
    submit(swipe);
    reader.show(result);
    if (result == CardSwipe.Result.ACCEPTED) {
      card.movable(false);
      card.setUserObject(Cursors.DEFAULT);
    } else {
      card.addAction(Actions.moveTo(left + START_X, card.getY(), 0.25f, Interpolation.pow2Out));
    }
  }

  /** Housing with the slot, the status display and the light. */
  private static final class Reader extends CanvasNode {
    private Label display;
    private Color led = LED_OFF;

    private Reader() {
      super("reader", READER_WIDTH, READER_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    void show(CardSwipe.Result result) {
      boolean granted = result == CardSwipe.Result.ACCEPTED;
      led = granted ? MorphCoUI.GO : MorphCoUI.STOP;
      display.setText(
          MorphCoText.text(
              switch (result) {
                case ACCEPTED -> "world.entrance.swipe-accepted";
                case TOO_FAST -> "world.entrance.swipe-too-fast";
                case TOO_SLOW -> "world.entrance.swipe-too-slow";
                case INCOMPLETE -> "world.entrance.swipe-incomplete";
              }));
      display.setColor(led);
    }

    void reset() {
      led = LED_OFF;
      display.setText(MorphCoText.text("world.entrance.swipe-ready"));
      display.setColor(MorphCoUI.SIGNAL);
    }

    @Override
    protected void buildContent() {
      display = MorphCoUI.display("", 28, Color.WHITE);
      display.setAlignment(Align.center);
      display.setBounds(
          LCD_X + LCD_PAD, LCD_Y + LCD_PAD, LCD_WIDTH - 2 * LCD_PAD, LCD_HEIGHT - 2 * LCD_PAD);
      addActor(display);
      reset();
      Label brand = MorphCoUI.bold("MORPH & CO.", 18, MorphCoUI.MUTED);
      brand.setAlignment(Align.center);
      brand.setBounds(650, 40, 170, 30);
      addActor(brand);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      MorphCoUI.hazard(batch, alpha, x + 16, y + height() - 18, width() - 32, 8);
      MorphCoUI.screen(batch, alpha, x + LCD_X, y + LCD_Y, LCD_WIDTH, LCD_HEIGHT);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 700, y + 100, 70, 70);
      CanvasGraphics.fill(batch, led, alpha, x + 710, y + 110, 50, 50);
      // The card hangs out of the slot along the bottom edge.
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 10, y, width() - 20, 18);
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 16, y, width() - 32, 10);
    }
  }

  /** The security card; it only slides along the slot. */
  private final class Card extends CanvasNode {

    private Card() {
      super("card", CARD_WIDTH, CARD_HEIGHT);
      position(START_X, 0);
      z(1);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.GRAB);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (!movable() || keyRun) return;
      if (startedAt < 0) {
        startedAt = TimeUtils.millis();
        reader.reset();
      }
      clearActions();
      pull += dx;
      setX(left + Math.clamp(START_X + pull, START_X, END_X));
      if (getX() >= left + END_X && arrivedAt < 0) arrivedAt = TimeUtils.millis();
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      if (startedAt < 0 || keyRun) return;
      release((arrivedAt < 0 ? TimeUtils.millis() : arrivedAt) - startedAt);
    }

    @Override
    protected void buildContent() {
      Label brand = MorphCoUI.bold("MORPH & CO. KG", 18, MorphCoUI.INK);
      brand.setWrap(false);
      brand.setBounds(12, 80, 210, 24);
      addActor(brand);
      Label kind = MorphCoUI.bold(MorphCoText.text("world.entrance.card"), 15, MorphCoUI.INK);
      kind.setWrap(false);
      kind.setBounds(80, 44, 160, 22);
      addActor(kind);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      CanvasGraphics.fill(batch, MorphCoUI.TAG, alpha, x, y, width(), height());
      CanvasGraphics.outline(batch, MorphCoUI.GHOST, alpha, x, y, width(), height(), 2);
      CanvasGraphics.fill(batch, MorphCoUI.SIGNAL, alpha, x, y + 78, width(), 28);
      // Passport photo: head and shoulders.
      CanvasGraphics.fill(batch, MorphCoUI.GHOST, alpha, x + 14, y + 12, 52, 58);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 32, y + 42, 16, 22);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 30, y + 45, 20, 16);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 36, y + 36, 8, 6);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 26, y + 32, 28, 4);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL_DARK, alpha, x + 20, y + 12, 40, 20);
      for (int bar = 0; bar < 12; bar++) {
        CanvasGraphics.fill(
            batch, MorphCoUI.INK, alpha, x + 80 + bar * 11, y + 14, bar % 3 == 0 ? 6 : 3, 22);
      }
    }
  }
}
