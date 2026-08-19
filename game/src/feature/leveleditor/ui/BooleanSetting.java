package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** A labeled checkbox setting backed by a getter and setter. */
public class BooleanSetting extends Table {

  private final BooleanSupplier getter;
  private final CheckBox checkBox;

  /**
   * Creates a boolean setting.
   *
   * @param label the text shown next to the checkbox.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   */
  public BooleanSetting(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
    this.getter = getter;
    checkBox = new CheckBox("", UIUtils.defaultSkin());
    checkBox.setChecked(getter.getAsBoolean());
    checkBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            setter.accept(checkBox.isChecked());
          }
        });

    setTouchable(Touchable.enabled);
    add(Scene2dElementFactory.createLabel(label, 16, ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left();
    add(checkBox).right();
  }

  /** Synchronizes the displayed value with the current setting. */
  public void refresh() {
    boolean current = getter.getAsBoolean();
    if (current != checkBox.isChecked()) {
      checkBox.setChecked(current);
    }
  }
}
