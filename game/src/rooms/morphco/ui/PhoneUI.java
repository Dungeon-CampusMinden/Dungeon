package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.keypad.DigitEntry;
import feature.interaction.phone.Dialer;
import feature.interaction.phone.PhoneLine;
import rooms.morphco.util.MorphCoText;

/**
 * The engine's phone in the control room, in the look of the room's consoles: handset, mailbox
 * lamp, dial pad and the note with the important extensions.
 *
 * <p>The green handset key dials through the engine's {@link Dialer}, the red one hangs up and
 * clears the digits; on the keyboard Enter dials and Backspace deletes. An unknown number gets "no
 * connection" right away. For a line the server answers in the phone: the screen below it shows
 * what the line says, a question with a yes and a no button. The names of the listed lines are text
 * keys of the room.
 */
final class PhoneUI extends MorphCoConsoleUI implements DialogFeedbackReceiver {

  private static final float PHONE_HEIGHT = 300;
  private static final float NOTE_WIDTH = 300;
  private static final float GAP = 28;
  private static final float MARGIN = 16;
  // The mailbox lamp sits between the handset and the dial pad.
  private static final float LAMP_X = 206;
  private static final Color HANDSET_EDGE = Color.valueOf("96461a");

  // The dial pad on the right of the phone: a display above three columns of keys.
  private static final float PAD = 10;
  private static final float KEY_GAP = 6;
  private static final float KEY_WIDTH = 60;
  private static final float KEY_HEIGHT = 40;
  private static final float DISPLAY_HEIGHT = 46;
  private static final float PAD_WIDTH = 3 * KEY_WIDTH + 2 * KEY_GAP + 2 * PAD;
  private static final float PAD_HEIGHT = DISPLAY_HEIGHT + 4 * KEY_HEIGHT + 4 * KEY_GAP + 2 * PAD;
  private static final float PAD_X = 256;
  private static final float PHONE_WIDTH = PAD_X + PAD_WIDTH + 24;
  // The display shows the last digits of a longer number.
  private static final int DISPLAY_DIGITS = 9;
  private static final String DIAL = "dial";
  private static final String HANG_UP = "hangUp";
  private static final String[] KEYS = {
    "1", "2", "3", "4", "5", "6", "7", "8", "9", DIAL, "0", HANG_UP
  };

  // The number of the mailbox while it holds the unheard message, otherwise empty.
  private String waitingMailbox;
  private final Dialer dialer;
  private final DigitEntry entry;
  private final Label display = MorphCoUI.display("", 28, Color.WHITE);
  private final Phone phone = area().addNode(new Phone());
  private final PanelNode note = area().addNode(new PanelNode("note", PanelNode.Look.PAPER, 16));
  private final PanelNode speech =
      area().addNode(new PanelNode("speech", PanelNode.Look.SCREEN, 22));
  private final Label speechText = MorphCoUI.label("", 18, MorphCoUI.TEXT);
  private final Table question = new Table();

