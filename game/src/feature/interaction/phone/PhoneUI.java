package feature.interaction.phone;

import static feature.interaction.FeatureDialogStyle.GO;
import static feature.interaction.FeatureDialogStyle.INK;
import static feature.interaction.FeatureDialogStyle.MUTED;
import static feature.interaction.FeatureDialogStyle.PANEL;
import static feature.interaction.FeatureDialogStyle.SCREEN;
import static feature.interaction.FeatureDialogStyle.STEEL;
import static feature.interaction.FeatureDialogStyle.STOP;
import static feature.interaction.FeatureDialogStyle.tinted;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Translation;
import engine.utils.BaseContainerUI;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.keypad.DigitEntry;

/**
 * The engine's dialog of a phone: a display above the dial pad and, if the phone lists lines, the
 * note of important extensions beside it. The keys type into a {@link DigitEntry}; the digits can
 * also be typed, Enter dials. The display shows whether a line answers, see {@link Dialer}.
 */
public final class PhoneUI extends Table {

  private static final Translation TRANSLATION = new Translation("dialog.phone_dialog");
  private static final Color PAPER = Color.valueOf("fbf6dcff");
  private static final String CLEAR = "clear";
  private static final String DIAL = "dial";
  private static final String[] KEYS = {
    "1", "2", "3", "4", "5", "6", "7", "8", "9", CLEAR, "0", DIAL
  };

  private static final float TEXT_WIDTH = 640;
  private static final float KEY_SIZE = 72;
  private static final float KEY_GAP = 10;

  private final Dialer dialer;
  private final DigitEntry entry;
  private final String noConnection;
  private final Label display = Scene2dElementFactory.createBoldLabel("", 28, Color.WHITE);
  private final Label feedback = Scene2dElementFactory.createLabel("", 20, STOP);

  private PhoneUI(DialogContext context) {
    dialer = new Dialer(context);
    entry = new DigitEntry(dialer.maxDigits(), this::dial);
    String text = context.find(PhoneFactory.NO_CONNECTION, String.class).orElse("");
    noConnection = text.isBlank() ? TRANSLATION.text("no_connection") : text;
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
    add(feedback).width(TEXT_WIDTH).padTop(16);
    showDigits();

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
   * Builds the dialog of a phone.
   *
   * @param context the dialog context from {@link PhoneFactory#use}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new BaseContainerUI(new PhoneUI(context));
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // The digits belong to the phone, not to the hero behind it.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  private Table keypad() {
    Table keypad = new Table();
    keypad.setBackground(tinted(STEEL));
    keypad.pad(KEY_GAP * 1.5f);
    Table screen = new Table();
    screen.setBackground(tinted(SCREEN));
    display.setAlignment(Align.center);
    screen.add(display).grow();
    keypad
        .add(screen)
        .colspan(3)
        .size(3 * KEY_SIZE + 2 * KEY_GAP, 64)
        .pad(KEY_GAP / 2)
        .padBottom(KEY_GAP)
        .row();
    for (int index = 0; index < KEYS.length; index++) {
      String key = KEYS[index];
      String caption = key.length() == 1 ? key : TRANSLATION.text(key);
      TextButton button = Scene2dElementFactory.createButton(caption, "default", 28);
      button.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              press(key);
            }
          });
      keypad.add(button).size(KEY_SIZE).pad(KEY_GAP / 2);
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

  private void press(String key) {
    switch (key) {
      case CLEAR -> entry.clear();
      case DIAL -> entry.confirm();
      default -> entry.digit(key.charAt(0));
    }
    showDigits();
  }

  // An answer stays on the display until the next key.
  private void showDigits() {
    if (entry.answered()) return;
    display.setText(entry.digits().isEmpty() ? "_" : entry.digits());
    display.setColor(INK);
    feedback.setText("");
  }

  private void dial(String number) {
    boolean connected = dialer.dial(number);
    // The display keeps the call until the server closes the phone, so OK cannot dial twice.
    entry.answer();
    display.setText(TRANSLATION.text(connected ? "calling" : "no_connection_short"));
    display.setColor(connected ? GO : STOP);
    feedback.setText(connected ? "" : noConnection);
  }
}
