package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import engine.systems.DrawSystem;
import engine.utils.Cursors;
import feature.hud.elements.RichLabel;
import feature.leveleditor.EditorActions;
import feature.leveleditor.LevelEditorMode;
import feature.leveleditor.LevelProblems;
import feature.prefabs.PrefabProblem;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Bar along the bottom of the screen: the number of problems of the level and the last feedback
 * message of the editor on the left, the state of the active mode in the middle and its controls on
 * the right, with key and mouse symbols.
 *
 * <p>Clicking the number of problems opens the list of the problems, see {@link ProblemsPanel}.
 */
public final class StatusBar extends Table {

  /** Height of the bar. */
  public static final float HEIGHT = 30f;

  private static final int FONT_SIZE = 14;
  private static final float MESSAGE_SECONDS = 4f;
  private static final float FADE_SECONDS = 0.6f;
  // Room kept for the message and the status of the mode.
  private static final float MESSAGE_WIDTH = 560f;

  private final Table problems = new Table();
  // White, so the color of the label sets the color of the text.
  private final Label errorCount = EditorTheme.label("0", FONT_SIZE, Color.WHITE);
  private final Label warningCount = EditorTheme.label("0", FONT_SIZE, Color.WHITE);
  private final Image errorIcon = EditorTheme.iconImage("error", Color.WHITE);
  private final Image warningIcon = EditorTheme.iconImage("warning", Color.WHITE);
  private final BooleanSupplier problemsShown;
  private int shownErrors = -1;
  private int shownWarnings = -1;
  private ClickListener problemsClick;
  private boolean problemsHighlighted;
  private final Label message = EditorTheme.label("", FONT_SIZE, EditorTheme.TEXT);
  private final Label status = EditorTheme.label("", FONT_SIZE, EditorTheme.TEXT_MUTED);
  private final RichLabel hints = new RichLabel("", FONT_SIZE, EditorTheme.TEXT_MUTED, false);
  private LevelEditorMode mode;
  private String hintText = "";
  private final List<String> hintItems = new ArrayList<>();
  private float fittedWidth = -1f;

  /**
   * Creates an empty status bar.
   *
   * @param toggleProblems opens or closes the list of the problems of the level
   * @param problemsShown whether the list of the problems is open
   */
  public StatusBar(Runnable toggleProblems, BooleanSupplier problemsShown) {
    this.problemsShown = problemsShown;
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.flat(new Color(0.06f, 0.065f, 0.08f, 0.94f)));
    pad(0f, 6f, 0f, 12f);
    buildProblems(toggleProblems);
    add(problems).height(HEIGHT - 6f).left().padRight(10f);
    message.setEllipsis(true);
    status.setEllipsis(true);
    hints.setWrap(false);
    hints.setAlignment(Align.right);
    add(message).minWidth(0f).prefWidth(420f).left();
    add(status).minWidth(0f).padLeft(16f).left();
    add().growX();
    add(hints).right();
  }

  /**
   * Returns whether an actor is the entry of the status bar that opens and closes the list of the
   * problems of the level.
   *
   * @param actor actor, e.g. the target of a click
   * @return true if the actor is the entry or a part of it
   */
  public boolean togglesProblems(Actor actor) {
    return actor.isDescendantOf(problems);
  }

  // Looks like plain text, the outline and background only show on hover or while the list is open.
  private void buildProblems(Runnable toggleProblems) {
    problems.setTouchable(Touchable.enabled);
    problems.setUserObject(Cursors.INTERACT);
    problems.pad(0f, 7f, 0f, 7f);
    problems.add(errorCount);
    problems.add(errorIcon).size(13f).padLeft(5f);
    problems.add(warningCount).padLeft(12f);
    problems.add(warningIcon).size(13f).padLeft(5f);
    problemsClick =
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            toggleProblems.run();
          }
        };
    problems.addListener(problemsClick);
  }

  private void updateProblems() {
    int errors = LevelProblems.count(PrefabProblem.Severity.ERROR);
    int warnings = LevelProblems.count(PrefabProblem.Severity.WARNING);
    // More digits take room from the hints.
    if (errors != shownErrors || warnings != shownWarnings) fittedWidth = -1f;
    if (errors != shownErrors) {
      shownErrors = errors;
      errorCount.setText(String.valueOf(errors));
      Color color = errors > 0 ? EditorTheme.ERROR : EditorTheme.TEXT_DISABLED;
      errorCount.setColor(color);
      errorIcon.setColor(color);
    }
    if (warnings != shownWarnings) {
      shownWarnings = warnings;
      warningCount.setText(String.valueOf(warnings));
      Color color = warnings > 0 ? EditorTheme.WARNING : EditorTheme.TEXT_DISABLED;
      warningCount.setColor(color);
      warningIcon.setColor(color);
    }
    boolean highlighted = problemsClick.isOver() || problemsShown.getAsBoolean();
    if (highlighted != problemsHighlighted) {
      problemsHighlighted = highlighted;
      problems.setBackground(
          highlighted ? EditorTheme.rounded(EditorTheme.ROW_OVER, EditorTheme.LINE, 4) : null);
    }
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (LevelEditorSystem.active()) updateProblems();
    LevelEditorMode current = LevelEditorSystem.currentModeInstance();
    if (current != mode) {
      mode = current;
      updateHints();
    }
    if (getWidth() != fittedWidth) fitHints();
    String statusText = mode == null ? "" : Objects.requireNonNullElse(mode.statusText(), "");
    String shaders = "Shaders: " + DrawSystem.shadersActiveLastFrame();
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
          .forEach((action, label) ->
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
    float available =
        getWidth() - MESSAGE_WIDTH - problems.getPrefWidth() - getPadLeft() - getPadRight();
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
