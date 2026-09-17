package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogDesign;
import feature.leveleditor.PrefabMode;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A labeled text field setting backed by a getter and setter. */
public class StringSetting extends Table {

  private static final int FONT_SIZE = 16;
  private static final float HORIZONTAL_PADDING = 10f;

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
      String label,
      Supplier<String> getter,
      Consumer<String> setter,
      boolean commitOnFocusLost) {
    this.getter = getter;
    this.setter = setter;
    textField = Scene2dElementFactory.createTextField(Objects.requireNonNullElse(getter.get(), ""));
    TextField.TextFieldStyle style = new TextField.TextFieldStyle(textField.getStyle());
    style.font = FontHelper.getFont(DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(FONT_SIZE));
    style.messageFont = style.font;
    style.background = withHorizontalPadding(style.background);
    style.focusedBackground = withHorizontalPadding(style.focusedBackground);
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
              if (!focused) commitText();
            }
          });
    } else {
      Scene2dElementFactory.addTextFieldChangeListener(textField, setter);
    }

    add(Scene2dElementFactory.createLabel(label, PrefabMode.PROPERTY_LABEL_SIZE, ModeDetailsPanel.TEXT_COLOR))
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

  private void commitText() {
    String value = textField.getText();
    String current = Objects.requireNonNullElse(getter.get(), "");
    if (!Objects.equals(value, current)) setter.accept(value);
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
