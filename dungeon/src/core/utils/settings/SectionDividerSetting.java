package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import core.utils.Scene2dElementFactory;

public class SectionDividerSetting extends SettingValue<Boolean> {

  public SectionDividerSetting(String name) {
    super(name, false);
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.center);

    Image dividerL = Scene2dElementFactory.createHorizontalDivider();
    Image dividerR = Scene2dElementFactory.createHorizontalDivider();

    Table table = new Table();
    table.setTouchable(Touchable.disabled);

    Table row = new Table();
    row.add(dividerL).growX();
    row.add(label).growX().pad(0, 10, 0, 10);
    row.add(dividerR).growX();
    table.add(row).growX().pad(20, 0, 0, 0);

    return table;
  }

  @Override
  public void value(Boolean value) {
    throw new IllegalStateException();
  }
}
