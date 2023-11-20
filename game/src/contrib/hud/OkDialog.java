package contrib.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A Dialog with a "ok" Button on the Bottom.
 *
 * <p>Use {@link #showOkDialog(String, String, IVoidFunction)} to create a simple dialog.
 */
public class OkDialog {

    public static final String DEFAULT_OK_BUTTON = "Ok";

    /**
     * Show a Ok-Dialog
     *
     * @param text text to show in the dialog
     * @param title title of the dialog window
     * @param onOk function to execute if "ok" is pressed
     * @return Entity that stores the HUD components.
     */
    public static Entity showOkDialog(
            final String text, final String title, final IVoidFunction onOk) {

        Entity entity = showOkDialog(UITools.DEFAULT_SKIN, text, title, onOk);
        Game.add(entity);
        return entity;
    }

    /**
     * Show a Ok-Dialog.
     *
     * @param skin UI skin to use
     * @param text text to show in the dialog
     * @param title title of the dialog window
     * @param onOk function to execute if "ok" is pressed
     * @return Entity that stores the HUD components.
     */
    public static Entity showOkDialog(
            final Skin skin, final String text, final String title, final IVoidFunction onOk) {
        Entity entity = new Entity();

        UITools.show(
                () -> {
                    Dialog dialog =
                            createOkDialog(
                                    skin,
                                    text,
                                    title,
                                    createResultHandlerYesNo(entity, DEFAULT_OK_BUTTON, onOk));
                    UITools.centerActor(dialog);
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
                .add(DialogDesign.createTextDialog(skin, UITools.formatStringForDialogWindow(text)))
                .center()
                .grow();
        textDialog.button(DEFAULT_OK_BUTTON, DEFAULT_OK_BUTTON);
        textDialog.pack(); // resizes to size
        return textDialog;
    }

    private static BiFunction<TextDialog, String, Boolean> createResultHandlerYesNo(
            final Entity entity, final String okButtonId, final IVoidFunction onOk) {
        return (d, id) -> {
            if (Objects.equals(id, okButtonId)) {
                onOk.execute();
                Game.remove(entity);
                return true;
            }
            return false;
        };
    }
}
