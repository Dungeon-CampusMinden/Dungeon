package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A text-editable RGBA color setting with a live color and alpha swatch. */
public final class ColorSetting extends EditorSetting {

  private final Supplier<Color> getter;
  private final Consumer<Color> setter;
  private final Consumer<String> invalidCommit;
  private final TextField textField;
  private final Image swatch;
  private final Label alphaLabel;

  /**
   * Creates a color setting.
   *
   * @param label setting label
   * @param getter current color
   * @param setter applies a color
   * @param invalidCommit called when an invalid color is submitted or loses focus
   */
  public ColorSetting(
      String label,
      Supplier<Color> getter,
      Consumer<Color> setter,
      Consumer<String> invalidCommit) {
    super(label);
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    this.invalidCommit = Objects.requireNonNull(invalidCommit, "invalidCommit");
    Color initial = checked(getter.get());

    textField = Scene2dElementFactory.createTextField(format(initial), 16);
    textField.setMaxLength(9);
    textField.setTextFieldListener(
        (field, character) -> {
          if (character == '\r' || character == '\n') commitText();
        });
    textField.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            parse(textField.getText()).ifPresent(ColorSetting.this::updateSwatch);
          }
        });
    textField.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(
              FocusListener.FocusEvent event, Actor actor, boolean focused) {
            if (!focused) commitText();
          }
        });

    swatch = new Image(UIUtils.defaultSkin().getDrawable("generic-area"));
    alphaLabel = Scene2dElementFactory.createLabel("", 12, ModeDetailsPanel.TEXT_COLOR);
    Table swatchContainer = new Table();
    swatchContainer.add(swatch).size(32f, 30f);
    swatchContainer.add(alphaLabel).padLeft(5f);
    row();
    add(textField).growX().height(40f).padTop(4f);
    add(swatchContainer).padLeft(6f);
    updateSwatch(initial);
  }

  /** Synchronizes the field and swatch with the current color. */
  public void refresh() {
    Color current = checked(getter.get());
    if (!textField.hasKeyboardFocus() && !format(current).equals(textField.getText())) {
      textField.setText(format(current));
    }
    updateSwatch(current);
  }

  private void commitText() {
    java.util.Optional<Color> parsed = parse(textField.getText());
    if (parsed.isPresent()) {
      apply(parsed.get());
      return;
    }
    invalidCommit.accept("Color must be eight hexadecimal RGBA digits, optionally prefixed by #.");
    Color current = checked(getter.get());
    textField.setText(format(current));
    updateSwatch(current);
  }

  private void apply(Color color) {
    setter.accept(color.cpy());
    updateSwatch(color);
  }

  private void updateSwatch(Color color) {
    swatch.setColor(color.r, color.g, color.b, color.a);
    alphaLabel.setText("A " + Math.round(color.a * 100f) + "%");
  }

  private static Color checked(Color color) {
    Objects.requireNonNull(color, "color");
    return color.cpy();
  }

  private static String format(Color color) {
    return String.format(
        Locale.ROOT,
        "%02X%02X%02X%02X",
        Math.round(color.r * 255f),
        Math.round(color.g * 255f),
        Math.round(color.b * 255f),
        Math.round(color.a * 255f));
  }

  private static java.util.Optional<Color> parse(String text) {
    if (text == null) return java.util.Optional.empty();
    String value = text.startsWith("#") ? text.substring(1) : text;
    if (value.length() != 8) return java.util.Optional.empty();
    for (int i = 0; i < value.length(); i++) {
      char digit = value.charAt(i);
      if (!((digit >= '0' && digit <= '9')
          || (digit >= 'a' && digit <= 'f')
          || (digit >= 'A' && digit <= 'F'))) {
        return java.util.Optional.empty();
      }
    }
    int red = Integer.parseInt(value.substring(0, 2), 16);
    int green = Integer.parseInt(value.substring(2, 4), 16);
    int blue = Integer.parseInt(value.substring(4, 6), 16);
    int alpha = Integer.parseInt(value.substring(6, 8), 16);
    return java.util.Optional.of(new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f));
  }
}
