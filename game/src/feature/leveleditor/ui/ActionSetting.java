package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.Scene2dElementFactory;

/** A full-width button setting that performs an action when activated. */
public class ActionSetting extends Table {

  private static final int FONT_SIZE = 16;

  /**
   * Creates an action setting.
   *
   * @param label the text shown on the button.
   * @param action the action performed when the button is activated.
   */
  public ActionSetting(String label, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(label, "default", FONT_SIZE);
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
