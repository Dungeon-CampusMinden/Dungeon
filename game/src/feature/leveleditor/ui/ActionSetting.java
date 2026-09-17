package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.Scene2dElementFactory;

/** A full-width button setting that performs an action when activated. */
public class ActionSetting extends EditorSetting {

  /**
   * Creates an action setting.
   *
   * @param label the text shown on the button.
   * @param action the action performed when the button is activated.
   */
  public ActionSetting(String label, Runnable action) {
    super(label);
    TextButton button = Scene2dElementFactory.createButton(label, "default", 18);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    add(button).growX().height(40f);
  }
}
