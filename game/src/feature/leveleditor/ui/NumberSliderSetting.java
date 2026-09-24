package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A bounded numeric setting edited with a horizontal slider. */
public final class NumberSliderSetting extends EditorSetting {

  private static final float SLIDER_STEP = 0.000001f;
  private static final float COMMIT_DELAY_SECONDS = 0.12f;

  private final Supplier<Float> getter;
  private final Consumer<Float> setter;
  private final float min;
  private final float max;
  private final float step;
  private final Slider slider;
  private final RichLabel valueLabel;
  private final Timer.Task pendingCommit =
      new Timer.Task() {
        @Override
        public void run() {
          if (getStage() == null || !hasPendingValue) {
            hasPendingValue = false;
            return;
          }
          float value = pendingValue;
          hasPendingValue = false;
          setter.accept(value);
          lastCommittedValue = value;
          if (getStage() == null) {
            showValue(value);
            return;
          }
          lastCommittedValue = checked(getter.get());
          refresh();
        }
      };

  private boolean updating;
  private boolean hasPendingValue;
  private float pendingValue;
  private float lastCommittedValue;

  /**
   * Creates a continuous slider setting with bounds from zero to one.
   *
   * @param label the setting name shown above the control
   * @param getter supplies the current value
   * @param setter applies a new value
   */
  public NumberSliderSetting(String label, Supplier<Float> getter, Consumer<Float> setter) {
    this(label, 0f, 1f, 0f, getter, setter);
  }

  /**
   * Creates a slider setting.
   *
   * @param label the setting name shown above the control
   * @param min the minimum value
   * @param max the maximum value
   * @param step quantization step, or zero for continuous values
   * @param getter supplies the current value
   * @param setter applies a new value
   */
  public NumberSliderSetting(
      String label,
      float min,
      float max,
      float step,
      Supplier<Float> getter,
      Consumer<Float> setter) {
    super(label);
    if (!Float.isFinite(min)
        || !Float.isFinite(max)
        || !Float.isFinite(step)
        || min > max
        || step < 0f) {
      throw new IllegalArgumentException("invalid number slider bounds or step");
    }
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    this.min = min;
    this.max = max;
    this.step = step;

    slider = new Slider(0f, 1f, SLIDER_STEP, false, UIUtils.defaultSkin());
    slider.setDisabled(min == max);
    valueLabel =
        new RichLabel(
            format(checked(getter.get())), DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(16));
    lastCommittedValue = checked(getter.get());
    valueLabel.setAlignment(Align.right);
    valueLabel.setWrap(false);
    slider.setValue(toSliderValue(lastCommittedValue));
    slider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (updating) return;
            float value = quantize(fromSliderValue(slider.getValue()));
            pendingValue = value;
            hasPendingValue = true;
            valueLabel.setText(format(value));
            if (pendingCommit.isScheduled()) pendingCommit.cancel();
            Timer.schedule(pendingCommit, COMMIT_DELAY_SECONDS);
          }
        });

    row();
    add(slider).growX().height(28f);
    add(valueLabel).width(50f).padLeft(5f);
  }

  /**
   * Returns the current backing value.
   *
   * @return current value
   */
  public float value() {
    lastCommittedValue = checked(getter.get());
    return lastCommittedValue;
  }

  /**
   * Sets the slider value, clamping and quantizing it to the descriptor.
   *
   * @param value the new finite value
   */
  public void value(float value) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException("value must be finite");
    }
    float normalized = quantize(value);
    pendingCommit.cancel();
    hasPendingValue = false;
    setter.accept(normalized);
    lastCommittedValue = normalized;
    if (getStage() == null) {
      showValue(normalized);
      return;
    }
    refresh();
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) {
      pendingCommit.cancel();
      hasPendingValue = false;
      showValue(lastCommittedValue);
    }
  }

  /** Synchronizes the displayed control with the current value. */
  public void refresh() {
    if (hasPendingValue) return;
    float current = checked(getter.get());
    lastCommittedValue = current;
    showValue(current);
  }

  private void showValue(float value) {
    updating = true;
    try {
      slider.setValue(toSliderValue(value));
      valueLabel.setText(format(value));
    } finally {
      updating = false;
    }
  }

  private float checked(Float value) {
    if (value == null || !Float.isFinite(value) || value < min || value > max) {
      throw new IllegalStateException("number slider getter returned an invalid value");
    }
    return quantize(value);
  }

  private float quantize(float value) {
    value = Math.max(min, Math.min(max, value));
    if (step == 0f || min == max) return value;

    return quantizedDecimal(value).floatValue();
  }

  private BigDecimal quantizedDecimal(float value) {
    BigDecimal decimalMin = decimal(min);
    BigDecimal decimalMax = decimal(max);
    BigDecimal decimalStep = decimal(step);
    BigDecimal decimalValue = decimal(value).max(decimalMin).min(decimalMax);

    BigDecimal count =
        decimalValue
            .subtract(decimalMin)
            .divide(decimalStep, 0, RoundingMode.HALF_UP);
    BigDecimal nearest = decimalMin.add(count.multiply(decimalStep));
    if (nearest.compareTo(decimalMax) > 0) nearest = decimalMax;

    BigDecimal maxDistance = decimalMax.subtract(decimalValue).abs();
    BigDecimal nearestDistance = nearest.subtract(decimalValue).abs();
    return maxDistance.compareTo(nearestDistance) < 0 ? decimalMax : nearest;
  }

  private static BigDecimal decimal(float value) {
    // Float.toString preserves the intended decimal value of UI inputs such as 0.1f.
    return new BigDecimal(Float.toString(value));
  }

  private float toSliderValue(float value) {
    if (min == max) return 0f;
    return (float) (((double) value - min) / ((double) max - min));
  }

  private float fromSliderValue(float sliderValue) {
    if (min == max) return min;
    return (float) ((double) min + sliderValue * ((double) max - min));
  }

  private String format(float value) {
    if (step == 0f) return Float.toString(value);
    return quantizedDecimal(value).stripTrailingZeros().toPlainString();
  }
}