  private PhoneUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.control.phone-title"),
        MorphCoText.text(
            waitingMailbox(context).isEmpty()
                ? "world.control.phone-quiet"
                : "world.control.phone"),
        MorphCoText.text("ui.phone.help"));
    waitingMailbox = waitingMailbox(context);
    dialer = new Dialer(context);
    entry = new DigitEntry(dialer.maxDigits(), this::dial);
    // Below the strip of tape.
    note.content().padTop(8).defaults().left();
    note.content()
        .add(MorphCoUI.bold(MorphCoText.text("ui.phone.note-title"), 18, MorphCoUI.INK))
        .colspan(2)
        .growX()
        .padBottom(10)
        .row();
    for (PhoneLine line : dialer.note()) {
      // A number longer than an extension, Rosi's mobile, gets a row of its own in blocks of four.
      int columns = line.number().length() <= 4 ? 1 : 2;
      note.content()
          .add(MorphCoUI.label(MorphCoText.text(line.name()), 16, MorphCoUI.INK))
          .colspan(columns)
          .growX()
          .padBottom(columns == 1 ? 6 : 2);
      if (columns == 2) note.content().row();
      Label number =
          MorphCoUI.bold(line.number().replaceAll("(\\d{4})(?=\\d)", "$1 "), 18, MorphCoUI.INK);
      number.setWrap(false);
      note.content().add(number).colspan(columns).right().top().padLeft(12).padBottom(6).row();
    }
    question.add(MorphCoUI.button(MorphCoText.text("ui.phone.yes"), true, this::yes));
    question
        .add(MorphCoUI.button(MorphCoText.text("ui.phone.no"), false, this::hangUp))
        .padLeft(12);
    showDigits();
    showSpeech("", MorphCoUI.TEXT, false);
    addListener(
        new InputListener() {
          @Override
          public boolean keyTyped(InputEvent event, char character) {
            if (!entry.type(character)) return false;
            showDigits();
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
    return new PhoneUI(context);
  }

  private static String waitingMailbox(DialogContext context) {
    return context.find(MorphCoDialogs.MESSAGE_WAITING, String.class).orElse("");
  }

  @Override
  public void applyFeedback(DialogFeedbackMessage message) {
    Dialer.Answer answer = Dialer.answer(message);
    // The mailbox has played its message.
    if (entry.digits().equals(waitingMailbox)) waitingMailbox = "";
    display.setText(shown(entry.digits()));
    display.setColor(MorphCoUI.GO);
    showSpeech(answer.text(), MorphCoUI.TEXT, answer.question());
  }

  @Override
  protected float arrange(float width, float height) {
    float wide = PHONE_WIDTH + GAP + NOTE_WIDTH;
    float left = Math.max(0, Math.round((width - wide) / 2));
    float top = -MARGIN;
    phone.position(left, top - PHONE_HEIGHT);
    float noteHeight = note.fit(NOTE_WIDTH);
    note.position(left + PHONE_WIDTH + GAP, top - noteHeight);
    float bottom = top - Math.max(PHONE_HEIGHT, noteHeight);
    if (speech.isVisible()) {
      bottom -= MARGIN + speech.fit(wide);
      speech.position(left, bottom);
    }
    return MARGIN - bottom;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // The digits belong to the dial pad, not to the hero.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  private void press(String key) {
    switch (key) {
      case DIAL -> entry.confirm();
      case HANG_UP -> hangUp();
      default -> entry.digit(key.charAt(0));
    }
    showDigits();
  }

  private void hangUp() {
    entry.clear();
    showDigits();
    showSpeech("", MorphCoUI.TEXT, false);
  }

  // The question stays readable until the line answers.
  private void yes() {
    dialer.yes();
    showSpeech(speechText.getText().toString(), MorphCoUI.TEXT, false);
  }

  // An answer stays on the display until the next key.
  private void showDigits() {
    if (entry.answered()) return;
    display.setText(entry.digits().isEmpty() ? "_" : shown(entry.digits()));
    display.setColor(MorphCoUI.SIGNAL);
  }

  private static String shown(String digits) {
    if (digits.length() <= DISPLAY_DIGITS) return digits;
    return ".." + digits.substring(digits.length() - DISPLAY_DIGITS + 2);
  }

  private void showSpeech(String text, Color color, boolean asks) {
    speechText.setText(text);
    speechText.setColor(color);
    speech.content().clearChildren();
    speech.content().add(speechText).growX().row();
    if (asks) speech.content().add(question).left().padTop(14);
    speech.setVisible(!text.isEmpty());
    changed();
  }

  private void dial(String digits) {
    // The display keeps the call until the next key, so the green key cannot dial twice.
    entry.answer();
    if (dialer.dial(digits)) {
      display.setText(MorphCoText.text("ui.phone.calling"));
      display.setColor(MorphCoUI.SIGNAL);
      showSpeech("", MorphCoUI.TEXT, false);
      return;
    }
    display.setText(MorphCoText.text("ui.phone.no-line-short"));
    display.setColor(MorphCoUI.STOP);
    showSpeech(MorphCoText.text("ui.phone.no-line"), MorphCoUI.STOP, false);
  }

  // A black handset as on the keys of a phone: lifted at 135 degrees, lying down at 0.
  private static BaseDrawable handset(float rotation) {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        Color previous = batch.getColor().cpy();
        batch.setColor(MorphCoUI.INK.r, MorphCoUI.INK.g, MorphCoUI.INK.b, previous.a);
        // The grip on top and the two cups below its ends: x, y, width and height in parts of the
        // icon's width, from its centre.
        float[][] parts = {
          {-.42f, .06f, .84f, .2f}, {-.5f, -.26f, .3f, .4f}, {.2f, -.26f, .3f, .4f}
        };
        for (float[] part : parts) {
          float partX = part[0] * width;
          float partY = part[1] * width;
          batch.draw(
              CanvasGraphics.white(),
              centerX + partX,
              centerY + partY,
              -partX,
              -partY,
              part[2] * width,
              part[3] * width,
              1,
              1,
              rotation);
        }
        batch.setColor(previous);
      }
    };
  }

  /** Body of the phone with the handset, the mailbox lamp and the dial pad. */
  private final class Phone extends CanvasNode {

    private final Table keys = new Table();

    private Phone() {
      super("phone", PHONE_WIDTH, PHONE_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      // A press between the keys must not start a drag on the canvas.
      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              return true;
            }
          });
    }

    @Override
    protected void buildContent() {
      Table tag = MorphCoUI.tag(MorphCoText.text("ui.phone.lamp"));
      tag.pack();
      tag.setPosition(LAMP_X - tag.getWidth() / 2, 150);
      addActor(tag);
      display.setWrap(false);
      display.setAlignment(Align.center);
      keys.pad(PAD);
      keys.defaults().space(KEY_GAP).size(KEY_WIDTH, KEY_HEIGHT);
      keys.add(display).colspan(3).size(PAD_WIDTH - 2 * PAD, DISPLAY_HEIGHT).row();
      for (int index = 0; index < KEYS.length; index++) {
        keys.add(key(KEYS[index]));
        if (index % 3 == 2) keys.row();
      }
      addActor(keys);
    }

    // A digit key, or the green handset that dials and the red one that hangs up.
    private TextButton key(String key) {
      boolean digit = key.length() == 1;
      TextButton button = MorphCoUI.button(digit ? key : "", false, () -> press(key));
      button.pad(2);
      if (digit) return button;
      Color face = key.equals(DIAL) ? MorphCoUI.GO : MorphCoUI.STOP;
      button.getStyle().up = MorphCoUI.plate(face);
      button.getStyle().over = MorphCoUI.plate(face.cpy().lerp(Color.WHITE, .18f));
      button.getStyle().down = MorphCoUI.plate(face.cpy().lerp(Color.BLACK, .2f));
      button.clearChildren();
      button.add(new Image(handset(key.equals(DIAL) ? 135 : 0))).size(30);
      return button;
    }

    @Override
    protected void layoutContent() {
      keys.setBounds(PAD_X, (PHONE_HEIGHT - PAD_HEIGHT) / 2, PAD_WIDTH, PAD_HEIGHT);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      // The cradle, the two cups of the handset and the grip between them.
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 28, y + 22, 116, 256);
      cup(batch, alpha, x + 34, y + 206);
      cup(batch, alpha, x + 34, y + 32);
      CanvasGraphics.fill(batch, HANDSET_EDGE, alpha, x + 62, y + 92, 48, 116);
      CanvasGraphics.fill(batch, MorphCoUI.ORANGE, alpha, x + 64, y + 92, 44, 116);
      // The lamp blinks while the mailbox holds the unheard message.
      boolean lit = !waitingMailbox.isEmpty() && TimeUtils.millis() / 500 % 2 == 0;
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + LAMP_X - 14, y + 196, 28, 28);
      CanvasGraphics.fill(
          batch, lit ? MorphCoUI.STOP : MorphCoUI.LED_OFF, alpha, x + LAMP_X - 10, y + 200, 20, 20);
      // The dial pad with its display.
      float padY = y + (PHONE_HEIGHT - PAD_HEIGHT) / 2;
      MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x + PAD_X, padY, PAD_WIDTH, PAD_HEIGHT);
      MorphCoUI.screen(
          batch,
          alpha,
          x + PAD_X + PAD,
          padY + PAD_HEIGHT - PAD - DISPLAY_HEIGHT,
          PAD_WIDTH - 2 * PAD,
          DISPLAY_HEIGHT);
    }

    private void cup(Batch batch, float alpha, float x, float y) {
      CanvasGraphics.fill(batch, HANDSET_EDGE, alpha, x, y, 104, 62);
      CanvasGraphics.fill(batch, MorphCoUI.ORANGE, alpha, x + 2, y + 3, 100, 57);
      for (int column = 0; column < 4; column++) {
        for (int row = 0; row < 2; row++) {
          CanvasGraphics.fill(
              batch, HANDSET_EDGE, alpha, x + 28 + column * 14, y + 20 + row * 14, 6, 6);
        }
      }
    }
  }
}
