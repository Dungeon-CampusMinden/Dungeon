package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import java.util.function.Function;

/** A SettingValue that represents an integer value that can be adjusted with a slider in the UI. */
public class IntSliderSetting extends SettingValue<Integer> {

  private final Function<Integer, String> labelFormatter;
  private final int min;
  private final int max;
  private final int step;

  /**
   * Create a new IntSliderSetting with the given name and default value.
   *
   * @param name the name of the setting to display in the UI
   * @param defaultValue the default integer value for this setting
   */
  public IntSliderSetting(String name, int defaultValue) {
    this(name, defaultValue, 0, 100, 1, null);
  }

  /**
   * Create a new IntSliderSetting with the given name, default value, minimum, maximum, and step
   * size.
   *
   * @param name the name of the setting to display in the UI
   * @param defaultValue the default integer value for this setting
   * @param min the minimum integer value for this setting
   * @param max the maximum integer value for this setting
   * @param step the step size for the slider (e.g. 1 for integers, 10 for tens, etc.)
   */
  public IntSliderSetting(String name, int defaultValue, int min, int max, int step) {
    this(name, defaultValue, min, max, step, null);
  }

  /**
   * Create a new IntSliderSetting with the given name, default value, minimum, maximum, step size,
   * and label formatter.
   *
   * @param name the name of the setting to display in the UI
   * @param defaultValue the default integer value for this setting
   * @param min the minimum integer value for this setting
   * @param max the maximum integer value for this setting
   * @param step the step size for the slider (e.g. 1 for integers, 10 for tens, etc.)
   * @param labelFormatter a function that takes the current integer value and returns a formatted
   *     string for display next to the slider
   */
  public IntSliderSetting(
      String name,
      int defaultValue,
      int min,
      int max,
      int step,
      Function<Integer, String> labelFormatter) {
    super(name, defaultValue);

    this.min = min;
    this.max = max;
    this.step = step;

    if (labelFormatter == null) {
      labelFormatter = Object::toString;
    }
    this.labelFormatter = labelFormatter;
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    Label valueLabel =
        Scene2dElementFactory.createLabel(labelFormatter.apply(value()), 24, Color.BLACK);
    valueLabel.setAlignment(Align.center);
    Slider slider = new Slider(min, max, step, false, UIUtils.defaultSkin(), "clean-horizontal");
    slider.setValue(value());
    slider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (actor instanceof Slider slider) {
              int val = (int) slider.getValue();
              if (val == value()) return;
              value(val);
              valueLabel.setText(labelFormatter.apply(val));
              Sounds.play(CoreSounds.SETTINGS_SLIDER_STEP);
            }
          }
        });

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(slider).center().width(250).padRight(10);
    table.add(valueLabel).width(50).center();

    return table;
  }
}
