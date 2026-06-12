package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.hud.elements.RichLabel;
import core.utils.Scene2dElementFactory;

/** A non-interactive setting used to visually separate sections of the settings menu. */
public class SectionDividerSetting extends SettingValue<Boolean> {

  /**
   * Creates a new SectionDividerSetting with the specified translation key.
   *
   * @param translationKey translation key of the section title shown in the settings menu
   */
  public SectionDividerSetting(String translationKey) {
    super(translationKey, false);
  }

  @Override
  public Actor toUIActor() {
    RichLabel label = new RichLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.center);
    label.setWrap(false);

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

  /**
   * This method is overridden to prevent changing the value of this setting, as it is not meant to
   * be interactive.
   *
   * @param value the new value to set (ignored)
   * @throws IllegalStateException always thrown to indicate that this setting cannot be changed
   */
  @Override
  public void value(Boolean value) {
    throw new IllegalStateException();
  }
}
