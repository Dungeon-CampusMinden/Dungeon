package engine.input;

import static engine.input.Binding.key;
import static engine.input.Binding.pad;

import com.badlogic.gdx.Input.Keys;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;

/** Actions and contexts of dialogs and other modal UIs. */
public final class UiActions {
  /** Closes the UI. */
  public static final ActionId CLOSE = ui("close", key(Keys.ESCAPE), pad(Pad.B));

  /** Advances or confirms a dialog. */
  public static final ActionId CONFIRM = ui("confirm", key(Keys.SPACE), pad(Pad.A));

  /** Moves the selection up; repeats while held. */
  public static final ActionId NAV_UP =
      ui("nav_up", key(Keys.W), key(Keys.UP), pad(Pad.DPAD_UP), pad(PadAxisId.LSTICK_UP));

  /** Moves the selection down; repeats while held. */
  public static final ActionId NAV_DOWN =
      ui("nav_down", key(Keys.S), key(Keys.DOWN), pad(Pad.DPAD_DOWN), pad(PadAxisId.LSTICK_DOWN));

  /** Selects the previous entry of a horizontal list. */
  public static final ActionId NAV_LEFT =
      ui("nav_left", key(Keys.A), key(Keys.LEFT), pad(Pad.DPAD_LEFT), pad(PadAxisId.LSTICK_LEFT));

  /** Selects the next entry of a horizontal list. */
  public static final ActionId NAV_RIGHT =
      ui(
          "nav_right",
          key(Keys.D),
          key(Keys.RIGHT),
          pad(Pad.DPAD_RIGHT),
          pad(PadAxisId.LSTICK_RIGHT));

  /** Clicks with the pad cursor; the mouse talks to the stage directly. */
  public static final ActionId CLICK = ui("click", pad(Pad.A));

  /** Uses the own inventory item under pointer 0. */
  public static final ActionId USE_ITEM = ui("use_item", key(Keys.E), pad(Pad.X));

  /** Moves the item under pointer 0 to the other inventory. */
  public static final ActionId TRANSFER_ITEM = ui("transfer_item", pad(Pad.RB));

  /** Screens outside a running game. On the base layer, so that their windows keep their keys. */
  public static final ContextSpec MENU =
      ContextSpec.of("menu_ui", ContextLayer.BASE, CLICK, NAV_UP, NAV_DOWN, NAV_LEFT, NAV_RIGHT)
          .blocking();

  /** A modal dialog without a confirm action; the pad works through its cursor. */
  public static final ContextSpec DIALOG =
      ContextSpec.of("dialog", ContextLayer.UI, NAV_UP, NAV_DOWN, CLICK)
          .closedBy(CLOSE)
          .padOnly(CoreActions.PAUSE_MENU)
          .blocking();

  /**
   * A modal dialog with a confirm action. It does not take {@link #CLICK}: both lie on the same pad
   * button, and selection and confirmation already cover the pad.
   */
  public static final ContextSpec CONFIRM_DIALOG =
      ContextSpec.of("confirm_dialog", ContextLayer.UI, CONFIRM, NAV_UP, NAV_DOWN)
          .closedBy(CLOSE)
          .padOnly(CoreActions.PAUSE_MENU)
          .blocking();

  /** The room's controls; their open action closes them as well. */
  public static final ContextSpec CONTROLS =
      ContextSpec.of("controls", ContextLayer.UI, CONFIRM, NAV_UP, NAV_DOWN)
          .closedBy(CLOSE, CoreActions.SHOW_CONTROLS)
          .padOnly(CoreActions.PAUSE_MENU)
          .blocking();

  /** The pause menu; its open action closes it as well. */
  public static final ContextSpec PAUSE =
      ContextSpec.of("pause", ContextLayer.UI, NAV_UP, NAV_DOWN, CLICK)
          .closedBy(CLOSE, CoreActions.PAUSE_MENU)
          .blocking();

  /** Inventory and chest; the inventory action closes them as well. */
  public static final ContextSpec INVENTORY =
      ContextSpec.of("inventory", ContextLayer.UI, USE_ITEM, TRANSFER_ITEM, CLICK)
          .closedBy(CLOSE, CoreActions.INVENTORY)
          .padOnly(CoreActions.PAUSE_MENU)
          .blocking();

  /** The quest log; its open action closes it as well. */
  public static final ContextSpec QUESTLOG =
      ContextSpec.of("questlog", ContextLayer.UI, NAV_UP, NAV_DOWN, CLICK)
          .closedBy(CLOSE, CoreActions.QUESTLOG)
          .padOnly(CoreActions.PAUSE_MENU)
          .blocking();

  private UiActions() {}

  private static ActionId ui(String name, Binding... defaults) {
    return new ActionId("ui", name, defaults);
  }
}
