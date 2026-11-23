package feature.interaction.phone;

import static feature.hud.UIUtils.tinted;
import static feature.interaction.InteractionDialogColors.GO;
import static feature.interaction.InteractionDialogColors.INK;
import static feature.interaction.InteractionDialogColors.MUTED;
import static feature.interaction.InteractionDialogColors.PANEL;
import static feature.interaction.InteractionDialogColors.SCREEN;
import static feature.interaction.InteractionDialogColors.STEEL;
import static feature.interaction.InteractionDialogColors.STOP;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.BaseContainerUI;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;

/**
 * The engine's dialog of a phone: a display above the dial pad and, if the phone lists lines, the
 * note of important extensions beside it. The keys type into a {@link DigitEntry}; the digits can
 * also be typed, Enter dials, see {@link Dialer#INPUT}. The display shows whether a line answers;
 * what the line says appears below the dial pad, a question with a yes and a no button.
 */
public final class PhoneUI extends Table implements DialogFeedbackReceiver {

  private static final Translation TRANSLATION = new Translation("dialog.phone_dialog");
  private static final Translation YES_NO = new Translation("dialog.yes_no_dialog");
  private static final Color PAPER = Color.valueOf("fbf6dcff");
  private static final String CLEAR = "clear";
  private static final String DIAL = "dial";
  private static final String BACKSPACE = "backspace";
  private static final String[] KEYS = {
    "1", "2", "3", "4", "5", "6", "7", "8", "9", CLEAR, "0", DIAL
  };

  private static final float TEXT_WIDTH = 640;
  private static final float KEY_SIZE = 72;
  private static final float KEY_GAP = 10;
  private static final float SCREEN_WIDTH = 3 * KEY_SIZE + 2 * KEY_GAP;

  private final Dialer dialer;
  private final DigitEntry entry;
  private final String noConnection;
  private final Label display = Scene2dElementFactory.createBoldLabel("", 28, Color.WHITE);
  private final Label feedback = Scene2dElementFactory.createLabel("", 20, STOP);
  private final Table question = new Table();

  private PhoneUI(DialogContext context) {
    dialer = new Dialer(context);
    entry = new DigitEntry(dialer.maxDigits(), this::dial);
    noConnection =
        context
            .find(PhoneFactory.NO_CONNECTION, String.class)
            .orElseGet(() -> TRANSLATION.text("no_connection"));
    setUserObject(Cursors.DEFAULT);
    setBackground(tinted(PANEL));
    pad(28, 40, 32, 40);
    defaults().left();
    add(Scene2dElementFactory.createBoldLabel(TRANSLATION.text("title"), 34, INK)).row();
    Label help = Scene2dElementFactory.createLabel(TRANSLATION.text("help"), 20, MUTED);
    help.setWrap(true);
    add(help).width(TEXT_WIDTH).padTop(12).padBottom(24).row();

    Table body = new Table();
    body.add(keypad()).top();
    if (!dialer.note().isEmpty()) body.add(note()).top().padLeft(32);
    add(body).row();
    feedback.setWrap(true);
    add(feedback).width(TEXT_WIDTH).padTop(16).row();
    question.add(button(YES_NO.text("yes"), 24, () -> answerQuestion(true)));
    question.add(button(YES_NO.text("no"), 24, () -> answerQuestion(false))).padLeft(16);
    add(question).padTop(12);
    showDigits();
  }

