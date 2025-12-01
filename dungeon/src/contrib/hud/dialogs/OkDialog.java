package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;

/**
 * Package-private builder for OK dialogs.
 *
 * <p>Creates simple confirmation dialogs with a single OK button. Use {@link
 * DialogFactory#showOkDialog} instead of accessing this class directly.
 */
final class OkDialog {
  static final String DEFAULT_OK_BUTTON = "Ok";

  private OkDialog() {}

  /**
   * Builds an OK dialog from the given context.
   *
   * @param context The dialog context containing the message, title, and confirmation callback
   * @return A fully configured OK dialog
   */
  static Dialog build(DialogContext context) {
    String text = context.require(DialogContextKeys.MESSAGE, String.class);
    String title = context.title().orElse("OK");
    IVoidFunction onOk = context.require(DialogContextKeys.ON_CONFIRM, IVoidFunction.class);
    Entity entity = context.requireEntity();
    Dialog dialog = create(context.skin(), title, text, entity, onOk);
    return dialog;
  }

  private static Dialog create(
      Skin skin, String title, String text, Entity entity, IVoidFunction onOk) {
    Dialog textDialog =
        new TextDialog(
            title, skin, "Letter", (d, id) -> handleResult(entity, onOk, id, DEFAULT_OK_BUTTON));
    textDialog.getContentTable().add(DialogDesign.createTextDialog(skin, text)).center().grow();
    textDialog.button(DEFAULT_OK_BUTTON, DEFAULT_OK_BUTTON);
    textDialog.pack();
    return textDialog;
  }

  private static boolean handleResult(
      Entity entity, IVoidFunction onOk, String id, String expectedButton) {
    if (Objects.equals(id, expectedButton)) {
      onOk.execute();
      Game.remove(entity);
      return true;
    }
    return false;
  }
}
