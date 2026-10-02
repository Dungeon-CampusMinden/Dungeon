package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogDesign;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A labeled text field for a bounded, finite floating-point setting. */
public class FloatSetting extends EditorSetting {

  public static final int FONT_SIZE = 16;
  private static final float HORIZONTAL_PADDING = 10f;

  private final Supplier<Float> getter;
  private final Consumer<Float> setter;
  private final float min;
  private final float max;
  private final TextField textField;

  /**
   * Creates a float setting.
   *
   * @param label the text shown above the text field
   * @param min the smallest allowed value
   * @param max the largest allowed value
   * @param getter supplies the current value
   * @param setter applies a new value
   */
  public FloatSetting(
      String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
    this(label, min, max, getter, setter, true, false);
  }

  /**
   * Creates a float setting with optional commit-on-submit behavior.
   *
   * @param label the text shown above the text field
   * @param min the smallest allowed value
   * @param max the largest allowed value
   * @param getter supplies the current value
   * @param setter applies a new value
   * @param commitOnFocusLost whether to commit only on Enter or focus loss
   * @param isNested whether the setting is nested inside another setting
   */
  public FloatSetting(
      String label,
      float min,
      float max,
      Supplier<Float> getter,
      Consumer<Float> setter,
      boolean commitOnFocusLost,
      boolean isNested) {
    super(label, isNested);
    if (!Float.isFinite(min) || !Float.isFinite(max) || min > max) {
      throw new IllegalArgumentException("invalid float bounds");
    }
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    this.min = min;
    this.max = max;

    float initial = checked(getter.get());
    textField = Scene2dElementFactory.createTextField(format(initial));
    TextField.TextFieldStyle style = new TextField.TextFieldStyle(textField.getStyle());
    style.font = FontHelper.getFont(DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(FONT_SIZE));
    style.messageFont = style.font;
    style.background.setLeftWidth(10);
    if (style.focusedBackground != null) {
      style.focusedBackground.setLeftWidth(10);
    }
    if (style.disabledBackground != null) {
      style.disabledBackground.setLeftWidth(10);
    }
    textField.setStyle(style);
    if (commitOnFocusLost) {
      textField.setTextFieldListener(
          (field, character) -> {
            if (character == '\r' || character == '\n') commitText();
          });
      textField.addListener(
          new FocusListener() {
            @Override
            public void keyboardFocusChanged(
                FocusListener.FocusEvent event, Actor actor, boolean focused) {
              if (!focused && !commitText()) refresh();
            }
          });
    } else {
      Scene2dElementFactory.addTextFieldChangeListener(textField, this::applyText);
    }

    row();
    add(textField).growX().minWidth(0).height(40f).padTop(4f);
  }

  /**
   * Returns the current backing value.
   *
   * @return current value
   */
  public float value() {
    return checked(getter.get());
  }

  /**
   * Applies a value after clamping it to the configured bounds.
   *
   * @param value new finite value
   */
  public void value(float value) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException("value must be finite");
    }
    setter.accept(Math.max(min, Math.min(max, value)));
    refresh();
  }

  /** Synchronizes the displayed value with the current setting. */
  public void refresh() {
    if (textField.hasKeyboardFocus()) return;

    String current = format(value());
    if (!current.equals(textField.getText())) {
      textField.setText(current);
    }
  }

  private void applyText(String text) {
    try {
      float value = Float.parseFloat(text);
      if (Float.isFinite(value) && value >= min && value <= max) {
        setter.accept(value);
      }
    } catch (NumberFormatException ignored) {
      // Partial values such as "-" are allowed while the user is typing.
    }
  }

  private boolean commitText() {
    try {
      float value = Float.parseFloat(textField.getText());
      if (Float.isFinite(value) && value >= min && value <= max) {
        setter.accept(value);
        return true;
      }
    } catch (NumberFormatException ignored) {
      // Partial values such as "-" are allowed while the user is typing.
    }
    return false;
  }

  private float checked(Float value) {
    if (value == null || !Float.isFinite(value) || value < min || value > max) {
      throw new IllegalStateException("float setting getter returned an invalid value");
    }
    return value;
  }

  private static String format(float value) {
    return Float.toString(value);
  }

  private static Drawable withHorizontalPadding(Drawable drawable) {
    if (!(drawable instanceof NinePatchDrawable ninePatch)) return drawable;

    NinePatchDrawable adjusted = new NinePatchDrawable(ninePatch);
    adjusted.setPadding(
        adjusted.getPatch().getPadTop(),
        HORIZONTAL_PADDING,
        adjusted.getPatch().getPadBottom(),
        HORIZONTAL_PADDING);
    return adjusted;
  }
}
