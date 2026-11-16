package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import engine.language.Translation;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.Scene2dElementFactory;
import feature.hud.elements.RichLabel;

/**
 * The limit of the frame rate in frames per second, {@link #UNLIMITED} for none. The limit only
 * changes how often the game draws; the world ticks at its own fixed rate.
 *
 * <p>The player picks a preset from the list or types a number into the same field. Typing ends
 * with Enter or a click elsewhere; the list opens at the arrow.
 */
public class FrameLimitSetting extends SettingValue<Integer> {

  /** No limit: as many frames as the device and its display allow. */
  public static final int UNLIMITED = 0;

  /** The limit every game starts with. */
  public static final int DEFAULT = 60;

  private static final int[] PRESETS = {UNLIMITED, 30, 60, 120, 144};
  // The field takes limits up to 999; value(Integer) accepts any positive number.
  private static final int MAX_DIGITS = 3;
  private static final Translation TRANSLATION = new Translation();

  private final String unlimitedKey;

  /**
   * Creates the setting with the {@link #DEFAULT} limit.
   *
   * @param translationKey the translation key of the setting label
   * @param unlimitedKey the translation key of the entry for no limit
   */
  public FrameLimitSetting(String translationKey, String unlimitedKey) {
    super(translationKey, DEFAULT);
    this.unlimitedKey = unlimitedKey;
  }

  /**
   * Sets the limit.
   *
   * @param fps the frames per second, or {@link #UNLIMITED}
   */
  @Override
  public void value(Integer fps) {
    if (fps < 0) throw new IllegalArgumentException("The frame limit must not be negative");
    super.value(fps);
  }

  @Override
  public Actor toUIActor() {
    RichLabel label = new RichLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    label.setWrap(false);

    TextField field = Scene2dElementFactory.createTextField("");
    // While the player types, the field shows the number in place of the selected entry.
    SelectBox<Integer> selectBox =
        Scene2dElementFactory.createSelectBox(
            fps ->
                field.hasKeyboardFocus()
                    ? ""
                    : fps == UNLIMITED ? TRANSLATION.text(unlimitedKey) : String.valueOf(fps));
    // Only a pick by the player counts as a change, not the list following the typed value.
    selectBox.getSelection().setProgrammaticChangeEvents(false);
    showValue(selectBox);

    // The field lies on the select box and borrows its look; the arrow stays free for the list.
    TextField.TextFieldStyle style = new TextField.TextFieldStyle(field.getStyle());
    style.font = selectBox.getStyle().font;
    style.fontColor = selectBox.getStyle().fontColor;
    style.background = null;
    style.focusedBackground = null;
    field.setStyle(style);
    field.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
    field.setMaxLength(MAX_DIGITS);

    selectBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            apply(selectBox.getSelected(), selectBox);
          }
        });
    field.setTextFieldListener(
        (typedIn, character) -> {
          if (character == '\r' || character == '\n') typedIn.getStage().setKeyboardFocus(null);
        });
    // A click anywhere but in the field ends typing; libGDX keeps the focus otherwise.
    InputListener clickElsewhere =
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (event.getTarget() != field) event.getStage().setKeyboardFocus(null);
            return false;
          }
        };
    field.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
            if (focused) {
              // The current limit is selected, so typing replaces it.
              int fps = value();
              boolean typeable = fps != UNLIMITED && String.valueOf(fps).length() <= MAX_DIGITS;
              field.setText(typeable ? String.valueOf(fps) : "");
              field.selectAll();
              field.getStage().addCaptureListener(clickElsewhere);
            } else {
              field.getStage().removeCaptureListener(clickElsewhere);
              String typed = field.getText();
              field.setText("");
              // An empty field and 0 are no limit a player can type; the old limit stays.
              if (!typed.isEmpty() && Integer.parseInt(typed) > 0) {
                apply(Integer.parseInt(typed), selectBox);
              }
            }
          }
        });

    Drawable box = selectBox.getStyle().background;
    Table typing = new Table();
    typing.setTouchable(Touchable.childrenOnly);
    typing.add(field).grow().padLeft(box.getLeftWidth()).padRight(box.getRightWidth());

    Table table = new Table();
    // A label wider than its column grows to the left, so the controls of all rows line up.
    table.right();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);
    table.add(new Stack(selectBox, typing)).left().width(310);

    return table;
  }

  private void apply(int fps, SelectBox<Integer> selectBox) {
    if (fps != value()) {
      value(fps);
      Sounds.playUi(CoreSounds.SETTINGS_ENUM_VALUE_SELECTED, 1.0f);
    }
    showValue(selectBox);
  }

  // Lists the presets and, between them, a typed limit that is none of them.
  private void showValue(SelectBox<Integer> selectBox) {
    Array<Integer> limits = new Array<>();
    for (int preset : PRESETS) limits.add(preset);
    if (!limits.contains(value(), false)) limits.add(value());
    limits.sort();
    selectBox.setItems(limits);
    selectBox.setSelected(value());
  }
}
