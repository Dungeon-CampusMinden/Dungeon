package feature.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Event;
import engine.Game;
import engine.input.ActionId;
import engine.input.ContextSpec;
import engine.input.CoreActions;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.input.InputDevice;
import engine.input.KeyboardFocus;
import engine.input.UiActions;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.interaction.phone.Dialer;
import feature.inventory.ui.InventoryGUI;
import java.util.HashMap;
import java.util.Map;

/** Builds the input context of a dialog. */
public final class DialogInput {
  private static final Map<String, ContextSpec> SPECS = new HashMap<>();

  static {
    register(DialogType.DefaultTypes.PAUSE_MENU, UiActions.PAUSE);
    register(DialogType.DefaultTypes.INVENTORY, UiActions.INVENTORY);
    register(DialogType.DefaultTypes.DUAL_INVENTORY, UiActions.INVENTORY);
    register(DialogType.DefaultTypes.OK, UiActions.CONFIRM_DIALOG);
    register(DialogType.DefaultTypes.TEXT, UiActions.CONFIRM_DIALOG);
    register(DialogType.DefaultTypes.DIALOG_DIALOG, UiActions.CONFIRM_DIALOG);
    register(DialogType.DefaultTypes.MULTIPLE_CHOICE, UiActions.CONFIRM_DIALOG);
    register(DialogType.DefaultTypes.CONTROLS, UiActions.CONTROLS);
    register(DialogType.DefaultTypes.IMAGE, UiActions.IMAGE);
    register(DialogType.DefaultTypes.PHONE, Dialer.INPUT);
  }

  private DialogInput() {}

  /**
   * Sets the context declaration of a dialog type. Types without one use {@link UiActions#DIALOG}.
   *
   * @param type the dialog type
   * @param spec the context declaration
   */
  public static void register(DialogType type, ContextSpec spec) {
    SPECS.put(type.type(), spec);
  }

  /**
   * Action sent to the outermost actor of a dialog that handles it itself.
   *
   * @see UIUtils#onUiAction
   */
  public static final class UiActionEvent extends Event {
    private final ActionId action;

    /**
     * Creates an event.
     *
     * @param action the action
     */
    public UiActionEvent(ActionId action) {
      this.action = action;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public ActionId action() {
      return action;
    }
  }

  /**
   * Creates the input context of a dialog.
   *
   * @param ui the dialog
   * @return a new context that closes and drives this dialog
   */
  public static InputContext context(UIComponent ui) {
    ContextSpec spec = SPECS.getOrDefault(ui.dialogContext().dialogType().type(), UiActions.DIALOG);
    InputContext context = new InputContext(spec);
    KeyboardFocus focus = KeyboardFocus.of(ui.dialog());
    // Every action of the spec reaches the dialog, see UIUtils#onUiAction; the handlers below
    // replace this for the shared ones.
    spec.actions().forEach(action -> context.onPress(action, () -> fire(ui, action)));
    context
        .onPress(
            CoreActions.PAUSE_MENU, () -> Game.player().ifPresent(PauseDialog::showPauseDialog))
        .onPress(UiActions.CONFIRM, () -> confirm(ui))
        .onPress(UiActions.FOCUS_NEXT, () -> focus.move(1))
        .onPress(UiActions.FOCUS_PREVIOUS, () -> focus.move(-1))
        .onPress(UiActions.PRESS, focus::press)
        .onPress(UiActions.NAV_UP, () -> navigate(ui, spec, UiActions.NAV_UP, 0, 1))
        .onPress(UiActions.NAV_DOWN, () -> navigate(ui, spec, UiActions.NAV_DOWN, 0, -1))
        .onPress(UiActions.NAV_LEFT, () -> navigate(ui, spec, UiActions.NAV_LEFT, -1, 0))
        .onPress(UiActions.NAV_RIGHT, () -> navigate(ui, spec, UiActions.NAV_RIGHT, 1, 0))
        .onPress(UiActions.USE_ITEM, () -> inventories(ui).forEach(InventoryGUI::useItemAtPointer))
        .onPress(
            UiActions.TRANSFER_ITEM,
            () -> inventories(ui).forEach(InventoryGUI::transferItemAtPointer));
    spec.closing().forEach(action -> context.onPress(action, () -> close(ui)));
    return context;
  }

  private static Iterable<InventoryGUI> inventories(UIComponent ui) {
    return UIUtils.findAllTypesInGroup(ui.dialog(), InventoryGUI.class).toList();
  }

  // A dialog that cannot be closed leaves Escape to the pause menu; the pad has START for that.
  private static void close(UIComponent ui) {
    if (ui.canBeClosed() || UIUtils.hasCloseRequestHandler(ui.dialog())) {
      UIUtils.requestCloseDialog(ui);
    } else if (GameInput.device() != InputDevice.PAD) {
      Game.player().ifPresent(PauseDialog::showPauseDialog);
    }
  }

  // A button with the keyboard focus takes the confirmation; dialogs that do not handle confirm
  // themselves confirm through their callback.
  private static void confirm(UIComponent ui) {
    if (KeyboardFocus.of(ui.dialog()).pressFocused() || fire(ui, UiActions.CONFIRM)) return;
    DialogCallbackResolver.createButtonCallback(
            ui.dialogContext().dialogId(), DialogContextKeys.ON_CONFIRM)
        .accept(null);
  }

  private static boolean fire(UIComponent ui, ActionId action) {
    UiActionEvent event = new UiActionEvent(action);
    ui.dialog().fire(event);
    return event.isHandled();
  }

  // Dialogs without a selection of their own, which take everything a plain dialog takes, let the
  // D-pad move the pad cursor between their controls. Up and down scroll the text where that finds
  // none, which also serves the keyboard.
  private static void navigate(UIComponent ui, ContextSpec spec, ActionId action, int dx, int dy) {
    if (fire(ui, action)) return;
    boolean padCursor =
        spec.actions().containsAll(UiActions.DIALOG.actions())
            && GameInput.device() == InputDevice.PAD;
    if (padCursor && PadNavigation.move(ui.dialog(), dx, dy)) return;
    if (dy != 0) GameInput.scroll(-dy);
  }
}
