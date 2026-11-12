package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import feature.hud.elements.RichLabel;
import feature.leveleditor.EditorActions;
import feature.leveleditor.LevelEditorMode;
import feature.systems.LevelEditorSystem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bar along the bottom of the screen: the last feedback message of the editor on the left, the
 * state of the active mode in the middle and its controls on the right, with key and mouse symbols.
 */
public final class StatusBar extends Table {

  /** Height of the bar. */
  public static final float HEIGHT = 30f;

  private static final int FONT_SIZE = 14;
  private static final float MESSAGE_SECONDS = 4f;
  private static final float FADE_SECONDS = 0.6f;
  // Room kept for the message and the status of the mode.
  private static final float MESSAGE_WIDTH = 560f;

  private final Label message = EditorTheme.label("", FONT_SIZE, EditorTheme.TEXT);
  private final Label status = EditorTheme.label("", FONT_SIZE, EditorTheme.TEXT_MUTED);
  private final RichLabel hints = new RichLabel("", FONT_SIZE, EditorTheme.TEXT_MUTED, false);
  private LevelEditorMode mode;
  private String hintText = "";
  private final java.util.List<String> hintItems = new java.util.ArrayList<>();
  private float fittedWidth = -1f;

  /** Creates an empty status bar. */
  public StatusBar() {
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.flat(new Color(0.06f, 0.065f, 0.08f, 0.94f)));
    pad(0f, 12f, 0f, 12f);
    message.setEllipsis(true);
    status.setEllipsis(true);
    hints.setWrap(false);
    hints.setAlignment(Align.right);
    add(message).minWidth(0f).prefWidth(420f).left();
    add(status).minWidth(0f).padLeft(16f).left();
    add().growX();
    add(hints).right();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    LevelEditorMode current = LevelEditorSystem.currentModeInstance();
    if (current != mode) {
      mode = current;
      updateHints();
    }
    if (getWidth() != fittedWidth) fitHints();
    String statusText = mode == null ? "" : Objects.requireNonNullElse(mode.statusText(), "");
    String shaders = "Shaders: " + LevelEditorSystem.shadersActiveLastFrame();
    statusText = statusText.isEmpty() ? shaders : statusText + "     " + shaders;
    if (!statusText.equals(status.getText().toString())) status.setText(statusText);
    updateMessage();
  }

  private void updateMessage() {
    String text = LevelEditorSystem.feedbackMessage();
    float age = LevelEditorSystem.feedbackAge();
    if (text == null || text.isEmpty() || age > MESSAGE_SECONDS) {
      if (message.getText().length() > 0) message.setText("");
      return;
    }
    if (!text.contentEquals(message.getText())) message.setText(text);
    Color color = messageColor(LevelEditorSystem.feedbackColor()).cpy();
    float remaining = MESSAGE_SECONDS - age;
    color.a = Math.min(1f, remaining / FADE_SECONDS);
    message.setColor(color);
  }

  private static Color messageColor(Color feedback) {
    if (feedback == Color.RED) return EditorTheme.ERROR;
    if (feedback == Color.YELLOW) return EditorTheme.WARNING;
    if (feedback == Color.GREEN) return EditorTheme.SUCCESS;
    return EditorTheme.TEXT;
  }

  private void updateHints() {
    hintItems.clear();
    if (mode != null) {
      Map<String, String> inputsByLabel = new LinkedHashMap<>();
      mode.controls()
          .forEach(
              (action, label) ->
                  inputsByLabel.merge(label, inputTag(action), (inputs, next) -> inputs + next));
      inputsByLabel.forEach((label, inputs) -> hintItems.add(hint(inputs, label)));
    }
    hintItems.add(hint(inputTag(EditorActions.DEBUG_SHADER), "Layer debug"));
    hintItems.add(hint(inputTag(EditorActions.TOGGLE), "Close editor"));
    fitHints();
  }

  // Leaves out the last hints while they do not fit next to the message.
  private void fitHints() {
    fittedWidth = getWidth();
    float available = getWidth() - MESSAGE_WIDTH - getPadLeft() - getPadRight();
    for (int count = hintItems.size(); count >= 0; count--) {
      String text = String.join("     ", hintItems.subList(0, count));
      if (!text.equals(hintText)) {
        hintText = text;
        hints.setText(text);
      }
      if (available <= 0f || hints.getPrefWidth() <= available) break;
    }
    invalidateHierarchy();
  }

  private static String hint(String inputs, String label) {
    return inputs + " [color=#E6E8EB]" + escape(label) + "[/color]";
  }

  // Square brackets would start markup tags.
  private static String escape(String text) {
    return RichLabel.toRichText(text.replace('[', '(').replace(']', ')'));
  }

  /**
   * Builds the markup that renders the input symbols of an action.
   *
   * @param action the action
   * @return rich text markup
   */
  private static String inputTag(ActionId action) {
    return "[action id=" + action + " all]";
  }
}
