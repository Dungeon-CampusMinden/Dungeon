package core.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.utils.Scene2dElementFactory;

import java.util.function.Function;

public class IntSliderSetting extends SettingValue<Integer> {

  private final Function<Integer, String> labelFormatter;
  private final int min;
  private final int max;
  private final int step;

  public IntSliderSetting(String name, int defaultValue) {
    this(name, defaultValue, 0, 100, 1, null);
  }
  public IntSliderSetting(String name, int defaultValue, int min, int max, int step) {
    this(name, defaultValue, min, max, step, null);
  }

  public IntSliderSetting(String name, int defaultValue, int min, int max, int step, Function<Integer, String> labelFormatter) {
    super(name, defaultValue);

    this.min = min;
    this.max = max;
    this.step = step;

    if(labelFormatter == null){
      labelFormatter = Object::toString;
    }
    this.labelFormatter = labelFormatter;
  }

  @Override
  public Actor toUIActor() {
    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    Label valueLabel = Scene2dElementFactory.createLabel(labelFormatter.apply(value()), 24, Color.BLACK);
    valueLabel.setAlignment(Align.center);
    Slider slider = new Slider(min, max, step, false, UIUtils.defaultSkin(), "clean-horizontal");
    slider.setValue(value());
    slider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (actor instanceof Slider slider) {
          int val = (int) slider.getValue();
          value(val);
          valueLabel.setText(labelFormatter.apply(val));
        }
      }
    });

    Table table = new Table();
    table.add(label).right().growX().padRight(10);
    table.add(slider).center().width(250).padRight(10);
    table.add(valueLabel).width(50).center();

    return table;
  }
}
