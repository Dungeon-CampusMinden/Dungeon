package rooms.morphco.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.List;
import rooms.morphco.puzzles.CardSwipe;
import rooms.morphco.util.MorphCoText;

/**
 * The card reader next to the control room door.
 *
 * <p>The player drags the security card through the slot; what counts is the time until the card
 * reaches the end. Without a mouse the player holds space or the right arrow key while the card
 * runs through on its own; then the time until the key is let go counts. The display rates the
 * swipe with {@link CardSwipe} right away; the server rates it again and opens the door.
 */
final class CardReaderUI extends CanvasUI {

  private static final String ID = "morphco.card-reader";

  private static final Color PANEL = Color.valueOf("22282c");
  private static final Color TEXT = Color.valueOf("eef1f2");
  private static final Color MUTED = Color.valueOf("b4bec3");
  private static final Color HOUSING = Color.valueOf("4b555b");
  private static final Color EDGE = Color.valueOf("2e3539");
  private static final Color SLOT = Color.valueOf("0c0f11");
  private static final Color SIGNAL = Color.valueOf("f2a531");
  private static final Color LCD = Color.valueOf("0f2219");
  private static final Color READY = Color.valueOf("ffc857");
  private static final Color ERROR = Color.valueOf("ff7a66");
  private static final Color GRANTED = Color.valueOf("63e892");
  private static final Color LED_OFF = Color.valueOf("4a2622");
  private static final Color LED_RED = Color.valueOf("e8483c");
  private static final Color LED_GREEN = Color.valueOf("3fd46c");
  private static final Color CARD = Color.valueOf("eef0f1");
  private static final Color CARD_INK = Color.valueOf("1d2326");
  private static final Color PHOTO = Color.valueOf("9aa5ab");

  private static final float READER_WIDTH = 840;
  private static final float READER_HEIGHT = 220;
  private static final float CARD_WIDTH = 250;
  private static final float CARD_HEIGHT = 110;
  private static final float CARD_Y = 5;
  private static final float READER_Y = CARD_Y + CARD_HEIGHT;
  private static final float START_X = 20;
  private static final float END_X = READER_WIDTH - 20 - CARD_WIDTH;

  /** Without a mouse the card needs this long for the slot, so letting go at the end fits. */
  private static final float KEY_RUN_SECONDS = 0.45f;

  private final Reader reader = new Reader();
  private final Card card = new Card();
  private long startedAt = -1;
  private long arrivedAt = -1;
  private boolean keyRun;
  private float pull;

  private CardReaderUI(String dialogId) {
    super(
        ID,
        new CanvasLayout(
            "",
            READER_WIDTH + 40,
            READER_Y + READER_HEIGHT + 40,
            new CanvasOptions()
                .backgroundColor(PANEL)
                .grid(32, false)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false)
                .panButton(-1)
                .panWithSpace(false)
                .zoom(1, 1),
            false),
        CanvasSnapshot.empty(),
        dialogId,
        List.of());
    area().addNode(reader);
    area().addNode(card);
    area().resetView();

