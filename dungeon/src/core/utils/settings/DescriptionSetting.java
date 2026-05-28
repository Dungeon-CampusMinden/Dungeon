package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import core.utils.Scene2dElementFactory;

/**
 * A non-interactive setting that renders a descriptive text in the settings menu.
 *
 * <p>Unlike {@link SectionDividerSetting}, this setting is intended for longer explanatory text
 * shown in a smaller font. It cannot be edited by the user.
 */
public class DescriptionSetting extends SettingValue<String> {

  /** Font size used for the description text. */
  private static final int FONT_SIZE = 14;

  /**
   * Creates a new DescriptionSetting with the specified text.
   *
   * @param text the description text to display in the settings menu
   */
  public DescriptionSetting(String text) {
    super(text, "");
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), FONT_SIZE, Color.BLACK);
    label.setAlignment(Align.center);
    label.setWrap(true);

    Table table = new Table();
    table.setTouchable(Touchable.disabled);
    table.add(label).growX().pad(2, 10, 2, 10);

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
  public void value(String value) {
    throw new IllegalStateException();
  }
}

