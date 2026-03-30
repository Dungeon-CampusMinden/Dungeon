package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;

/**
 * Package-private Scene2D OK dialog.
 *
 * <p>Creates simple confirmation dialogs with a single OK button.
 */
final class OkDialog {
  static final String DEFAULT_OK_BUTTON = "Ok";

  private OkDialog() {}

  static Dialog create(Skin skin, String title, String text, String dialogId) {
    Dialog textDialog =
      new TextDialog(
        title,
        skin,
        "Letter",
        (d, id) -> {
          if (id.equals(DEFAULT_OK_BUTTON)) {
            DialogCallbackResolver.createButtonCallback(
                dialogId, DialogContextKeys.ON_CONFIRM)
              .accept(null);
          }
          return true;
        });

    textDialog
      .getContentTable()
      .add(DialogDesign.createTextDialog(skin, text))
      .center()
      .grow();

    textDialog.button(DEFAULT_OK_BUTTON, DEFAULT_OK_BUTTON);
    textDialog.pack();
    return textDialog;
  }
}
