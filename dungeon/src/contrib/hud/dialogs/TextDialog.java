package contrib.hud.dialogs;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
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
public final class TextDialog extends Dialog {

  /** Handler for Button presses. */
  private final BiFunction<TextDialog, String, Boolean> resultHandler;

  /**
   * creates a Textdialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param title Title of the dialog
   * @param resultHandler controls the button presses
   */
  public TextDialog(
      final String title,
      final Skin skin,
      final BiFunction<TextDialog, String, Boolean> resultHandler) {
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
  public TextDialog(
      final String title,
      final Skin skin,
      final String windowStyleName,
      final BiFunction<TextDialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * Creates a dialog for displaying the text message.
   *
   * <p>The entity will already be added to the game.
   *
   * @param content Text which should be shown in the body of the dialog.
   * @param buttonText Text which should be shown in the button for closing the TextDialog.
   * @param windowText Text which should be shown as the name for the TextDialog.
   * @param width Width of the dialog.
   * @param height Height of the dialog.
   * @param align {@link com.badlogic.gdx.utils.Align Alignment} of the text.
   * @return Entity that contains the {@link UIComponent}. The entity will already be added to the
   *     game by this method.
   */
  public static Entity textDialog(
      final String content,
      final String buttonText,
      final String windowText,
      int width,
      int height,
      int align) {
    Entity entity = new Entity();
    UIUtils.show(
        () -> {
          Dialog textDialog =
              DialogFactory.createTextDialog(
                  defaultSkin(),
                  content,
                  buttonText,
                  windowText,
                  createResultHandler(entity, buttonText),
                  width,
                  height,
                  align);
          UIUtils.center(textDialog);
          return textDialog;
        },
        entity);
    Game.add(entity);
    return entity;
  }

  /**
   * Creates a dialog for displaying the text message.
   *
   * <p>The entity will already be added to the game.
   *
   * @param content Text which should be shown in the body of the dialog.
   * @param buttonText Text which should be shown in the button for closing the TextDialog.
   * @param windowText Text which should be shown as the name for the TextDialog.
   * @return Entity that contains the {@link UIComponent}. The entity will already be added to the *
   *     game by this method.
   */
  public static Entity textDialog(
      final String content, final String buttonText, final String windowText) {
    return textDialog(content, buttonText, windowText, 150, 150, Align.center);
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
  private static BiFunction<TextDialog, String, Boolean> createResultHandler(
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
