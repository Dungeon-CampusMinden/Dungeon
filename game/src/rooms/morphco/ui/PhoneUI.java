package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.phone.Dialer;
import feature.interaction.phone.PhoneLine;
import rooms.morphco.util.MorphCoText;

/**
 * The engine's phone in the control room, in the look of the room's consoles: handset, mailbox
 * lamp, dial pad and the note with the important extensions.
 *
 * <p>The green handset key of the dial pad dials through the engine's {@link Dialer}, the red one
 * hangs up. After a line has answered, the green key and Enter say yes to its question, or dial the
 * same number again if it asked none. An unknown number gets "no connection" right away. A known
 * number takes a second to dial, with one dot more on the display for every third of it; the keys
 * are disabled and the number goes to the server only then. The red key stays enabled and hangs up;
 * before the second is over, the line never hears of the call. The server answers in the phone: the
 * screen below it shows what the line says, a question with a yes and a no button. The names of the
 * listed lines are text keys of the room.
 */
final class PhoneUI extends MorphCoConsoleUI implements DialogFeedbackReceiver {

  private static final Translation YES_NO = new Translation("dialog.yes_no_dialog");
  private static final float KEYPAD_X = 262;
  private static final float PHONE_WIDTH = KEYPAD_X + KeypadNode.Look.PHONE.width() + 24;
  private static final float PHONE_HEIGHT = 300;
  private static final float NOTE_WIDTH = 300;
  private static final float GAP = 28;
  private static final float MARGIN = 16;
  // The mailbox lamp sits between the handset and the keypad.
  private static final float LAMP_X = 206;
  private static final Color HANDSET_EDGE = Color.valueOf("96461a");
  private static final int EXTENSION_DIGITS = 4;
  private static final long DIAL_MILLIS = 1000;
  private static final int DIAL_DOTS = 3;

  private final Dialer dialer;
  private final Phone phone = area().addNode(new Phone());
  private final KeypadNode keypad;
  private final PanelNode note = area().addNode(new PanelNode("note", PanelNode.Look.PAPER, 16));
  private final PanelNode speech =
      area().addNode(new PanelNode("speech", PanelNode.Look.SCREEN, 22));
  private final Label speechText = MorphCoUI.label("", 18, MorphCoUI.TEXT);
  private final Table question = new Table();
  // The number of the mailbox while it holds the unheard message, otherwise empty.
  private String waitingMailbox;
  private String dialed = "";
  // While a known number is being dialed: when it started.
  private long dialingSince = -1;
  private int dots;

  private PhoneUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.control.phone-title"),
        "",
        MorphCoText.text("ui.phone.help"));
    waitingMailbox = waitingMailbox(context);
    dialer = new Dialer(context);
    keypad =
        area()
            .addNode(
                new KeypadNode(
                    "keypad", KeypadNode.Look.PHONE, dialer.maxDigits(), this::dial, this::hangUp));
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
      int columns = line.number().length() <= EXTENSION_DIGITS ? 1 : 2;
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
    question.add(MorphCoUI.button(YES_NO.text("yes"), true, this::yes));
    question.add(MorphCoUI.button(YES_NO.text("no"), false, this::clearSpeech)).padLeft(12);
    clearSpeech();
    // The keys come through the engine's input context of every phone dialog, see Dialer#INPUT.
    keypad.listenTo(this);
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
    showAnswer(Dialer.answer(message));
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (dialingSince < 0) return;
    long millis = TimeUtils.timeSinceMillis(dialingSince);
    // The display keeps calling until the line answers.
    if (millis >= DIAL_MILLIS) {
      dialingSince = -1;
      dialer.dial(dialed);
      return;
    }
    int now = (int) Math.min(DIAL_DOTS, 1 + millis * DIAL_DOTS / DIAL_MILLIS);
    if (now == dots) return;
    dots = now;
    keypad.show(MorphCoText.text("ui.phone.calling") + ".".repeat(dots), MorphCoUI.SIGNAL);
  }

  private void showAnswer(Dialer.Answer answer) {
    keypad.typing(true);
    // The mailbox has played its message.
    if (dialed.equals(waitingMailbox)) waitingMailbox = "";
    keypad.showDialed(dialed, MorphCoUI.GO, this::dialAgain);
    showSpeech(answer.text(), answer.question());
  }

  @Override
  protected float arrange(float width, float height) {
    float wide = PHONE_WIDTH + GAP + NOTE_WIDTH;
    float left = Math.max(0, Math.round((width - wide) / 2));
    float top = -MARGIN;
    phone.position(left, top - PHONE_HEIGHT);
    keypad.position(left + KEYPAD_X, top - PHONE_HEIGHT + (PHONE_HEIGHT - KeypadNode.HEIGHT) / 2);
    float noteHeight = note.fit(NOTE_WIDTH);
    note.position(left + PHONE_WIDTH + GAP, top - noteHeight);
    float bottom = top - Math.max(PHONE_HEIGHT, noteHeight);
    if (speech.isVisible()) {
      bottom -= MARGIN + speech.fit(wide);
      speech.position(left, bottom);
    }
    return MARGIN - bottom;
  }

  private void dial(String digits) {
    dialed = digits;
    if (dialer.connects(digits)) {
      // The display keeps the call until the next key, so the green key cannot dial twice.
      dialingSince = TimeUtils.millis();
      dots = 0;
      keypad.typing(false);
      clearSpeech();
      return;
    }
    dialer.dial(digits);
    keypad.show(MorphCoText.text("ui.phone.no-line-short"), MorphCoUI.STOP);
    showSpeech(MorphCoText.text("ui.phone.no-line"), false);
    speechText.setColor(MorphCoUI.STOP);
  }

  // The green key after an answer: yes to the line's question as in the engine's phone, otherwise
  // the same number again.
  private void dialAgain() {
    if (question.hasParent()) yes();
    else dial(dialed);
  }

  // The line answers right away; until then the question stays without its buttons.
  private void yes() {
    dialer.yes();
    question.remove();
    changed();
  }

  private void hangUp() {
    dialingSince = -1;
    keypad.typing(true);
    clearSpeech();
  }

  private void clearSpeech() {
    showSpeech("", false);
  }

  private void showSpeech(String text, boolean asks) {
    speechText.setText(text);
    speechText.setColor(MorphCoUI.TEXT);
    speech.content().clearChildren();
    speech.content().add(speechText).growX().row();
    if (asks) speech.content().add(question).left().padTop(14);
    speech.setVisible(!text.isEmpty());
    changed();
  }

  /** Body of the phone with the handset and the mailbox lamp; it takes no input. */
  private final class Phone extends CanvasNode {

    private Phone() {
      super("phone", PHONE_WIDTH, PHONE_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {
      Table tag = MorphCoUI.tag(MorphCoText.text("ui.phone.lamp"));
      tag.pack();
      tag.setPosition(LAMP_X - tag.getWidth() / 2, 150);
      addActor(tag);
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
