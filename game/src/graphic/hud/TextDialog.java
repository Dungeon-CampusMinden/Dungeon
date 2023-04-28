package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;
import tools.Constants;
import tools.Point;
// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UITest.java

/** Contains Constructor, which immediately creates the dialogue including all its elements. */
public final class TextDialog extends Dialog {

    /** button ID (used when control is pressed) */
    private static final String btnID = "confirm exit";
    private static final String defaultMsg = "No message was load.";
    private static final String defaultBtnMsg = "OK";

    /**
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     */
    public TextDialog(Skin skin, QuizQuestion quizQuestion, String outputMsg, String buttonMsg, String title) {
        super(title, skin);
        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = defaultMsg;
        dialogDesign.QuizQuestion( quizQuestion, skin, outputMsg );
        addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = defaultBtnMsg;
        button(buttonMsg, btnID);
    }


    public TextDialog(Skin skin, String outputMsg, String buttonMsg, String title) {
        super(title, skin);
        DialogDesign dialogDesign = new DialogDesign();
        if (outputMsg.trim().isEmpty()) outputMsg = defaultMsg;
        dialogDesign.TextDialog( skin, outputMsg );
        addActor(dialogDesign);
        if (buttonMsg.trim().isEmpty()) buttonMsg = defaultBtnMsg;
        button(buttonMsg, btnID);
    }

    /**
     * Provides information about the pressed Button
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (object.toString().equals(btnID)) {
            UITools.deleteDialogue(this);
        }
    }
}
