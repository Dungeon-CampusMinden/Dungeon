package graphic.hud;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import quizquestion.QuizQuestion;

/**
 * Contains Constructor, which immediately creates the dialogue including all its elements.
 * https://github.com/TomGrill/gdx-dialogs
 */
public final class TextDialog extends Dialog {

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
    public TextDialog(
            Skin skin,
            QuizQuestion quizQuestion,
            String outputMsg,
            String buttonMsg,
            String title) {
        super(title, skin);
        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        dialogDesign.QuizQuestion(quizQuestion, skin, outputMsg);
        addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        button(buttonMsg, BUTTON_ID);
    }

    /**
     * Constructor for Text-Dialog
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     */
    public TextDialog(Skin skin, String outputMsg, String buttonMsg, String title) {
        super(title, skin);
        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = DEFAULT_MSG;
        dialogDesign.TextDialog(skin, outputMsg);
        addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = DEFAULT_BUTTON_MSG;
        button(buttonMsg, BUTTON_ID);
    }

    /**
     * Provides information about the pressed Button
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (object.toString().equals(BUTTON_ID)) {
            UITools.deleteDialogue(this);
        }
    }
}
