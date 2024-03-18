package contrib.hud.dialogs;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A dialog with "yes" and "no" buttons at the bottom.
 *
 * <p>Use {@link #showYesNoDialog(String, String, IVoidFunction, IVoidFunction)} to create a simple
 * dialog.
 */
public final class YesNoDialog {
  private static final String DEFAULT_DIALOG_YES = "Ja";
  private static final String DEFAULT_DIALOG_NO = "Nein";

  /**
   * Show a Yes or No Dialog.
   *
   * <p>The entity will already be added to the game.
   *
   * @param text Text to show in the dialog.
   * @param title Title of the dialog window.
   * @param onYes Function to execute if "yes" is pressed.
   * @param onNo Function to execute if "no" is pressed.
   * @return Entity that stores the HUD components.
   */
  public static Entity showYesNoDialog(
      final String text, final String title, final IVoidFunction onYes, final IVoidFunction onNo) {

    Entity entity = showYesNoDialog(defaultSkin(), text, title, onYes, onNo);
    Game.add(entity);
    return entity;
  }

  /**
   * Show a Yes or No Dialog.
   *
   * <p>The entity will already be added to the game.
   *
   * @param skin UI skin to use.
   * @param text Text to show in the dialog.
   * @param title Title of the dialog window.
   * @param onYes Function to execute if "yes" is pressed.
   * @param onNo Function to execute if "no" is pressed.
   * @return Entity that stores the HUD components.
   */
  public static Entity showYesNoDialog(
      final Skin skin,
      final String text,
      final String title,
      final IVoidFunction onYes,
      final IVoidFunction onNo) {
    Entity entity = new Entity();

    UIUtils.show(
        () -> {
          Dialog dialog =
              createYesNoDialog(skin, text, title, createResultHandlerYesNo(entity, onYes, onNo));
          UIUtils.center(dialog);
          return dialog;
        },
        entity);
    Game.add(entity);
    return entity;
  }

  private static Dialog createYesNoDialog(
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
    textDialog.button(DEFAULT_DIALOG_NO, DEFAULT_DIALOG_NO);
    textDialog.button(DEFAULT_DIALOG_YES, DEFAULT_DIALOG_YES);
    textDialog.pack(); // resizes to size
    return textDialog;
  }

  private static BiFunction<TextDialog, String, Boolean> createResultHandlerYesNo(
      final Entity entity, final IVoidFunction onYes, final IVoidFunction onNo) {
    return (d, id) -> {
      if (Objects.equals(id, DEFAULT_DIALOG_YES)) {
        onYes.execute();
        Game.remove(entity);
        return true;
      }
      if (Objects.equals(id, DEFAULT_DIALOG_NO)) {
        onNo.execute();
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }
}
