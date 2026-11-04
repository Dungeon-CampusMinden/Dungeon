package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import engine.input.GameInput;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;

/** A read-only setting that shows the current binding of an action. */
public class ActionBindingSetting extends SettingValue<ActionId> {

  /**
   * Creates a new setting.
   *
   * @param translationKey the translation key of the setting label
   * @param action the action to show
   */
  public ActionBindingSetting(String translationKey, ActionId action) {
    super(translationKey, action);
  }

  @Override
  public Actor toUIActor() {
    RichLabel label = new RichLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    label.setWrap(false);

    Table bindingDisplay = new Table(UIUtils.defaultSkin());
    bindingDisplay.setBackground("generic-area");
    RichLabel bindingLabel = new RichLabel(GameInput.label(value()), 24, Color.BLACK);
    bindingLabel.setAlignment(Align.center);
    bindingDisplay.add(bindingLabel);

    Table bindingEntry = new Table();
    bindingEntry.left();
    bindingEntry.add(bindingDisplay).growY().minWidth(64).left();

    Table table = new Table();
    table.add(label).right().growX().padRight(10);
    table.add(bindingEntry).width(310);
    return table;
  }
}
