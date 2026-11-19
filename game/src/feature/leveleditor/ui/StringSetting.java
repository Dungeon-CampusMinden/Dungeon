package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A labeled text field setting backed by a getter and setter. */
public class StringSetting extends EditorSetting {

  private static final int FONT_SIZE = 15;

  private final Supplier<String> getter;
  private final Consumer<String> setter;
  private final TextField textField;

  /**
   * Creates a string setting.
   *
   * @param label the text shown above the text field.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   */
  public StringSetting(String label, Supplier<String> getter, Consumer<String> setter) {
    this(label, getter, setter, false);
  }

  /**
   * Creates a string setting with optional commit-on-submit behavior.
   *
   * @param label the text shown above the text field.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   * @param commitOnFocusLost whether to commit only on Enter or focus loss
   */
  public StringSetting(
      String label, Supplier<String> getter, Consumer<String> setter, boolean commitOnFocusLost) {
    super(label);
    this.getter = getter;
    this.setter = setter;
    textField = EditorTheme.textField(Objects.requireNonNullElse(getter.get(), ""), FONT_SIZE);
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
              if (!focused) commitText();
            }
          });
    } else {
      Scene2dElementFactory.addTextFieldChangeListener(textField, setter);
    }

    row();
    add(textField).growX().height(EditorTheme.CONTROL_HEIGHT).padTop(3f);
  }

  /**
   * Gets the current text field value.
   *
   * @return the current text.
   */
  public String value() {
    return textField.getText();
  }

  /** Synchronizes the displayed value with the current setting. */
  public void refresh() {
    String current = Objects.requireNonNullElse(getter.get(), "");
    if (!current.equals(textField.getText())) {
      textField.setText(current);
    }
  }

  private void commitText() {
    String value = textField.getText();
    String current = Objects.requireNonNullElse(getter.get(), "");
    if (!Objects.equals(value, current)) setter.accept(value);
  }
}
