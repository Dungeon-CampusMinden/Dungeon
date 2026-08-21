package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A labeled integer setting consisting of a minus button, a read-only display of the current value,
 * and a plus button.
 *
 * <p>The value is always clamped into the configured range.
 */
public class NumberSetting extends Table {

  private static final int FONT_SIZE = 16;
  private static final float BUTTON_SIZE = 30f;
  private static final float VALUE_WIDTH = 50f;

  private final RichLabel valueLabel;
  private final IntSupplier getter;
  private final IntConsumer setter;
  private final int min;
  private final int max;

  private int lastValue;

  /**
   * Creates a new number setting.
   *
   * @param label the text shown in front of the controls.
   * @param min the smallest allowed value.
   * @param max the largest allowed value.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   */
  public NumberSetting(String label, int min, int max, IntSupplier getter, IntConsumer setter) {
    this.getter = getter;
    this.setter = setter;
    this.min = min;
    this.max = max;
    this.lastValue = getter.getAsInt();

    Label name =
        Scene2dElementFactory.createLabel(
            label,
            FontSpec.of(Scene2dElementFactory.FONT_PATH, FONT_SIZE, ModeDetailsPanel.TEXT_COLOR));

    TextButton minus = Scene2dElementFactory.createButton("-", "default", FONT_SIZE + 4);
    minus.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(value() - 1);
          }
        });

    valueLabel =
        new RichLabel(String.valueOf(lastValue), DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(16));
    valueLabel.setAlignment(Align.center);
    Table valueContainer = new Table();
    valueContainer.setBackground(UIUtils.defaultSkin().getDrawable("generic-area"));
    valueContainer.add(valueLabel).grow();

    TextButton plus = Scene2dElementFactory.createButton("+", "default", FONT_SIZE + 4);
    plus.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(value() + 1);
          }
        });

    add(name).growX().left();
    add(minus).size(BUTTON_SIZE).padRight(4f);
    add(valueContainer).width(VALUE_WIDTH).height(BUTTON_SIZE).padRight(4f);
    add(plus).size(BUTTON_SIZE);
  }

  /**
   * Gets the current value of this setting.
   *
   * @return the current value.
   */
  public int value() {
    return getter.getAsInt();
  }

  /**
   * Sets the value of this setting, clamped into the configured range.
   *
   * @param value the new value.
   */
  public void value(int value) {
    setter.accept(Math.max(min, Math.min(max, value)));
    refresh();
  }

  /** Synchronizes the displayed value with the current value. */
  public void refresh() {
    int current = getter.getAsInt();
    if (current == lastValue) return;
    lastValue = current;
    valueLabel.setText(String.valueOf(current));
  }
}
