package contrib.modules.keypad.ui;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating keypad dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a keypad dialog overlay
 * for an entity within a dialog context.
 */
public final class KeypadDialogBuilder {

  private KeypadDialogBuilder() {}

  /**
   * Builds a UI node handle for a keypad dialog overlay.
   *
   * @param ctx the dialog context containing the entity and configuration
   * @return a UI node handle wrapping the created keypad dialog overlay
   * @throws IllegalArgumentException if the entity is not present in the dialog context
   */
  public static UiHandle build(DialogContext ctx) {
    Entity keypad = ctx.requireEntity(DialogContextKeys.ENTITY);
    return new OverlayHandle(new KeypadDialogOverlay(keypad));
  }
}
