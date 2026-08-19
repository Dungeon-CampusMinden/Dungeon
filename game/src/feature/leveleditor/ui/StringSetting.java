package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A labeled text field setting backed by a getter and setter. */
public class StringSetting extends Table {

  private final Supplier<String> getter;
  private final TextField textField;

  /**
   * Creates a string setting.
   *
   * @param label the text shown above the text field.
   * @param getter supplies the current value.
   * @param setter applies a new value.
   */
  public StringSetting(String label, Supplier<String> getter, Consumer<String> setter) {
    this.getter = getter;
    textField = Scene2dElementFactory.createTextField(Objects.requireNonNullElse(getter.get(), ""));
    Scene2dElementFactory.addTextFieldChangeListener(textField, setter);

    add(Scene2dElementFactory.createLabel(label, 16, ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left()
        .row();
    add(textField).growX().height(40f).padTop(4f);
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
}
