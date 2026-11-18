package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.input.GameInput;
import engine.language.Translation;
import engine.utils.Scene2dElementFactory;
import feature.hud.elements.RichLabel;

/**
 * The head of the {@link ActionBindingSetting} rows: it names the devices above their slots and
 * resets all bindings to their defaults. The reset asks for a second click, so that a stray click
 * does not throw the player's bindings away.
 */
public class BindingHeaderSetting extends SettingValue<Boolean> {
  private static final Translation TRANSLATION = new Translation("settings");

  /**
   * Creates the head of the binding rows.
   *
   * @param translationKey unique key of this setting
   */
  public BindingHeaderSetting(String translationKey) {
    super(translationKey, false);
  }

  @Override
  public Actor toUIActor() {
    TextButton reset =
        Scene2dElementFactory.createButton(
            TRANSLATION.text("controls_reset_all"), "blue-outline", 16);
    reset.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (reset.isChecked()) {
              reset.setText(TRANSLATION.text("controls_reset_all_confirm"));
            } else {
              reset.setText(TRANSLATION.text("controls_reset_all"));
              GameInput.resolver().resetBindings();
            }
          }
        });
    reset.addListener(
        new InputListener() {
          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            // Leaving the button withdraws the first click.
            if (pointer != -1 || !reset.isChecked()) return;
            reset.setProgrammaticChangeEvents(false);
            reset.setChecked(false);
            reset.setProgrammaticChangeEvents(true);
            reset.setText(TRANSLATION.text("controls_reset_all"));
          }
        });

    float keysWidth = 2 * ActionBindingSetting.KEY_SLOT_WIDTH + ActionBindingSetting.SLOT_GAP;
    Table devices = new Table();
    devices.left();
    devices
        .add(caption("controls_keyboard"))
        .width(keysWidth)
        .padRight(ActionBindingSetting.DEVICE_GAP);
    devices.add(caption("controls_pad")).width(ActionBindingSetting.PAD_SLOT_WIDTH);

    Table table = new Table();
    table.right();
    // Both texts of the button fit, so it keeps its place when it asks.
    table.add(reset).size(170, 36).right().expandX().padRight(10);
    table.add(devices).width(ActionBindingSetting.CONTROL_WIDTH).bottom();
    return table;
  }

  private static RichLabel caption(String key) {
    RichLabel caption = new RichLabel(TRANSLATION.text(key), 16, Color.DARK_GRAY);
    caption.setAlignment(Align.center);
    caption.setWrap(false);
    return caption;
  }
}
