package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.hud.elements.RichLabel;
import core.language.Translation;

/**
 * A non-interactive setting that renders a descriptive text in the settings menu.
 *
 * <p>Unlike {@link SectionDividerSetting}, this setting is intended for longer explanatory text
 * shown in a smaller font. It cannot be edited by the user.
 */
public class DescriptionSetting extends SettingValue<String> {

  /** Font size used for the description text. */
  private static final int FONT_SIZE = 14;

  private static final Translation TRANSLATION = new Translation();

  private final Object[] templateValues;

  /**
   * Creates a new DescriptionSetting with the specified translation key.
   *
   * @param translationKey translation key of the description text
   */
  public DescriptionSetting(String translationKey) {
    super(translationKey, "");
    this.templateValues = new Object[0];
  }

  /**
   * Creates a new DescriptionSetting with the specified translation key and template values.
   *
   * @param translationKey translation key of the description text
   * @param templateValues positional template values for placeholders like {@code $1}, {@code $2}
   */
  public DescriptionSetting(String translationKey, Object... templateValues) {
    super(translationKey, "");
    this.templateValues = templateValues == null ? new Object[0] : templateValues.clone();
  }

  @Override
  public Actor toUIActor() {
    RichLabel label =
        new RichLabel(TRANSLATION.text(translationKey(), templateValues), FONT_SIZE, Color.BLACK);
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
