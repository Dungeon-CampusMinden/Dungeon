package feature.interaction.cardreader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Translation;
import engine.utils.BaseContainerUI;
import engine.utils.Cursors;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.Optional;

/**
 * The engine's dialog of a card reader: the reader with its display, its light and the slot, and
 * the card hanging out of the slot. The card moves as {@link CardSwipe} says; the display and the
 * light show the rating of the last swipe.
 */
public final class CardReaderUI extends Table {

  private static final Translation TRANSLATION = new Translation("dialog.card_reader_dialog");
  private static final Color PANEL = Color.valueOf("f2efe6ff");
  private static final Color INK = Color.valueOf("1b1f27ff");
  private static final Color MUTED = Color.valueOf("4a5262ff");
  private static final Color STEEL = Color.valueOf("5b6474ff");
  private static final Color STEEL_DARK = Color.valueOf("2b313bff");
  private static final Color SCREEN = Color.valueOf("d4e6c9ff");
  private static final Color LIGHT_OFF = Color.valueOf("8a919eff");
  private static final Color GO = Color.valueOf("1f7a3aff");
  private static final Color STOP = Color.valueOf("b42323ff");
  private static final Color CARD = Color.valueOf("fbfbf8ff");
  private static final Color CARD_BAND = Color.valueOf("3b6ea8ff");
  private static final Color CHIP = Color.valueOf("d9b44aff");

  private static final float TEXT_WIDTH = 760;
  private static final float READER_WIDTH = 760;
  private static final float READER_HEIGHT = 200;
  private static final float CARD_WIDTH = 240;
  private static final float CARD_HEIGHT = 120;
  private static final float START_X = 20;
  private static final float SLOT_LENGTH = READER_WIDTH - 2 * START_X - CARD_WIDTH;

  // The display on the reader, relative to the reader's bottom left corner.
  private static final float SCREEN_X = 24;
  private static final float SCREEN_Y = 40;
  private static final float SCREEN_WIDTH = 560;
  private static final float SCREEN_HEIGHT = 136;
  private static final float SCREEN_PAD = 18;
  private static final float FRAME = 4;

  // The light right of the display, in a dark frame.
  private static final float LIGHT_X = 640;
  private static final float LIGHT_Y = 80;
  private static final float LIGHT_SIZE = 60;
  private static final float LIGHT_FRAME = 10;

  // The slot along the bottom edge of the reader.
  private static final float SLOT_INSET = 10;
  private static final float SLOT_HEIGHT = 16;

  private final CardSwipe swipe;
  private final Label display;
  private final Card card = new Card();
  private Optional<Pace.Result> shown;
  private Color light = LIGHT_OFF;

  private CardReaderUI(DialogContext context) {
    swipe = new CardSwipe(context);
    setUserObject(Cursors.DEFAULT);
    setBackground(UIUtils.defaultSkin().newDrawable("white", PANEL));
    pad(28, 40, 32, 40);
    defaults().left();
    add(label(TRANSLATION.text("title"), Scene2dElementFactory.FONT_PATH_BOLD, 34, INK)).row();
    Label help = label(TRANSLATION.text("help"), Scene2dElementFactory.FONT_PATH, 20, MUTED);
    help.setWrap(true);
    add(help).width(TEXT_WIDTH).padTop(12).padBottom(24).row();

    display = label("", Scene2dElementFactory.FONT_PATH_BOLD, 26, Color.WHITE);
    display.setWrap(true);
    display.setAlignment(Align.center);
    display.setBounds(
        SCREEN_X + SCREEN_PAD,
        CARD_HEIGHT + SCREEN_Y + SCREEN_PAD,
        SCREEN_WIDTH - 2 * SCREEN_PAD,
        SCREEN_HEIGHT - 2 * SCREEN_PAD);
    Reader reader = new Reader();
    reader.addActor(display);
    reader.addActor(card);
    add(reader).size(READER_WIDTH, READER_HEIGHT + CARD_HEIGHT);
    shown = swipe.result();
    show();
    addListener(swipe.keys());
  }

