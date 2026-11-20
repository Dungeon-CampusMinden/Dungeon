package engine.utils.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.language.Translation;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;

/**
 * The head of the {@link ActionBindingSetting} rows: it shows the device in use above the slots and
 * resets all bindings to their defaults. The reset asks for a second click and turns red while it
 * asks, so that a stray click does not throw the player's bindings away.
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
    // While it asks, the button turns red, so that the question shows in every language.
    TextButton.TextButtonStyle asking =
        UIUtils.defaultSkin().get("red-outline", TextButton.TextButtonStyle.class);
    reset.getStyle().checked = asking.up;
    reset.getStyle().checkedOver = asking.over;
    reset.getStyle().checkedDown = asking.down;
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
            // Leaving the button withdraws the first click; moving between it and its label is no
            // leaving.
            boolean inside = toActor != null && toActor.isDescendantOf(reset);
            if (pointer != -1 || inside || !reset.isChecked()) return;
            reset.setChecked(false);
            reset.setText(TRANSLATION.text("controls_reset_all"));
          }
        });

    // The icons follow the device in use, like the slots below them.
    Table device =
        new Table() {
          private String shown;

          @Override
          public void act(float delta) {
            super.act(delta);
            String current = GameInput.device() + GameInput.padName();
            if (current.equals(shown)) return;
            shown = current;
            clearChildren();
            add(ActionBindingSetting.deviceIcons(GameInput.device() == InputDevice.PAD, 20));
          }
        };

    Table table = new Table();
    table.right();
    // Both texts of the button fit, so it keeps its place when it asks.
    table.add(reset).size(170, 36).right().expandX().padRight(10);
    table.add(device).width(ActionBindingSetting.CONTROL_WIDTH).bottom();
    return table;
  }
}
