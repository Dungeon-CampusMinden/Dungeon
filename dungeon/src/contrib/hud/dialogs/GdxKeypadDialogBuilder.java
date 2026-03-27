package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.keypad.KeypadUI;
import core.Entity;
import core.Game;

/**
 * Builds the libGDX-backed keypad dialog.
 */
public final class GdxKeypadDialogBuilder {

  private GdxKeypadDialogBuilder() {}

  /**
   * Builds a Scene2D keypad dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the keypad dialog
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Keypad", null);
    }

    Entity keypad = ctx.requireEntity(DialogContextKeys.ENTITY);
    return new KeypadUI(keypad);
  }
}
