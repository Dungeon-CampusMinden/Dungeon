package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.sound.CoreSounds;
import core.sound.Sounds;

/** A concrete implementation of SettingValue for boolean settings. */
public class BoolSetting extends SettingValue<Boolean> {

  /**
   * Creates a new BoolSetting with the specified translation key and default value.
   *
   * @param translationKey the translation key of the setting label
   * @param defaultValue the default boolean value for the setting
   */
  public BoolSetting(String translationKey, boolean defaultValue) {
    super(translationKey, defaultValue);
  }

  @Override
  public Actor toUIActor() {
    RichLabel label = new RichLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    label.setWrap(false);

    CheckBox checkBox = new CheckBox("", UIUtils.defaultSkin());
    checkBox.setChecked(value());
    checkBox.align(Align.left);

    checkBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(checkBox.isChecked());
            float pitch = value() ? 1.0f : 0.8f;
            Sounds.play(CoreSounds.SETTINGS_TOGGLE_CLICK, pitch);
          }
        });

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(checkBox).width(310);

    return table;
  }
}