    clearChildren();
    Table panel = new Table();
    panel.setBackground(UIUtils.defaultSkin().newDrawable("white", PANEL));
    panel.pad(20);
    Table header = new Table();
    header.add(label(MorphCoText.text("world.entrance.swipe-title"), 28, TEXT)).growX().left();
    TextButton close =
        Scene2dElementFactory.createButton(MorphCoText.text("ui.close"), "red-outline", 20);
    close.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            requestClose();
          }
        });
    header.add(close);
    panel.add(header).growX().padBottom(8).row();
    panel.add(area()).size(area().getWidth(), area().getHeight()).row();
    Label help = label(MorphCoText.text("world.entrance.swipe-help"), 18, MUTED);
    help.setWrap(true);
    panel.add(help).growX().padTop(10);
    Table root = new Table();
    root.setFillParent(true);
    root.add(panel);
    addActor(root);

    addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (!swipeKey(keycode) || !card.movable() || startedAt >= 0) return false;
            card.clearActions();
            card.setPosition(START_X, CARD_Y);
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
    return new CardReaderUI(context.dialogId());
  }

  private static boolean swipeKey(int keycode) {
    return keycode == Input.Keys.SPACE || keycode == Input.Keys.RIGHT;
  }

  private static Label label(String text, int size, Color color) {
    Label label = Scene2dElementFactory.createLabel(text, size, color);
    label.setTouchable(Touchable.disabled);
    return label;
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
      card.setX(Math.min(END_X, card.getX() + (END_X - START_X) / KEY_RUN_SECONDS * delta));
    }
  }

  /** Every swipe starts with the card on the left, so there is nothing to keep between openings. */
  @Override
  public void flush() {}

  private void release(long millis) {
    String swipe = CardSwipe.encode(millis, card.getX() >= END_X);
    startedAt = -1;
    arrivedAt = -1;
    keyRun = false;
    pull = 0;
    CardSwipe.Result result = CardSwipe.rate(swipe);
    area().fireServerEvent(MorphCoDialogs.SUBMIT, new DialogResponseMessage.StringValue(swipe));
    reader.show(result);
    if (result == CardSwipe.Result.ACCEPTED) {
      card.movable(false);
      card.setUserObject(Cursors.DEFAULT);
    } else {
      card.addAction(Actions.moveTo(START_X, CARD_Y, 0.25f, Interpolation.pow2Out));
    }
  }

  /** Housing with the slot, the status display and the light. */
  private static final class Reader extends CanvasNode {
    private Label display;
    private Color led = LED_OFF;

    private Reader() {
      super("reader", READER_WIDTH, READER_HEIGHT);
      position(0, READER_Y);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    void show(CardSwipe.Result result) {
      boolean granted = result == CardSwipe.Result.ACCEPTED;
      led = granted ? LED_GREEN : LED_RED;
      display.setText(
          MorphCoText.text(
              switch (result) {
                case ACCEPTED -> "world.entrance.swipe-accepted";
                case TOO_FAST -> "world.entrance.swipe-too-fast";
                case TOO_SLOW -> "world.entrance.swipe-too-slow";
                case INCOMPLETE -> "world.entrance.swipe-incomplete";
              }));
      display.setColor(granted ? GRANTED : ERROR);
    }

    void reset() {
      led = LED_OFF;
      display.setText(MorphCoText.text("world.entrance.swipe-ready"));
      display.setColor(READY);
    }

    @Override
    protected void buildContent() {
      display = label(MorphCoText.text("world.entrance.swipe-ready"), 26, Color.WHITE);
      display.setColor(READY);
      display.setWrap(true);
      display.setAlignment(Align.center);
      display.setBounds(44, 56, 552, 128);
      addActor(display);
      Label brand = label("MORPH & CO.", 18, MUTED);
      brand.setAlignment(Align.center);
      brand.setBounds(650, 40, 170, 30);
      addActor(brand);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = getX();
      float y = getY();
      CanvasGraphics.fill(batch, HOUSING, alpha, x, y, getWidth(), getHeight());
      CanvasGraphics.outline(batch, EDGE, alpha, x, y, getWidth(), getHeight(), 4);
      // Warning stripes along the top edge.
      for (float stripe = 8; stripe + 24 <= getWidth() - 8; stripe += 48) {
        CanvasGraphics.fill(batch, SIGNAL, alpha, x + stripe, y + getHeight() - 16, 24, 8);
      }
      CanvasGraphics.fill(batch, EDGE, alpha, x + 24, y + 40, 592, 160);
      CanvasGraphics.fill(batch, LCD, alpha, x + 32, y + 48, 576, 144);
      CanvasGraphics.fill(batch, EDGE, alpha, x + 700, y + 100, 70, 70);
      CanvasGraphics.fill(batch, led, alpha, x + 710, y + 110, 50, 50);
      // The card hangs out of the slot along the bottom edge.
      CanvasGraphics.fill(batch, EDGE, alpha, x + 10, y, getWidth() - 20, 18);
      CanvasGraphics.fill(batch, SLOT, alpha, x + 16, y, getWidth() - 32, 10);
    }
  }

  /** The security card; it only slides along the slot. */
  private final class Card extends CanvasNode {

    private Card() {
      super("card", CARD_WIDTH, CARD_HEIGHT);
      position(START_X, CARD_Y);
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
      setX(Math.clamp(START_X + pull, START_X, END_X));
      if (getX() >= END_X && arrivedAt < 0) arrivedAt = TimeUtils.millis();
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      if (startedAt < 0 || keyRun) return;
      release((arrivedAt < 0 ? TimeUtils.millis() : arrivedAt) - startedAt);
    }

    @Override
    protected void buildContent() {
      Label brand = label("MORPH & CO. KG", 18, CARD_INK);
      brand.setBounds(12, 80, 210, 24);
      addActor(brand);
      Label kind = label(MorphCoText.text("world.entrance.card"), 15, CARD_INK);
      kind.setBounds(80, 46, 160, 22);
      addActor(kind);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = getX();
      float y = getY();
      CanvasGraphics.fill(batch, CARD, alpha, x, y, getWidth(), getHeight());
      CanvasGraphics.outline(batch, PHOTO, alpha, x, y, getWidth(), getHeight(), 2);
      CanvasGraphics.fill(batch, SIGNAL, alpha, x, y + 78, getWidth(), 28);
      // Passport photo: head and shoulders.
      CanvasGraphics.fill(batch, PHOTO, alpha, x + 14, y + 12, 52, 58);
      CanvasGraphics.fill(batch, HOUSING, alpha, x + 32, y + 42, 16, 22);
      CanvasGraphics.fill(batch, HOUSING, alpha, x + 30, y + 45, 20, 16);
      CanvasGraphics.fill(batch, HOUSING, alpha, x + 36, y + 36, 8, 6);
      CanvasGraphics.fill(batch, HOUSING, alpha, x + 26, y + 32, 28, 4);
      CanvasGraphics.fill(batch, HOUSING, alpha, x + 20, y + 12, 40, 20);
      for (int bar = 0; bar < 12; bar++) {
        CanvasGraphics.fill(
            batch, CARD_INK, alpha, x + 80 + bar * 11, y + 14, bar % 3 == 0 ? 6 : 3, 22);
      }
    }
  }
}
