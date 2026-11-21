package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Align;
import engine.sound.Sounds;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.UIUtils;
import feature.interaction.keypad.KeypadFactory;
import feature.interaction.phone.Dialer;
import feature.interaction.phone.DigitEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * A keypad on a control panel: the digits 0 to 9, a clear and a confirm key and a small display
 * with a light.
 *
 * <p>The keys type into the engine's {@link DigitEntry}; the display shows the typed digits, the
 * last ones of a number too long for it. Confirming hands them to the owner, which answers on the
 * display with {@link #show(String, Color)}; the next key starts a new entry. After {@link
 * #showDialed(String, Color)} the confirm key hands the same digits over again. With {@link
 * #listenTo(Actor)} the keypad also takes the digits, backspace and dial keys of the engine's
 * {@link Dialer}.
 */
final class KeypadNode extends CanvasNode {

  static final float HEIGHT = 250;

  private static final float PAD = 10;
  private static final float GAP = 6;
  private static final float KEY_HEIGHT = 40;
  private static final float DISPLAY_HEIGHT = 46;
  // Room for the light at the right end of the display. The text is centred, so it keeps this much
  // clear on both sides.
  private static final float LIGHT_SPACE = 20;
  private static final String CLEAR = "clear";
  private static final String CONFIRM = "confirm";
  // Deletes the last digit; the keypad has no button for it.
  private static final String BACKSPACE = "backspace";

  /** Width of a keypad in the look {@link Look#KEYPAD}. */
  static final float WIDTH = Look.KEYPAD.width();

  /** How the keypad looks. */
  enum Look {
    /** Keypad of a lock: C clears, the yellow OK confirms. */
    KEYPAD(52),
    /** Dial pad of a phone with wider keys: the green handset dials, the red one hangs up. */
    PHONE(60);

    private final float keyWidth;

    Look(float keyWidth) {
      this.keyWidth = keyWidth;
    }

    /**
     * Returns the width of a keypad in this look.
     *
     * @return node width
     */
    float width() {
      return 3 * keyWidth + 2 * GAP + 2 * PAD;
    }
  }

  private final Look look;
  private final DigitEntry entry;
  private final Runnable onClear;
  private final Table content = new Table();
  private final List<Button> keys = new ArrayList<>();
  private Label display;
  private boolean enabled = true;
  private Color light = MorphCoUI.LED_OFF;

  /**
   * Creates the keypad of a lock.
   *
   * @param id node ID
   * @param maxDigits digits that fit on the display
   * @param onConfirm receives the typed digits when the confirm key is pressed
   */
  KeypadNode(String id, int maxDigits, Consumer<String> onConfirm) {
    this(id, Look.KEYPAD, maxDigits, onConfirm, () -> {});
  }

  /**
   * Creates a keypad.
   *
   * @param id node ID
   * @param look how the keypad looks
   * @param maxDigits digits that can be typed
   * @param onConfirm receives the typed digits when the confirm key is pressed
   * @param onClear called when the clear key has cleared the digits, e.g. to hang up a phone
   */
  KeypadNode(String id, Look look, int maxDigits, Consumer<String> onConfirm, Runnable onClear) {
    super(id, look.width(), HEIGHT);
    this.look = look;
    this.onClear = onClear;
    entry = new DigitEntry(maxDigits, onConfirm);
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

  /**
   * Shows the owner's answer until the next key.
   *
   * @param text localized text, a word or two, or digits
   * @param color color of the text and the light
   */
  void show(String text, Color color) {
    entry.answer();
    showAnswer(text, color);
  }

  /**
   * Shows the typed digits until the next key, e.g. after a line has answered; the confirm key then
   * hands them over again, e.g. to redial. Once a key has been pressed since the digits were
   * confirmed, the display keeps the new entry.
   *
   * @param digits the confirmed digits
   * @param color color of the digits and the light
   */
  void showDialed(String digits, Color color) {
    if (!entry.answered() || !entry.digits().equals(digits)) return;
    entry.answerRedial();
    showAnswer(digits, color);
  }

  /**
   * Enables or disables all keys, e.g. once a lock is open.
   *
   * @param value whether the keys can be pressed
   */
  void enabled(boolean value) {
    ensureContentBuilt();
    enabled = value;
    keys.forEach(key -> key.setDisabled(!value));
  }

  /**
   * Takes the keys of the {@link Dialer}'s actions that reach a dialog, see {@link
   * UIUtils#onUiAction}; its input context must declare them.
   *
   * @param dialog the dialog's outermost actor
   */
  void listenTo(Actor dialog) {
    for (int digit = 0; digit < Dialer.DIGITS.size(); digit++) {
      String key = String.valueOf(digit);
      UIUtils.onUiAction(dialog, Dialer.DIGITS.get(digit), () -> press(key));
    }
    UIUtils.onUiAction(dialog, Dialer.BACKSPACE, () -> press(BACKSPACE));
    UIUtils.onUiAction(dialog, Dialer.DIAL, () -> press(CONFIRM));
  }

  private void press(String key) {
    if (!enabled) return;
    switch (key) {
      case CLEAR -> {
        entry.clear();
        onClear.run();
      }
      case BACKSPACE -> entry.backspace();
      case CONFIRM -> entry.confirm();
      default -> {
        // Every digit of the phone beeps in its own pitch, rising from 0 to 9.
        if (look == Look.PHONE)
          Sounds.playUi(MorphCoSounds.KEY_BEEP, KeypadFactory.digitPitch(key.charAt(0) - '0'));
        entry.digit(key.charAt(0));
      }
    }
    showDigits();
  }

  // An answer stays on the display until the next key.
  private void showDigits() {
    if (entry.answered()) return;
    light = MorphCoUI.LED_OFF;
    showText(entry.digits().isEmpty() ? "_" : entry.digits());
    display.setColor(MorphCoUI.SIGNAL);
  }

  // Text and light in one color; the entry stays as it is.
  private void showAnswer(String text, Color color) {
    ensureContentBuilt();
    light = color;
    showText(text);
    display.setColor(color);
  }

  // A long number scrolls out of the display to the left, as on a phone.
  private void showText(String text) {
    display.setText(text);
    float room = width() - 2 * PAD - 2 * LIGHT_SPACE;
    for (int cut = 3; display.getPrefWidth() > room && cut < text.length(); cut++)
      display.setText(".." + text.substring(cut));
  }

  @Override
  protected void buildContent() {
    display = MorphCoUI.display("", 30, Color.WHITE);
    display.setWrap(false);
    display.setAlignment(Align.center);
    content.pad(PAD);
    content.defaults().space(GAP).size(look.keyWidth, KEY_HEIGHT);
    content.add(display).colspan(3).size(width() - 2 * PAD, DISPLAY_HEIGHT).row();
    String[] layout =
        look == Look.KEYPAD
            ? new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", CLEAR, "0", CONFIRM}
            : new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", CONFIRM, "0", CLEAR};
    for (int index = 0; index < layout.length; index++) {
      Button button = key(layout[index]);
      keys.add(button);
      content.add(button);
      if (index % 3 == 2) content.row();
    }
    addActor(content);
    showDigits();
  }

  private Button key(String key) {
    if (look == Look.PHONE && key.length() > 1) return handsetKey(key);
    String caption =
        switch (key) {
          case CLEAR -> MorphCoText.text("ui.keypad.clear");
          case CONFIRM -> MorphCoText.text("ui.keypad.confirm");
          default -> key;
        };
    TextButton button = MorphCoUI.button(caption, key.equals(CONFIRM), () -> press(key));
    if (look == Look.PHONE) Sounds.ownSound(button);
    button.pad(2);
    button.getStyle().disabled = MorphCoUI.plate(MorphCoUI.STEEL_DARK);
    button.getStyle().disabledFontColor = MorphCoUI.GHOST;
    return button;
  }

  // A black handset on the keys of a phone: lifted on green to dial, lying down on red to hang up.
  private Button handsetKey(String key) {
    boolean dial = key.equals(CONFIRM);
    Button button = MorphCoUI.plateButton(dial ? MorphCoUI.GO : MorphCoUI.STOP, () -> press(key));
    button.add(new Image(handset(dial ? 135 : 0))).size(30);
    return button;
  }

  // A handset turned by the given degrees: the grip on top and the two cups below its ends.
  private static BaseDrawable handset(float rotation) {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        Color previous = batch.getColor().cpy();
        batch.setColor(MorphCoUI.INK.r, MorphCoUI.INK.g, MorphCoUI.INK.b, previous.a);
        // x, y, width and height in parts of the icon's width, from its centre.
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

  @Override
  protected void layoutContent() {
    content.setBounds(0, 0, width(), height());
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
    float top = y() + height() - PAD;
    MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x(), y(), width(), height());
    MorphCoUI.screen(
        batch, alpha, x() + PAD, top - DISPLAY_HEIGHT, width() - 2 * PAD, DISPLAY_HEIGHT);
    CanvasGraphics.fill(batch, light, alpha, x() + width() - PAD - 14, top - 14, 8, 8);
  }
}
