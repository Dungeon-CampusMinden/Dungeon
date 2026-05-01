package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import java.util.Collection;
import java.util.function.Function;

/**
 * A SettingValue implementation for enum types, allowing users to select a value from an enum.
 *
 * @param <E> the enum type that this setting represents
 */
public class EnumSetting<E extends Enum<E>> extends SettingValue<E> {

  private final Array<E> values;
  private final Function<E, String> labelFormatter;

  /**
   * Creates a new EnumSetting with the specified name and default value, using all enum constants
   * as options.
   *
   * @param name the name of the setting
   * @param defaultValue the default enum value for the setting, which must be one of the enum
   *     constants
   */
  public EnumSetting(String name, E defaultValue) {
    this(name, defaultValue, defaultValue.getDeclaringClass().getEnumConstants(), null);
  }

  /**
   * Creates a new EnumSetting with the specified name, default value, and subset of enum constants.
   *
   * @param name the name of the setting
   * @param defaultValue the default enum value for the setting, which must be included in the
   *     subset
   * @param subset an array of enum constants to use as options for this setting, which must include
   *     the default value
   */
  public EnumSetting(String name, E defaultValue, E[] subset) {
    this(name, defaultValue, subset, null);
  }

  /**
   * Creates a new EnumSetting with the specified name, default value, subset of enum constants.
   *
   * @param name the name of the setting
   * @param defaultValue the default enum value for the setting, which must be included in the
   *     subset
   * @param subset a collection of enum constants to use as options for this setting, which must
   *     include the default value
   */
  public EnumSetting(String name, E defaultValue, Collection<E> subset) {
    this(name, defaultValue, subset.toArray((E[]) new Enum[0]), null);
  }

  /**
   * Creates a new EnumSetting with the specified name, default value, subset of enum constants, and
   * label formatter.
   *
   * @param name the name of the setting
   * @param defaultValue the default enum value for the setting, which must be included in the
   *     subset
   * @param subset an array of enum constants to use as options for this setting, which must include
   *     the default value
   * @param labelFormatter a function that converts enum values to display strings in the UI; if
   *     null, the enum's name() method will be used
   */
  public EnumSetting(String name, E defaultValue, E[] subset, Function<E, String> labelFormatter) {
    super(name, defaultValue);

    if (subset == null || subset.length == 0) {
      throw new IllegalArgumentException("EnumSetting requires at least one enum value");
    }

    this.values = new Array<>(subset);

    if (!values.contains(defaultValue, true)) {
      throw new IllegalArgumentException("Default value must be part of the enum subset");
    }

    this.labelFormatter = labelFormatter != null ? labelFormatter : Enum::name;
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);

    SelectBox<E> selectBox = Scene2dElementFactory.createSelectBox(labelFormatter);
    selectBox.setItems(values);
    selectBox.setSelected(value());

    selectBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(selectBox.getSelected());
            Sounds.play(CoreSounds.SETTINGS_ENUM_VALUE_SELECTED, 1.0f);
          }
        });

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(selectBox).left().width(310);

    return table;
  }
}
