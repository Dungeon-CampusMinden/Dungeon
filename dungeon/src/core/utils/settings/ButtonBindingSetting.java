package core.utils.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;

/** A SettingValue that allows the user to bind a keyboard key to an action. */
public class ButtonBindingSetting extends SettingValue<Integer> {

  private boolean editable = true;
  private boolean isEditing = false;
  private TextField dummy;

  /**
   * Create a new ButtonBindingSetting with the given name and default key code.
   *
   * @param name the display name of the setting
   * @param defaultValue the default key code to bind, using libGDX's Input.Keys constants
   */
  public ButtonBindingSetting(String name, int defaultValue) {
    super(name, defaultValue);
  }

  /**
   * Create a new ButtonBindingSetting with the given name, default key code, and editability.
   *
   * @param name the display name of the setting
   * @param defaultValue the default key code to bind, using libGDX's Input.Keys constants
   * @param editable whether the setting can be edited by the user (if false, the button will be
   *     displayed but not interactive)
   */
  public ButtonBindingSetting(String name, int defaultValue, boolean editable) {
    super(name, defaultValue);
    this.editable = editable;
  }

  @Override
  public Actor toUIActor() {
    dummy = new TextField("", UIUtils.defaultSkin());

    Label label = Scene2dElementFactory.createLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);

    Table buttonEntry = new Table();
    buttonEntry.left();

    Table buttonDisplay = new Table(UIUtils.defaultSkin());
    buttonDisplay.setBackground(editable ? "blue_square_depth_border" : "generic-area");
    Label buttonLabel =
        Scene2dElementFactory.createLabel(Input.Keys.toString(value()), 24, Color.BLACK);
    buttonLabel.setAlignment(Align.center);
    buttonDisplay.setTouchable(Touchable.enabled);
    buttonDisplay.addListener(
        new ClickListener() {
          @Override
          public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (!editable) {
              return;
            }
            isEditing = true;
            buttonLabel.setText("<>");
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
            buttonDisplay
                .getStage()
                .setKeyboardFocus(
                    dummy); // Prevent the stage from processing key input while waiting for key
            // press
            buttonDisplay.addAction(
                new Action() {
                  @Override
                  public boolean act(float delta) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                      isEditing = false;
                      buttonLabel.setText(Input.Keys.toString(value()));
                      buttonDisplay.getStage().setKeyboardFocus(null);
                      return true;
                    }
                    for (int key = 0; key < Input.Keys.MAX_KEYCODE; key++) {
                      if (Gdx.input.isKeyJustPressed(key)) {
                        value(key);
                        isEditing = false;
                        buttonLabel.setText(Input.Keys.toString(value()));
                        buttonDisplay.getStage().setKeyboardFocus(null);
                        return true;
                      }
                    }
                    return false;
                  }
                });
            super.touchUp(event, x, y, pointer, button);
          }
        });

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(label).right().growX().padRight(10);

    buttonDisplay.add(buttonLabel);
    buttonEntry.add(buttonDisplay).growY().width(64).left();
    table.add(buttonEntry).width(310);

    return table;
  }
}
