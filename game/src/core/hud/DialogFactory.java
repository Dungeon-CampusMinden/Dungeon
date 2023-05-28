package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import quizquestion.QuizQuestion;

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
        Dialog textDialog = new TextDialog(title, skin);
        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        dialogDesign.QuizQuestion(quizQuestion, skin, outputMsg);
        textDialog.addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        textDialog.button(buttonMsg, BUTTON_ID);
        return textDialog;
    }

    public static Dialog createTextDialog(
            Skin skin, String outputMsg, String buttonMsg, String title) {
        Dialog textDialog = new TextDialog(title, skin);

        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        dialogDesign.TextDialog(skin, outputMsg);
        textDialog.addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        textDialog.button(buttonMsg, BUTTON_ID);
        return textDialog;
    }
}
