package contrib.hud.dialogs;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import core.utils.logging.DungeonLogger;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A Dialog with an "ok" Button on the Bottom.
 *
 * <p>Use {@link #showOkDialog(String, String, IVoidFunction)} to create a simple dialog.
 */
public final class OkDialog {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(OkDialog.class);

  /** WTF? . */
  public static final String DEFAULT_OK_BUTTON = "Ok";

  /**
   * Show an Ok-Dialog.
   *
   * <p>Entity will already be added to the game.
   *
   * <p>If no UI is available (e.g. in headless mode), the onOk function will be executed
   * immediately and a dummy entity will be returned (will not be added to the game).
   *
   * @param text text to show in the dialog
   * @param title title of the dialog window
   * @param onOk function to execute if "ok" is pressed
   * @return Entity that stores the HUD components.
   */
  public static Entity showOkDialog(
      final String text, final String title, final IVoidFunction onOk) {
    Entity entity;
    entity = showOkDialog(defaultSkin(), text, title, onOk);
    Game.add(entity);
    return entity;
  }

  /**
   * Show an Ok-Dialog.
   *
   * <p>Entity will already be added to the game.
   *
   * @param skin UI skin to use
   * @param text text to show in the dialog
   * @param title title of the dialog window
   * @param onOk function to execute if "ok" is pressed
   * @return Entity that stores the HUD components.
   */
  public static Entity showOkDialog(
      final Skin skin, final String text, final String title, final IVoidFunction onOk) {
    Entity entity = new Entity("okDialog_" + title);

    UIUtils.show(
        () -> {
          Dialog dialog = createOkDialog(skin, text, title, createResultHandlerYesNo(entity, onOk));
          UIUtils.center(dialog);
          return dialog;
        },
        entity);
    Game.add(entity);
    return entity;
  }

  /**
   * A simple Ok Dialog that shows only the provided string.
   *
   * <p>This dialog is only created here, it is not added to the stage or game. Use
   * {@link #showOkDialog(String, String, IVoidFunction)} to create and add the dialog to the game.
   *
   * @param skin The style in which the whole dialog should be shown.
   * @param text The text which should be shown in the middle of the dialog.
   * @param title Title for the dialog.
   * @param resultHandler A callback method that is called when the ok button is pressed.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  public static Dialog createOkDialog(
      final Skin skin,
      final String text,
      final String title,
      final BiFunction<TextDialog, String, Boolean> resultHandler) {
    Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, UIUtils.formatString(text)))
        .center()
        .grow();
    textDialog.button(DEFAULT_OK_BUTTON, DEFAULT_OK_BUTTON);
    textDialog.pack(); // resizes to size
    return textDialog;
  }

  private static BiFunction<TextDialog, String, Boolean> createResultHandlerYesNo(
      final Entity entity, final IVoidFunction onOk) {
    return (d, id) -> {
      if (Objects.equals(id, OkDialog.DEFAULT_OK_BUTTON)) {
        onOk.execute();
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }
}
