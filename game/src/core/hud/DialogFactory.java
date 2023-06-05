package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import quizquestion.QuizQuestion;

import java.util.Objects;
import java.util.function.BiPredicate;

public class DialogFactory {
    /** button ID (used when control is pressed) */
    private static final String BUTTON_ID = "confirm exit";
    /** Default message when no text is transferred */
    private static final String DEFAULT_MSG = "No message was load.";
    /** Default Button message */
    private static final String DEFAULT_BUTTON_MSG = "OK";

    /**
     * Constructor for Quiz Question
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     */
    public static Dialog createQuizDialog(
            Skin skin,
            QuizQuestion quizQuestion,
            String outputMsg,
            String buttonMsg,
            String title) {
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        Dialog textDialog = new TextDialog(title, skin, getResultHandler(buttonMsg));
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.QuizQuestion(quizQuestion, skin, outputMsg);
        textDialog.addActor(dialogDesign);
        textDialog.button(buttonMsg, BUTTON_ID);
        return textDialog;
    }

    private static BiPredicate<TextDialog, String> getResultHandler(final String closeButtonMsg) {
        return (d, id) -> Objects.equals(id, closeButtonMsg);
    }

    public static Dialog createTextDialog(
            Skin skin, String outputMsg, String buttonMsg, String title) {
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        Dialog textDialog = new TextDialog(title, skin, getResultHandler(buttonMsg));
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.TextDialog(skin, outputMsg);
        textDialog.addActor(dialogDesign);
        textDialog.button(buttonMsg, BUTTON_ID);
        return textDialog;
    }
}
