package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import java.util.function.BiFunction;

/**
 * Package-private Scene2D yes/no dialog.
 *
 * <p>Creates confirmation dialogs with Yes and No buttons.
 */
final class YesNoDialog {
  static final String DEFAULT_DIALOG_YES = "Ja";
  static final String DEFAULT_DIALOG_NO = "Nein";

  private YesNoDialog() {}

  static Dialog create(final Skin skin, final String text, final String title, String dialogId) {
    BiFunction<Dialog, String, Boolean> resultHandler =
      (d, id) -> {
        if (id.equals(DEFAULT_DIALOG_YES)) {
          DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
            .accept(null);
        } else if (id.equals(DEFAULT_DIALOG_NO)) {
          DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
            .accept(null);
        }
        return true;
      };

    Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
    textDialog
      .getContentTable()
      .add(DialogDesign.createTextDialog(skin, UIUtils.formatString(text)))
      .center()
      .grow();
    textDialog.button(DEFAULT_DIALOG_NO, DEFAULT_DIALOG_NO);
    textDialog.button(DEFAULT_DIALOG_YES, DEFAULT_DIALOG_YES);
    textDialog.pack();
    return textDialog;
  }
}
