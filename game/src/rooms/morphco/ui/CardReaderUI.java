package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.cardreader.CardSwipe;
import feature.interaction.cardreader.Pace;
import java.util.Optional;
import rooms.morphco.util.MorphCoText;

/**
 * The card reader next to the control room door, in the look of the room's consoles.
 *
 * <p>The swipe works like the engine's card reader, see {@link CardSwipe}: the player drags the
 * security card through the slot or holds space or the right arrow key. The reader's display rates
 * the swipe right away; the server rates it again and opens the door.
 */
final class CardReaderUI extends MorphCoConsoleUI {

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

  private final Reader reader = area().addNode(new Reader());
  private final Card card = area().addNode(new Card());
  private final CardSwipe swipe;
  private Optional<Pace.Result> shown = Optional.empty();
  private float left;

  private CardReaderUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.entrance.swipe-title"),
        "",
        MorphCoText.text("world.entrance.swipe-help"));
    swipe = new CardSwipe(context);
    addListener(swipe.keys());
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

  @Override
  protected float arrange(float width, float height) {
    left = Math.max(0, Math.round((width - READER_WIDTH) / 2));
    reader.position(left, -MARGIN - READER_HEIGHT);
    card.position(cardX(), -MARGIN - READER_HEIGHT - CARD_HEIGHT);
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
    swipe.act(delta);
    card.setX(cardX());
    if (swipe.result().equals(shown)) return;
    shown = swipe.result();
    shown.ifPresentOrElse(reader::show, reader::reset);
    if (swipe.accepted()) {
      card.movable(false);
      card.setUserObject(Cursors.DEFAULT);
    }
  }

  private float cardX() {
    return left + START_X + swipe.position() * (END_X - START_X);
  }

  /** Housing with the slot, the status display and the light. */
  private static final class Reader extends CanvasNode {
    private Label display;
    private Color led = MorphCoUI.LED_OFF;

    private Reader() {
      super("reader", READER_WIDTH, READER_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    void show(Pace.Result result) {
      boolean granted = result == Pace.Result.ACCEPTED;
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
      led = MorphCoUI.LED_OFF;
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
      swipe.drag(dx / (END_X - START_X));
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      swipe.drop();
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
