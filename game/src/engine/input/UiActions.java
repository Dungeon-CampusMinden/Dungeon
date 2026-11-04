package engine.input;

/** Actions and contexts of dialogs and other modal UIs. */
public final class UiActions {
  /** Closes the UI. */
  public static final ActionId CLOSE = new ActionId("ui", "close");

  /** Advances or confirms a dialog. */
  public static final ActionId CONFIRM = new ActionId("ui", "confirm");

  /** Moves the selection up; repeats while held. */
  public static final ActionId NAV_UP = new ActionId("ui", "nav_up");

  /** Moves the selection down; repeats while held. */
  public static final ActionId NAV_DOWN = new ActionId("ui", "nav_down");

  /** Clicks with the pad cursor; the mouse talks to the stage directly. */
  public static final ActionId CLICK = new ActionId("ui", "click");

  /** Uses the own inventory item under pointer 0. */
  public static final ActionId USE_ITEM = new ActionId("ui", "use_item");

  /** Moves the item under pointer 0 to the other inventory. */
  public static final ActionId TRANSFER_ITEM = new ActionId("ui", "transfer_item");

  /** A modal dialog without a confirm action; the pad works through its cursor. */
  public static final ContextSpec DIALOG =
      ContextSpec.of("dialog", ContextLayer.UI, true, CLOSE, NAV_UP, NAV_DOWN, CLICK);

  /**
   * A modal dialog with a confirm action. It does not take {@link #CLICK}: both lie on the same pad
   * button, and selection and confirmation already cover the pad.
   */
  public static final ContextSpec CONFIRM_DIALOG =
      ContextSpec.of("confirm_dialog", ContextLayer.UI, true, CLOSE, CONFIRM, NAV_UP, NAV_DOWN);

  /** The pause menu. */
  public static final ContextSpec PAUSE =
      ContextSpec.of("pause", ContextLayer.UI, true, CLOSE, NAV_UP, NAV_DOWN, CLICK);

  /**
   * Inventory and chest. The pad's inventory button transfers items here, so only the keyboard
   * closes through {@link CoreActions#INVENTORY}.
   */
  public static final ContextSpec INVENTORY =
      ContextSpec.of(
              "inventory",
              ContextLayer.UI,
              true,
              CoreActions.INVENTORY,
              CLOSE,
              USE_ITEM,
              TRANSFER_ITEM,
              CLICK)
          .keyboardOnly(CoreActions.INVENTORY);

  /** The quest log; its open action closes it as well. */
  public static final ContextSpec QUESTLOG =
      ContextSpec.of(
          "questlog", ContextLayer.UI, true, CoreActions.QUESTLOG, CLOSE, NAV_UP, NAV_DOWN, CLICK);

  private UiActions() {}

  static void define(BindingTable table) {
    table.define(CLOSE, "key:ESCAPE", "pad:B");
    table.define(CONFIRM, "key:SPACE", "pad:A");
    table.define(NAV_UP, "key:W", "key:UP", "pad:DPAD_UP", "pad:LSTICK_UP");
    table.define(NAV_DOWN, "key:S", "key:DOWN", "pad:DPAD_DOWN", "pad:LSTICK_DOWN");
    table.define(CLICK, "pad:A");
    table.define(USE_ITEM, "key:E", "pad:X");
    table.define(TRANSFER_ITEM, "pad:Y");
    table.context(DIALOG);
    table.context(CONFIRM_DIALOG);
    table.context(PAUSE);
    table.context(INVENTORY);
    table.context(QUESTLOG);
  }
}
