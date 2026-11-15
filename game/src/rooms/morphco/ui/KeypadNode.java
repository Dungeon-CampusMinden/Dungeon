package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.interaction.keypad.DigitEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import rooms.morphco.util.MorphCoText;

/**
 * A keypad on a control panel: the digits 0 to 9, a clear and a confirm key and a small display
 * with a light.
 *
 * <p>The keys type into the engine's {@link DigitEntry}; the display shows the typed digits.
 * Confirming hands them to the owner, which answers on the display with {@link #show(String,
 * Color)}; the next key starts a new entry. {@link #type(char)} takes digits, backspace and enter
 * from the keyboard.
 */
final class KeypadNode extends CanvasNode {

  static final float WIDTH = 188;
  static final float HEIGHT = 250;

  private static final float PAD = 10;
  private static final float GAP = 6;
  private static final float KEY_WIDTH = 52;
  private static final float KEY_HEIGHT = 40;
  private static final float DISPLAY_HEIGHT = 46;
  private static final String CLEAR = "clear";
  private static final String CONFIRM = "confirm";

  private final DigitEntry entry;
  private final Table content = new Table();
  private final List<TextButton> keys = new ArrayList<>();
  private Label display;
  private boolean enabled = true;
  private Color light = MorphCoUI.LED_OFF;

  /**
   * Creates the keypad.
   *
   * @param id node ID
   * @param maxDigits digits that fit on the display
   * @param onConfirm receives the typed digits when the confirm key is pressed
   */
  KeypadNode(String id, int maxDigits, Consumer<String> onConfirm) {
    super(id, WIDTH, HEIGHT);
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
   * @param text localized text, a word or two
   * @param color color of the text and the light
   */
  void show(String text, Color color) {
    ensureContentBuilt();
    entry.answer();
    light = color;
    display.setText(text);
    display.setColor(color);
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
   * Takes a key from the keyboard.
   *
   * @param character typed character: a digit, backspace or enter
   * @return whether the keypad used it
   */
  boolean type(char character) {
    if (!enabled || !entry.type(character)) return false;
    showDigits();
    return true;
  }

  private void press(String key) {
    switch (key) {
      case CLEAR -> entry.clear();
      case CONFIRM -> entry.confirm();
      default -> entry.digit(key.charAt(0));
    }
    showDigits();
  }

  // An answer stays on the display until the next key.
  private void showDigits() {
    if (entry.answered()) return;
    light = MorphCoUI.LED_OFF;
    display.setText(entry.digits().isEmpty() ? "_" : entry.digits());
    display.setColor(MorphCoUI.SIGNAL);
  }

  @Override
  protected void buildContent() {
    display = MorphCoUI.display("", 30, Color.WHITE);
    display.setWrap(false);
    display.setAlignment(Align.center);
    content.pad(PAD);
    content.defaults().space(GAP).size(KEY_WIDTH, KEY_HEIGHT);
    content.add(display).colspan(3).size(WIDTH - 2 * PAD, DISPLAY_HEIGHT).row();
    String[] layout = {"1", "2", "3", "4", "5", "6", "7", "8", "9", CLEAR, "0", CONFIRM};
    for (int index = 0; index < layout.length; index++) {
      String key = layout[index];
      String caption =
          switch (key) {
            case CLEAR -> MorphCoText.text("ui.keypad.clear");
            case CONFIRM -> MorphCoText.text("ui.keypad.confirm");
            default -> key;
          };
      TextButton button = MorphCoUI.button(caption, key.equals(CONFIRM), () -> press(key));
      button.pad(2);
      button.getStyle().disabled = MorphCoUI.plate(MorphCoUI.STEEL_DARK);
      button.getStyle().disabledFontColor = MorphCoUI.GHOST;
      keys.add(button);
      content.add(button);
      if (index % 3 == 2) content.row();
    }
    addActor(content);
    showDigits();
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
