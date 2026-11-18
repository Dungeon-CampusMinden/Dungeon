package engine.utils.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.Binding.Pad;
import engine.input.BindingCapture;
import engine.input.BindingTable.Conflict;
import engine.input.GameInput;
import engine.input.KeyNames;
import engine.input.UiActions;
import engine.language.Translation;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Asks the player for the new binding of one slot of an action.
 *
 * <p>The dialog covers the stage and captures all input while it is open: the next key, mouse
 * button or pad button becomes the binding, Escape and the pad's START cancel. Clicks on its own
 * buttons stay clicks, with the mouse and with the pad's cursor, so that removing a binding and
 * restoring the default work on both devices. A binding that would collide with another action is
 * named and not applied; the dialog then waits for the next input.
 */
final class BindingCaptureDialog extends Table {
  private static final Translation TRANSLATION = new Translation("settings.binding");
  private static final Color CONFLICT_COLOR = new Color(0xb3261eff);
  private static final float TEXT_WIDTH = 460;

  private final Actor origin;
  private final ActionId action;
  private final boolean pad;
  private final int slot;
  private final Table buttons = new Table();
  private final RichLabel message = new RichLabel("", 20, CONFLICT_COLOR);

  private BindingCaptureDialog(Actor origin, ActionId action, boolean pad, int slot) {
    this.origin = origin;
    this.action = action;
    this.pad = pad;
    this.slot = slot;
    setFillParent(true);
    setTouchable(Touchable.enabled);
    setBackground(UIUtils.defaultSkin().newDrawable("white", new Color(0f, 0f, 0f, 0.6f)));

    List<Binding> current = ActionBindingSetting.bindings(action, pad);
    TextButton remove = button("remove", "red-outline", () -> apply(null));
    remove.setDisabled(slot >= current.size());
    TextButton restore = button("default", "blue-outline", this::restoreDefault);
    restore.setDisabled(GameInput.bindings(action).equals(action.defaults()));
    buttons.add(remove).width(150).padRight(10);
    buttons.add(restore).width(150).padRight(10);
    buttons.add(button("cancel", "blue-outline", this::remove)).width(150);

    Table panel = new Table(UIUtils.defaultSkin());
    panel.setBackground("window_background_big");
    panel.pad(25, 30, 25, 30);
    panel
        .add(
            Scene2dElementFactory.createLabel(
                KeyNames.label(action), FontSpec.of("fonts/Roboto-Bold.ttf", 36, Color.BLACK)))
        .padBottom(4)
        .row();
    panel.add(text(new Translation("settings").text(pad ? "controls_pad" : "controls_keyboard")));
    panel.row();
    Table currentPrompt = new Table(UIUtils.defaultSkin());
    currentPrompt.setBackground("generic-area");
    currentPrompt.add(
        ActionBindingSetting.prompt(slot < current.size() ? current.get(slot) : null));
    panel.add(currentPrompt).size(140, 56).pad(15, 0, 15, 0).row();
    panel.add(text(prompt())).width(TEXT_WIDTH).row();
    message.setAlignment(Align.center);
    panel.add(message).width(TEXT_WIDTH).minHeight(56).padTop(8).row();
    panel.add(buttons).padTop(8);
    add(panel);
  }

  /**
   * Opens the dialog on the stage of a slot.
   *
   * @param origin the actor that asks; the dialog closes with it
   * @param action the action to rebind
   * @param pad true for a pad slot, false for a slot of keyboard and mouse
   * @param slot index of the slot among the bindings of that device
   */
  static void show(Actor origin, ActionId action, boolean pad, int slot) {
    BindingCaptureDialog dialog = new BindingCaptureDialog(origin, action, pad, slot);
    origin.getStage().addActor(dialog);
    GameInput.resolver()
        .capture(new BindingCapture(GameInput.resolver(), pad, dialog::passes, dialog::captured));
  }

  private String prompt() {
    if (!pad) return TRANSLATION.text("prompt_keyboard", "[binding id=key:ESCAPE]");
    if (GameInput.padName().isEmpty()) return TRANSLATION.text("no_pad");
    return TRANSLATION.text("prompt_pad", "[binding id=" + Binding.pad(Pad.START).format() + "]");
  }

  private static RichLabel text(String markup) {
    RichLabel label = new RichLabel(markup, 20, Color.DARK_GRAY);
    label.setAlignment(Align.center);
    return label;
  }

  private TextButton button(String key, String style, Runnable onClick) {
    TextButton button = Scene2dElementFactory.createButton(TRANSLATION.text(key), style, 20);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            onClick.run();
          }
        });
    return button;
  }

  // A click on one of the dialog's buttons presses it; every other press is captured.
  private boolean passes(Binding input) {
    boolean click =
        input instanceof Binding.MouseButton || GameInput.bindings(UiActions.CLICK).contains(input);
    if (!click) return false;
    engine.utils.Vector2 pointer = GameInput.pointer();
    Vector2 position = getStage().screenToStageCoordinates(new Vector2(pointer.x(), pointer.y()));
    Actor hit = getStage().hit(position.x, position.y, true);
    return hit != null && hit.isDescendantOf(buttons);
  }

  private void captured(Optional<Binding> binding) {
    if (binding.isPresent()) apply(binding.get());
    else remove();
  }

  // Puts a binding into the slot, or empties the slot for null.
  private void apply(Binding binding) {
    List<Binding> keys = new ArrayList<>(ActionBindingSetting.bindings(action, false));
    List<Binding> pads = new ArrayList<>(ActionBindingSetting.bindings(action, true));
    List<Binding> device = pad ? pads : keys;
    if (binding == null) {
      device.remove(slot);
    } else if (!device.contains(binding)) {
      if (slot < device.size()) device.set(slot, binding);
      else device.add(binding);
    }
    keys.addAll(pads);
    // Bindings equal to the defaults need no override.
    finish(
        keys.equals(action.defaults())
            ? GameInput.resolver().resetBinding(action)
            : GameInput.resolver().rebind(action, keys));
  }

  private void restoreDefault() {
    finish(GameInput.resolver().resetBinding(action));
  }

  private void finish(Optional<Conflict> conflict) {
    if (conflict.isEmpty()) {
      remove();
      return;
    }
    ActionId other =
        conflict.get().first().equals(action) ? conflict.get().second() : conflict.get().first();
    message.setText(
        TRANSLATION.text(
            "conflict",
            "[binding id=" + conflict.get().binding().format() + "]",
            KeyNames.label(other)));
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (origin.getStage() == null || !origin.ancestorsVisible()) remove();
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) GameInput.resolver().capture(null);
  }
}
