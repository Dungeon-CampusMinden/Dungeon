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

  /** Clicks with the pad cursor; the mouse talks to the stage directly. */
  public static final ActionId CLICK = ui("click", pad(Pad.A));

  /** Uses the own inventory item under pointer 0. */
  public static final ActionId USE_ITEM = ui("use_item", key(Keys.E), pad(Pad.X));

  /** Moves the item under pointer 0 to the other inventory. */
  public static final ActionId TRANSFER_ITEM = ui("transfer_item", pad(Pad.Y));

  /** A modal dialog without a confirm action; the pad works through its cursor. */
  public static final ContextSpec DIALOG =
      ContextSpec.of("dialog", ContextLayer.UI, CLOSE, NAV_UP, NAV_DOWN, CLICK).blocking();

  /**
   * A modal dialog with a confirm action. It does not take {@link #CLICK}: both lie on the same pad
   * button, and selection and confirmation already cover the pad.
   */
  public static final ContextSpec CONFIRM_DIALOG =
      ContextSpec.of("confirm_dialog", ContextLayer.UI, CLOSE, CONFIRM, NAV_UP, NAV_DOWN)
          .blocking();

  /** The pause menu. */
  public static final ContextSpec PAUSE =
      ContextSpec.of("pause", ContextLayer.UI, CLOSE, NAV_UP, NAV_DOWN, CLICK).blocking();

  /**
   * Inventory and chest. The pad's inventory button transfers items here, so only the keyboard
   * closes through {@link CoreActions#INVENTORY}.
   */
  public static final ContextSpec INVENTORY =
      ContextSpec.of(
              "inventory",
              ContextLayer.UI,
              CoreActions.INVENTORY,
              CLOSE,
              USE_ITEM,
              TRANSFER_ITEM,
              CLICK)
          .blocking()
          .keyboardOnly(CoreActions.INVENTORY);

  /** The quest log; its open action closes it as well. */
  public static final ContextSpec QUESTLOG =
      ContextSpec.of(
              "questlog", ContextLayer.UI, CoreActions.QUESTLOG, CLOSE, NAV_UP, NAV_DOWN, CLICK)
          .blocking();

  private UiActions() {}

  private static ActionId ui(String name, Binding... defaults) {
    return new ActionId("ui", name, defaults);
  }
}