  /**
   * Builds the dialog of a phone.
   *
   * @param context the dialog context from {@link PhoneFactory#use}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    PhoneUI phone = new PhoneUI(context);
    BaseContainerUI container = new BaseContainerUI(phone);
    for (int digit = 0; digit < Dialer.DIGITS.size(); digit++) {
      String key = String.valueOf(digit);
      UIUtils.onUiAction(container, Dialer.DIGITS.get(digit), () -> phone.press(key));
    }
    UIUtils.onUiAction(container, Dialer.BACKSPACE, () -> phone.press(BACKSPACE));
    UIUtils.onUiAction(container, Dialer.DIAL, () -> phone.press(DIAL));
    return container;
  }

  @Override
  public void applyFeedback(DialogFeedbackMessage message) {
    // A late answer to an earlier number must not cover the current one.
    if (!entry.answered()
        || !message.sourceFingerprint().equals(DialogFeedbackFingerprint.of(entry.digits()))) {
      return;
    }
    Dialer.Answer answer = Dialer.answer(message);
    show(entry.digits(), GO);
    feedback.setText(answer.text());
    feedback.setColor(INK);
    question.setVisible(answer.question());
  }

  private Table keypad() {
    Table keypad = new Table();
    keypad.setBackground(tinted(STEEL));
    keypad.pad(KEY_GAP * 1.5f);
    Table screen = new Table();
    screen.setBackground(tinted(SCREEN));
    display.setAlignment(Align.center);
    screen.add(display).grow();
    keypad.add(screen).colspan(3).size(SCREEN_WIDTH, 64).pad(KEY_GAP / 2).padBottom(KEY_GAP).row();
    for (int index = 0; index < KEYS.length; index++) {
      String key = KEYS[index];
      String caption = key.length() == 1 ? key : TRANSLATION.text(key);
      keypad.add(button(caption, 28, () -> press(key))).size(KEY_SIZE).pad(KEY_GAP / 2);
      if (index % 3 == 2) keypad.row();
    }
    return keypad;
  }

  private Table note() {
    Table note = new Table();
    note.setBackground(tinted(PAPER));
    note.pad(16, 20, 16, 20);
    note.defaults().left();
    note.add(Scene2dElementFactory.createBoldLabel(TRANSLATION.text("note_title"), 22, INK))
        .colspan(2)
        .padBottom(10)
        .row();
    for (PhoneLine line : dialer.note()) {
      note.add(Scene2dElementFactory.createLabel(line.name(), 20, INK)).padBottom(6);
      note.add(Scene2dElementFactory.createBoldLabel(line.number(), 20, INK))
          .right()
          .padLeft(24)
          .padBottom(6)
          .row();
    }
    return note;
  }

  private TextButton button(String caption, int fontSize, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(caption, "default", fontSize);
    button.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            action.run();
          }
        });
    return button;
  }

  // Either answer ends the question; a no needs no answer to the server. A button released after
  // the question ended does nothing.
  private void answerQuestion(boolean yes) {
    if (!question.isVisible()) return;
    question.setVisible(false);
    if (yes) dialer.yes();
    else feedback.setText("");
  }

  // During a question, OK says yes; every other key says no by starting a new entry.
  private void press(String key) {
    switch (key) {
      case CLEAR -> entry.clear();
      case DIAL -> {
        if (question.isVisible()) answerQuestion(true);
        else entry.confirm();
      }
      case BACKSPACE -> entry.backspace();
      default -> entry.digit(key.charAt(0));
    }
    showDigits();
  }

  // An answer stays on the display until the next key.
  private void showDigits() {
    if (entry.answered()) return;
    show(entry.digits().isEmpty() ? "_" : entry.digits(), INK);
    feedback.setText("");
    question.setVisible(false);
  }

  private void dial(String number) {
    boolean connected = dialer.dial(number);
    // The display keeps the call until the next key, so OK cannot dial twice.
    entry.answer();
    show(TRANSLATION.text(connected ? "calling" : "no_connection_short"), connected ? GO : STOP);
    feedback.setText(connected ? "" : noConnection);
    feedback.setColor(STOP);
  }

  // A long number shrinks until it fits the screen.
  private void show(String text, Color color) {
    display.setText(text);
    display.setColor(color);
    display.setFontScale(1);
    display.setFontScale(Math.min(1, (SCREEN_WIDTH - 2 * KEY_GAP) / display.getPrefWidth()));
  }
}