  /**
   * Builds the dialog of a card reader.
   *
   * @param context the dialog context from {@link CardReaderFactory#use}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new BaseContainerUI(new CardReaderUI(context));
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
    card.setX(START_X + swipe.position() * SLOT_LENGTH);
    if (swipe.result().equals(shown)) return;
    shown = swipe.result();
    show();
  }

  // Shows the rating of the last swipe on the display and the light.
  private void show() {
    String key =
        shown
            .map(
                result ->
                    switch (result) {
                      case ACCEPTED -> "accepted";
                      case TOO_FAST -> "too_fast";
                      case TOO_SLOW -> "too_slow";
                      case INCOMPLETE -> "incomplete";
                    })
            .orElse("ready");
    display.setText(TRANSLATION.text(key));
    if (shown.isEmpty()) {
      light = LIGHT_OFF;
      display.setColor(INK);
    } else {
      light = swipe.accepted() ? GO : STOP;
      display.setColor(light);
    }
    card.setUserObject(swipe.accepted() ? Cursors.DEFAULT : Cursors.GRAB);
  }

  private static Label label(String text, String font, int size, Color color) {
    return Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, color));
  }

  /** Housing with the display, the light and the slot along its bottom edge. */
  private final class Reader extends Group {

    @Override
    public void draw(Batch batch, float alpha) {
      float x = getX();
      float y = getY() + CARD_HEIGHT;
      CanvasGraphics.fill(batch, STEEL, alpha, x, y, READER_WIDTH, READER_HEIGHT);
      CanvasGraphics.outline(batch, STEEL_DARK, alpha, x, y, READER_WIDTH, READER_HEIGHT, FRAME);
      CanvasGraphics.fill(
          batch,
          STEEL_DARK,
          alpha,
          x + SCREEN_X - FRAME,
          y + SCREEN_Y - FRAME,
          SCREEN_WIDTH + 2 * FRAME,
          SCREEN_HEIGHT + 2 * FRAME);
      CanvasGraphics.fill(
          batch, SCREEN, alpha, x + SCREEN_X, y + SCREEN_Y, SCREEN_WIDTH, SCREEN_HEIGHT);
      CanvasGraphics.fill(
          batch,
          STEEL_DARK,
          alpha,
          x + LIGHT_X - LIGHT_FRAME,
          y + LIGHT_Y - LIGHT_FRAME,
          LIGHT_SIZE + 2 * LIGHT_FRAME,
          LIGHT_SIZE + 2 * LIGHT_FRAME);
      CanvasGraphics.fill(batch, light, alpha, x + LIGHT_X, y + LIGHT_Y, LIGHT_SIZE, LIGHT_SIZE);
      // The card hangs out of the slot along the bottom edge.
      CanvasGraphics.fill(
          batch, STEEL_DARK, alpha, x + SLOT_INSET, y, READER_WIDTH - 2 * SLOT_INSET, SLOT_HEIGHT);
      CanvasGraphics.fill(
          batch,
          INK,
          alpha,
          x + SLOT_INSET + FRAME,
          y,
          READER_WIDTH - 2 * (SLOT_INSET + FRAME),
          SLOT_HEIGHT / 2);
      super.draw(batch, alpha);
    }
  }

  /** The card; it only slides along the slot. */
  private final class Card extends Actor {
    private float lastX;

    private Card() {
      setBounds(START_X, 0, CARD_WIDTH, CARD_HEIGHT);
      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              lastX = event.getStageX();
              return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
              swipe.drag((event.getStageX() - lastX) / SLOT_LENGTH);
              lastX = event.getStageX();
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
              swipe.drop();
            }
          });
    }

    @Override
    public void draw(Batch batch, float alpha) {
      float x = getX();
      float y = getY();
      CanvasGraphics.fill(batch, CARD, alpha, x, y, CARD_WIDTH, CARD_HEIGHT);
      CanvasGraphics.outline(batch, MUTED, alpha, x, y, CARD_WIDTH, CARD_HEIGHT, 2);
      CanvasGraphics.fill(batch, CARD_BAND, alpha, x, y + CARD_HEIGHT - 34, CARD_WIDTH, 26);
      CanvasGraphics.fill(batch, CHIP, alpha, x + 18, y + 30, 44, 34);
      CanvasGraphics.outline(batch, MUTED, alpha, x + 18, y + 30, 44, 34, 2);
      for (int line = 0; line < 3; line++) {
        CanvasGraphics.fill(
            batch, MUTED, alpha, x + 84, y + 54 - line * 16, line == 2 ? 80 : 130, 6);
      }
    }
  }
}
