package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bar of view options shown next to the mode panel, e.g. which names the prefab mode shows in the
 * world. Every option is a button showing its name and current value; clicking it switches to the
 * next value.
 */
public final class ViewOptionsBar extends Table {

  private final List<Runnable> refreshers = new ArrayList<>();

  /** Creates an empty bar. */
  public ViewOptionsBar() {
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.panel());
    pad(5f);
    Image eye = new Image(EditorTheme.icon("eye", EditorTheme.TEXT_MUTED));
    eye.setScaling(Scaling.fit);
    add(eye).size(16f).padLeft(6f).padRight(4f);
    defaults().height(34f).padLeft(1f).padRight(1f);
  }

  /**
   * Adds an option.
   *
   * @param name option name, shown in front of the value
   * @param value supplies the displayed current value
   * @param next switches to the next value
   * @param tooltip explains the option and its values
   * @return this bar
   */
  public ViewOptionsBar option(String name, Supplier<String> value, Runnable next, String tooltip) {
    Button.ButtonStyle style = new Button.ButtonStyle();
    style.up = EditorTheme.rounded(new Color(0f, 0f, 0f, 0f), null, 4);
    style.over = EditorTheme.rounded(EditorTheme.ROW_OVER, null, 4);
    style.down = EditorTheme.rounded(EditorTheme.BUTTON_DOWN, null, 4);
    Button button = new Button(style);
    button.setUserObject(engine.utils.Cursors.INTERACT);
    button.pad(0f, 9f, 0f, 9f);
    button.add(EditorTheme.label(name, 13, EditorTheme.TEXT_MUTED)).padRight(6f);
    Label current = EditorTheme.boldLabel(value.get(), 14, EditorTheme.TEXT);
    button.add(current);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            next.run();
            current.setText(value.get());
          }
        });
    EditorTheme.tooltip(button, tooltip);
    refreshers.add(() -> current.setText(value.get()));
    add(button);
    return this;
  }

  /** Updates the displayed values, e.g. after they were changed by a key. */
  public void refresh() {
    refreshers.forEach(Runnable::run);
  }
}
