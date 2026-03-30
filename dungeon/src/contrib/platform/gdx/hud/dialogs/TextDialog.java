package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.dialogs.DialogFactory;

import java.util.function.BiFunction;

/**
 * A TextDialog that allows the result handler to be defined per functional interface.
 *
 * <p>Use {@link DialogFactory#showTextDialog} to create and show a TextDialog.
 */
public final class TextDialog extends Dialog {

  /** Handler for button presses. */
  private final BiFunction<Dialog, String, Boolean> resultHandler;

  /**
   * Creates a TextDialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param title Title of the dialog
   * @param resultHandler controls the button presses
   */
  public TextDialog(
    final String title,
    final Skin skin,
    final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin);
    this.resultHandler = resultHandler;
  }

  /**
   * Creates a TextDialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param title Title of the dialog
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param windowStyleName the name of the style which should be used
   * @param resultHandler controls the button presses
   */
  public TextDialog(
    final String title,
    final Skin skin,
    final String windowStyleName,
    final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * Creates a simple text dialog with a configurable primary button.
   *
   * @param skin the dialog skin
   * @param outputMsg the dialog message
   * @param confirmButton the primary button label
   * @param title the dialog title
   * @param resultHandler callback for button results
   * @return the configured dialog
   */
  static Dialog create(
    final Skin skin,
    final String outputMsg,
    final String confirmButton,
    final String title,
    final BiFunction<Dialog, String, Boolean> resultHandler) {
    Dialog textDialog = new TextDialog(title, skin, resultHandler);
    textDialog
      .getContentTable()
      .add(DialogDesign.createTextDialog(skin, outputMsg))
      .center()
      .grow();
    textDialog.button(confirmButton, confirmButton);
    return textDialog;
  }

  @Override
  protected void result(Object object) {
    if (resultHandler != null) {
      resultHandler.apply(this, String.valueOf(object));
    }
  }
}
