package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogDesign;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A labeled integer setting consisting of a minus button, an editable value, and a plus button.
 *
 * <p>The value is always clamped into the configured range.
 */
public class IntegerSetting extends EditorSetting {

  private static final int FONT_SIZE = 16;
  private static final float BUTTON_SIZE = 30f;

  private final TextField valueField;
  private final IntSupplier getter;
  private final IntConsumer setter;
  private final int min;
  private final int max;

  /**
   * Creates a new number setting.
   *
   * @param label the text shown in front of the controls.
   * @param min the smallest allowed value.
   * @param max the largest allowed value.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   */
  public IntegerSetting(String label, int min, int max, IntSupplier getter, IntConsumer setter) {
    super(label);
    this.getter = getter;
    this.setter = setter;
    this.min = min;
    this.max = max;
    TextButton minus = Scene2dElementFactory.createButton("-", "default", FONT_SIZE + 4);
    minus.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(value() == min ? min : value() - 1);
          }
        });

    valueField = Scene2dElementFactory.createTextField(String.valueOf(getter.getAsInt()));
    TextField.TextFieldStyle style = new TextField.TextFieldStyle(valueField.getStyle());
    style.font = FontHelper.getFont(DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(FONT_SIZE));
    style.messageFont = style.font;
    style.background.setLeftWidth(10);
    if (style.focusedBackground != null) {
      style.focusedBackground.setLeftWidth(10);
    }
    if (style.disabledBackground != null) {
      style.disabledBackground.setLeftWidth(10);
    }
    valueField.setStyle(style);
    valueField.setTextFieldListener(
        (field, character) -> {
          if (character == '\r' || character == '\n') commitText();
        });
    valueField.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(
              FocusListener.FocusEvent event, Actor actor, boolean focused) {
            if (!focused && !commitText()) refresh();
          }
        });

    TextButton plus = Scene2dElementFactory.createButton("+", "default", FONT_SIZE + 4);
    plus.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            value(value() == max ? max : value() + 1);
          }
        });

    add(minus).size(BUTTON_SIZE).padRight(4f);
    add(valueField).width(80f).height(40f).padRight(4f);
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
    if (valueField.hasKeyboardFocus()) return;
    String current = String.valueOf(getter.getAsInt());
    if (!current.equals(valueField.getText())) valueField.setText(current);
  }

  private boolean commitText() {
    try {
      int value = Integer.parseInt(valueField.getText());
      if (value >= min && value <= max) {
        setter.accept(value);
        return true;
      }
    } catch (NumberFormatException ignored) {
      // Partial values such as "-" are allowed while the user is typing.
    }
    return false;
  }
}
