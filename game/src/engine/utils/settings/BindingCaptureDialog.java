package engine.utils.settings;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
 * <p>The dialog covers the stage and runs a {@link BindingCapture} while it is open. Clicks on its
 * own buttons stay clicks, with the mouse and with the pad's cursor, so that removing a binding and
 * restoring the default work on both devices. A binding that would collide with another action is
 * named and not applied; the dialog then waits for the next input.
 *
 * <p>It explains itself without sentences: the icons of the device, a pulsing field that shows the
 * current binding and the modifiers the player holds, and the key that cancels below the cancel
 * button.
 */
final class BindingCaptureDialog extends Table {
  private static final Translation TRANSLATION = new Translation("settings.binding");
  private static final Color CONFLICT_COLOR = new Color(0xb3261eff);
  private static final Color LISTENING_COLOR = new Color(0x8fd0ffff);
  private static final Color REJECTED_COLOR = new Color(0xff9a8fff);
  private static final float BUTTON_WIDTH = 150;

  private final Actor origin;
  private final ActionId action;
  private final boolean pad;
  private final int slot;
  private final Table buttons = new Table();
  private final Table field = new Table(UIUtils.defaultSkin());
  private final RichLabel message = new RichLabel("", 20, CONFLICT_COLOR);
  // The modifiers shown in the field; null before the first frame.
  private List<Integer> shownModifiers;

  private BindingCaptureDialog(Actor origin, ActionId action, boolean pad, int slot) {
    this.origin = origin;
    this.action = action;
    this.pad = pad;
    this.slot = slot;
    setFillParent(true);
    setTouchable(Touchable.enabled);
    setBackground(UIUtils.tinted(new Color(0f, 0f, 0f, 0.6f)));

    TextButton remove = button("remove", "red-outline", () -> apply(null));
    remove.setDisabled(slot >= ActionBindingSetting.bindings(action, pad).size());
    TextButton restore = button("default", "blue-outline", this::restoreDefault);
    restore.setDisabled(GameInput.bindings(action).equals(action.defaults()));
    buttons.add(remove).width(BUTTON_WIDTH).padRight(10);
    buttons.add(restore).width(BUTTON_WIDTH).padRight(10);
    buttons.add(button("cancel", "blue-outline", this::remove)).width(BUTTON_WIDTH).row();
    Binding cancel = pad ? Binding.pad(Pad.START) : Binding.key(Input.Keys.ESCAPE);
    RichLabel cancelKey =
        new RichLabel("[binding id=" + cancel.format() + "]", 20, Color.DARK_GRAY);
    cancelKey.setWrap(false);
    buttons.add(cancelKey).colspan(3).right().padTop(4).padRight((BUTTON_WIDTH - 30) / 2);

    field.setBackground("generic-area");
    listen();
    message.setAlignment(Align.center);
    message.setWrap(false);
    if (pad && GameInput.padName().isEmpty()) {
      message.setText("[color=#404040]" + TRANSLATION.text("no_pad"));
    }

    Table panel = new Table(UIUtils.defaultSkin());
    panel.setBackground("window_background_big");
    panel.pad(25, 30, 20, 30);
    panel
        .add(
            Scene2dElementFactory.createLabel(
                KeyNames.label(action), FontSpec.of("fonts/Roboto-Bold.ttf", 36, Color.BLACK)))
        .row();
    panel.add(ActionBindingSetting.deviceIcons(pad, 24)).row();
    panel.add(field).size(220, 64).padTop(12).row();
    panel.add(message).minHeight(40).padTop(6).row();
    panel.add(buttons).padTop(6);
    add(panel);
  }

  // The field pulses while the dialog waits for input.
  private void listen() {
    field.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.color(LISTENING_COLOR, 0.6f), Actions.color(Color.WHITE, 0.6f))));
  }

  // Shows the binding of the slot, or the held modifiers as the start of a combo.
  private void showField(List<Integer> modifiers) {
    field.clearChildren();
    if (modifiers.isEmpty()) {
      List<Binding> current = ActionBindingSetting.bindings(action, pad);
      field.add(ActionBindingSetting.prompt(slot < current.size() ? current.get(slot) : null));
      return;
    }
    StringBuilder markup = new StringBuilder();
    modifiers.forEach(
        modifier ->
            markup.append("[binding id=").append(Binding.key(modifier).format()).append("]"));
    RichLabel combo = new RichLabel(markup + "+", 24, Color.BLACK);
    combo.setWrap(false);
    field.add(combo);
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
    field.clearActions();
    field.setColor(REJECTED_COLOR);
    field.addAction(Actions.sequence(Actions.color(Color.WHITE, 0.5f), Actions.run(this::listen)));
    ActionId other =
        conflict.get().first().equals(action) ? conflict.get().second() : conflict.get().first();
    message.setText(
        "[binding id=" + conflict.get().binding().format() + "] = " + KeyNames.label(other));
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    // The capture ends with its slot and as soon as another dialog comes to lie above it.
    boolean covered = getStage().getRoot().getChildren().peek() != this;
    if (covered || origin.getStage() == null || !origin.ancestorsVisible()) {
      remove();
      return;
    }
    List<Integer> modifiers =
        pad
            ? List.of()
            : Binding.MODIFIERS.stream().filter(GameInput.resolver()::modifierHeld).toList();
    if (!modifiers.equals(shownModifiers)) {
      shownModifiers = modifiers;
      showField(modifiers);
    }
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) GameInput.resolver().capture(null);
  }
}
