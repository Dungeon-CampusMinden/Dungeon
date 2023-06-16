package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.function.BiFunction;

/**
 * Provides ease of use Factory methods to create a com.badlogic.gdx.scenes.scene2d.ui.Dialog based
 * Dialog for Questions and a simple Text
 */
public class DialogFactory {

    /**
     * a simple Text Dialog which shows only the provided string.
     *
     * @param skin the style in which the whole dialog should be shown
     * @param outputMsg the Text which should be shown in the middle of the dialog
     * @param confirmButton text which the button should have is also the ID for the resulthandler
     * @param title for the dialog
     * @param resultHandler a callback method which is called when the confirm button is pressed
     * @return the fully configured Dialog which then can be added where it is needed
     */
    public static Dialog createTextDialog(
            Skin skin,
            String outputMsg,
            String confirmButton,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, resultHandler);
        textDialog
                .getContentTable()
                .add(DialogDesign.createTextDialog(skin, outputMsg))
                .center()
                .grow();
        textDialog.button(confirmButton, confirmButton);
        return textDialog;
    }
}
