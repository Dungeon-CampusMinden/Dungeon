package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/** A full-width button setting that performs an action when activated. */
public class ActionSetting extends EditorSetting {

  /**
   * Creates an action setting.
   *
   * @param label the text shown on the button.
   * @param action the action performed when the button is activated.
   */
  public ActionSetting(String label, Runnable action) {
    this(label, action, true);
  }

  /**
   * Creates an action setting.
   *
   * @param label the text shown on the button.
   * @param action the action performed when the button is activated.
   * @param showStandaloneLabel whether to show the setting label above the button.
   */
  public ActionSetting(String label, Runnable action, boolean showStandaloneLabel) {
    super(label);
    if (!showStandaloneLabel) clearChildren();
    TextButton button = EditorTheme.button(label, EditorTheme.Variant.DEFAULT);
    EditorTheme.onChange(button, action);
    add(button).growX().height(EditorTheme.CONTROL_HEIGHT);
  }
}
