package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.function.BiFunction;

/**
 * Provides ease of use factory methods to create a {@link
 * com.badlogic.gdx.scenes.scene2d.ui.Dialog}-based dialog for questions and a simple text.
 */
public final class DialogFactory {

  private static final int DEFAULT_WIDTH = 800;
  private static final int DEFAULT_HEIGHT = 600;

  /**
   * A simple Text Dialog that shows only the provided string.
   *
   * @param skin The style in which the whole dialog should be shown.
   * @param outputMsg The text which should be shown in the middle of the dialog.
   * @param confirmButton Text that the button should have; also the ID for the result handler.
   * @param title Title for the dialog.
   * @param resultHandler A callback method that is called when the confirm button is pressed.
   * @param width Width of the dialog.
   * @param height Height of the dialog.
   * @param align {@link com.badlogic.gdx.utils.Align Alignment} of the text.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  public static Dialog createTextDialog(
      final Skin skin,
      final String outputMsg,
      final String confirmButton,
      final String title,
      final BiFunction<TextDialog, String, Boolean> resultHandler,
      int width,
      int height,
      int align) {
    Dialog textDialog = new TextDialog(title, skin, resultHandler);
    textDialog.setWidth(width);
    textDialog.setHeight(height);
    textDialog.getContentTable().setWidth(width);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, outputMsg, align))
        .width(width)
        .height(height - 80)
        .left()
        .grow();
    textDialog.button(confirmButton, confirmButton);
    return textDialog;
  }
}
