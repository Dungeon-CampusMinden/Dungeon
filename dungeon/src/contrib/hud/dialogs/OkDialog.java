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
    try {
      entity = showOkDialog(defaultSkin(), text, title, onOk);
      Game.add(entity);
    } catch (IllegalStateException e) {
      // in headless just run onOkn
      // TODO: share dialogs if server
      LOGGER.warn("No UI available, executing Ok-Dialog action without UI.");
      onOk.execute();
      entity = new Entity("okDialog_noUI");
    }
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

  private static Dialog createOkDialog(
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
