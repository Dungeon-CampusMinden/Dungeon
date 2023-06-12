package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import quizquestion.QuizQuestion;

import java.util.function.BiFunction;

/**
 * Provides ease of use Factory methods to create a com.badlogic.gdx.scenes.scene2d.ui.Dialog based
 * Dialog for Questions and a simple Text
 */
public class DialogFactory {

    /**
     * Factory for a generic Quizquestion.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     * @param resultHandler a callback method which is called when the confirm button is pressed
     * @return the fully configured Dialog which then can be added where it is needed
     */
    public static Dialog createQuizDialog(
            Skin skin,
            QuizQuestion quizQuestion,
            String outputMsg,
            String buttonMsg,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, resultHandler);
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.createQuizQuestion(quizQuestion, skin, outputMsg);
        textDialog.button(buttonMsg, buttonMsg);
        textDialog
                .getContentTable()
                .add(dialogDesign)
                .expand()
                .fill(); // changes size based on childrens
        textDialog.pack(); // resizes to size
        return textDialog;
    }

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
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.createTextDialog(skin, outputMsg);
        textDialog.addActor(dialogDesign);
        textDialog.button(confirmButton, confirmButton);
        return textDialog;
    }
}
