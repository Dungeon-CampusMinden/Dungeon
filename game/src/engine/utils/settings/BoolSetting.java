package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.language.Translation;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import java.util.Optional;

/** A concrete implementation of SettingValue for boolean settings. */
public class BoolSetting extends SettingValue<Boolean> {

  private static final Translation TRANSLATION = new Translation();
  private static final int DESCRIPTION_FONT_SIZE = 14;

  private final Optional<String> descriptionKey;

  /**
   * Creates a new BoolSetting with the specified translation key and default value.
   *
   * @param translationKey the translation key of the setting label
   * @param defaultValue the default boolean value for the setting
   */
  public BoolSetting(String translationKey, boolean defaultValue) {
    super(translationKey, defaultValue);
    this.descriptionKey = Optional.empty();
  }

  /**
   * Creates a new BoolSetting whose checkbox carries a short description of what it does.
   *
   * @param translationKey the translation key of the setting label
   * @param defaultValue the default boolean value for the setting
   * @param descriptionKey the translation key of the description shown beside the checkbox
   */
  public BoolSetting(String translationKey, boolean defaultValue, String descriptionKey) {
    super(translationKey, defaultValue);
    this.descriptionKey = Optional.of(descriptionKey);
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
            Sounds.playUi(CoreSounds.SETTINGS_TOGGLE_CLICK, pitch);
          }
        });

    Table box = new Table();
    box.add(checkBox).left();
    descriptionKey.ifPresent(
        key -> {
          RichLabel description =
              new RichLabel(TRANSLATION.text(key), DESCRIPTION_FONT_SIZE, Color.BLACK);
          description.setAlignment(Align.left);
          box.add(description).left().padLeft(10);
        });
    box.add().growX();

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(box).width(310);

    return table;
  }
}
