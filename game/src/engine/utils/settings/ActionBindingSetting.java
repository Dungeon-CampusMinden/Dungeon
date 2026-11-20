package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.Binding.PadAxisId;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.input.KeyNames;
import engine.utils.Cursors;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import feature.input.ui.InputMethod;
import feature.input.ui.InputPromptHelper;
import feature.input.ui.InputPromptHelper.InputPromptRegion;
import java.util.List;

/**
 * A setting that shows the bindings of an action and lets the player change them. It shows two
 * slots for the device the player used last, keyboard and mouse or the pad, like the prompts in the
 * game. A click on a slot opens the {@link BindingCaptureDialog} for it.
 *
 * <p>Movement on the stick shows its binding but cannot be changed.
 */
public class ActionBindingSetting extends SettingValue<ActionId> {
  /** Width of the column that holds the controls of all settings. */
  static final float CONTROL_WIDTH = 310;

  private static final int SLOTS = 2;
  private static final float SLOT_GAP = 6;
  private static final float SLOT_WIDTH = (CONTROL_WIDTH - (SLOTS - 1) * SLOT_GAP) / SLOTS;
  private static final float SLOT_HEIGHT = 48;
  private static final Color EMPTY_COLOR = new Color(0x00000066);

  /**
   * Creates a new setting. Its label is the name of the action, see {@link
   * KeyNames#label(ActionId)}.
   *
   * @param action the action to show and rebind
   */
  public ActionBindingSetting(ActionId action) {
    super(action.toString(), action);
  }

  @Override
  public String name() {
    return KeyNames.label(value());
  }

  @Override
  public Actor toUIActor() {
    RichLabel label = new RichLabel(name(), 24, Color.BLACK);
    label.setAlignment(Align.right);
    label.setWrap(false);

    Table table = new Table();
    // A label wider than its column grows to the left, so the controls of all rows line up.
    table.right();
    table.add(label).right().growX().padRight(10);
    table.add(new Slots(value())).width(CONTROL_WIDTH);
    return table;
  }

  /**
   * Splits the bindings of an action by device.
   *
   * @param action the action
   * @param pad true for the pad's bindings, false for those of keyboard and mouse
   * @return the bindings of that device, in their order
   */
  static List<Binding> bindings(ActionId action, boolean pad) {
    return GameInput.bindings(action).stream().filter(binding -> binding.isPad() == pad).toList();
  }

  /** The slots of one action; they follow the device in use and every change of its bindings. */
  private static final class Slots extends Table {
    private final ActionId action;
    private String shown;

    private Slots(ActionId action) {
      this.action = action;
      left();
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      // The pad's name decides between the prompt graphics of Xbox and PlayStation.
      String current = GameInput.device() + GameInput.padName() + GameInput.bindings(action);
      if (current.equals(shown)) return;
      shown = current;
      rebuild();
    }

    private void rebuild() {
      clearChildren();
      // The stick stays movement, see BindingCapture; its action only shows what moves the hero.
      if (action.defaults().contains(Binding.pad(PadAxisId.LSTICK))) {
        Table fixed = new Table(UIUtils.defaultSkin());
        fixed.setBackground("generic-area");
        fixed.add(new RichLabel("[action id=" + action + "]", 24, Color.BLACK));
        fixed.getColor().a = 0.6f;
        add(fixed).size(CONTROL_WIDTH, SLOT_HEIGHT);
        return;
      }
      boolean pad = GameInput.device() == InputDevice.PAD;
      List<Binding> bindings = bindings(action, pad);
      for (int slot = 0; slot < Math.max(SLOTS, bindings.size()); slot++) {
        add(slot(slot < bindings.size() ? bindings.get(slot) : null, pad, slot))
            .size(SLOT_WIDTH, SLOT_HEIGHT)
            .padLeft(slot == 0 ? 0 : SLOT_GAP);
      }
    }

    private Actor slot(Binding binding, boolean pad, int slot) {
      Skin skin = UIUtils.defaultSkin();
      Button button =
          new Button(
              new Button.ButtonStyle(
                  skin.getDrawable("generic-area"), skin.getDrawable("generic-area-depth"), null));
      button.getStyle().over = skin.getDrawable("generic-area-depth");
      button.setUserObject(Cursors.INTERACT);
      button.add(prompt(binding));
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              BindingCaptureDialog.show(Slots.this, action, pad, slot);
            }
          });
      return button;
    }
  }

  /**
   * Creates the icons of a device, which head the slots and name it in the capture dialog.
   *
   * @param pad true for the controller, false for keyboard and mouse
   * @param size font size the icons scale with
   * @return the icons
   */
  static RichLabel deviceIcons(boolean pad, int size) {
    InputMethod device = pad ? InputMethod.ofController(GameInput.padName()) : InputMethod.KEYBOARD;
    StringBuilder markup = new StringBuilder();
    for (InputPromptRegion icon : InputPromptHelper.lookupDevice(device)) {
      markup.append(
          String.format(
              "[img path=%s x=%d y=%d w=%d h=%d scale=1.5 color=#404040]",
              icon.texturePath(), icon.x(), icon.y(), icon.width(), icon.height()));
    }
    RichLabel icons = new RichLabel(markup.toString(), size, Color.DARK_GRAY);
    icons.setAlignment(Align.center);
    icons.setWrap(false);
    return icons;
  }

  /**
   * Creates the prompt of a binding as slots and the capture dialog show it. A combo with all three
   * modifiers shrinks, so that every binding fits a slot.
   *
   * @param binding the binding, or null for an empty slot
   * @return the prompt
   */
  static RichLabel prompt(Binding binding) {
    int keys = binding == null ? 1 : binding.prefixes().size() + 1;
    int size = keys > 3 ? 18 : 24;
    RichLabel prompt =
        binding == null
            ? new RichLabel("+", 24, EMPTY_COLOR)
            : new RichLabel("[binding id=" + binding.format() + "]", size, Color.BLACK);
    prompt.setWrap(false);
    prompt.setTouchable(Touchable.disabled);
    return prompt;
  }
}
