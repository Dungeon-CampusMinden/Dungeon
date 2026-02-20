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

public class EnumSetting<E extends Enum<E>> extends SettingValue<E> {

  private final Array<E> values;
  private final Function<E, String> labelFormatter;

  public EnumSetting(String name, E defaultValue) {
    this(name, defaultValue, defaultValue.getDeclaringClass().getEnumConstants(), null);
  }

  public EnumSetting(String name, E defaultValue, E[] subset) {
    this(name, defaultValue, subset, null);
  }

  public EnumSetting(String name, E defaultValue, Collection<E> subset) {
    this(name, defaultValue, subset.toArray((E[]) new Enum[0]), null);
  }

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
            Sounds.playLocal(CoreSounds.SETTINGS_ENUM_VALUE_SELECTED, 1.0f);
          }
        });

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(selectBox).left().width(310);

    return table;
  }
}
