package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;

public class BoolSetting extends SettingValue<Boolean> {

  public BoolSetting(String name, boolean defaultValue) {
    super(name, defaultValue);
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);

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
