package engine.utils.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.input.GameInput;
import engine.language.Translation;
import engine.utils.Scene2dElementFactory;

/**
 * The head of the {@link ActionBindingSetting} rows: it shows the devices above their slots and
 * resets all bindings to their defaults. The reset asks for a second click, so that a stray click
 * does not throw the player's bindings away.
 */
final class BindingHeaderSetting extends SettingValue<Boolean> {
  private static final Translation TRANSLATION = new Translation("settings");

  BindingHeaderSetting() {
    super("settings.controls_devices", false);
  }

  @Override
  public Actor toUIActor() {
    TextButton reset =
        Scene2dElementFactory.createButton(
            TRANSLATION.text("controls_reset_all"), "blue-outline", 16);
    // Only clicks report a change, so withdrawing the first click below resets nothing.
    reset.setProgrammaticChangeEvents(false);
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
            reset.setChecked(false);
            reset.setText(TRANSLATION.text("controls_reset_all"));
          }
        });

    Table table = new Table();
    table.right();
    // Both texts of the button fit, so it keeps its place when it asks.
    table.add(reset).size(170, 36).right().expandX().padRight(10);
    table
        .add(ActionBindingSetting.deviceIcons(false, 20))
        .width(ActionBindingSetting.KEYS_WIDTH)
        .padRight(ActionBindingSetting.DEVICE_GAP)
        .bottom();
    table
        .add(ActionBindingSetting.deviceIcons(true, 20))
        .width(ActionBindingSetting.PAD_SLOT_WIDTH)
        .bottom();
    return table;
  }
}
