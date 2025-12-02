package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.Entity;
import core.Game;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A TextDialog that allows the result handler to be defined per functional interface.
 *
 * <p>Use {@link #textDialog(String, String, String)} to create and add a HUD-Entity to the game,
 * that will show the given text.
 */
final class TextDialog extends Dialog {

  /** Handler for Button presses. */
  private final BiFunction<Dialog, String, Boolean> resultHandler;

  /**
   * creates a Textdialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param title Title of the dialog
   * @param resultHandler controls the button presses
   */
  TextDialog(
      final String title,
      final Skin skin,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin);
    this.resultHandler = resultHandler;
  }

  /**
   * creates a Textdialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param title Title of the dialog
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param windowStyleName the name of the style which should be used
   * @param resultHandler controls the button presses
   */
  TextDialog(
      final String title,
      final Skin skin,
      final String windowStyleName,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * A simple Text Dialog that shows only the provided string.
   *
   * @param skin The style in which the whole dialog should be shown.
   * @param outputMsg The text which should be shown in the middle of the dialog.
   * @param confirmButton Text that the button should have; also the ID for the result handler.
   * @param title Title for the dialog.
   * @param resultHandler A callback method that is called when the confirm button is pressed.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  static Dialog createTextDialog(
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

  /**
   * Create a {@link BiFunction} that removes the UI-Entity from the game and closes the dialog if
   * the close button was pressed.
   *
   * @param entity UI-Entity
   * @param closeButtonID ID of the close button. The handler will use the ID to execute the correct
   *     close logic.
   * @return The configured BiFunction that closes the window and removes the entity from the game
   *     if the close button was pressed.
   */
  private static BiFunction<Dialog, String, Boolean> createResultHandler(
      final Entity entity, final String closeButtonID) {
    return (d, id) -> {
      if (Objects.equals(id, closeButtonID)) {
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }

  /**
   * when a Button event happened calls the stored resultHandler and when the resultHandler returns
   * a false stops the default hide on button press.
   *
   * @param object Object associated with the button
   */
  @Override
  protected void result(final Object object) {
    if (!resultHandler.apply(this, object.toString())) cancel();
  }
}
