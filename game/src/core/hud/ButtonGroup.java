package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import core.utils.Constants;

import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;

import java.util.ArrayList;

/**
 * Buttons are added, a minimum and maximum number of ticked buttons are enforced. Thus, (button,
 * text button, check box, etc.) can be used synchronously as "radio buttons". The VerticalGroup is
 * used as the base class (Vertical representation of all button elements).
 */
public class ButtonGroup extends VerticalGroup {

    /** Maximum length of the content in one line */
    private static final int MAX_ROW_LENGTH = 40;

    /**
     * Constructor Fills the vertical button group with text boxes and text contents for it
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     */
    public ButtonGroup(Skin skin, QuizQuestion quizQuestion) {
        super();
        final QuizQuestion.QuizQuestionType questionType = quizQuestion.type();
        final QuizQuestionContent[] answers = quizQuestion.answers();
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup btnGroup =
                new com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup();

        for (QuizQuestionContent answer : answers) {
            if (answer.type() != QuizQuestionContent.QuizQuestionContentType.IMAGE) {
                String[] answerContent = {answer.content()};
                formatStringForWindow(answerContent);
                CheckBox newCheckBox = new CheckBox(answerContent[0], skin);
                newCheckBox.align(Align.left);
                checkBoxes.add(newCheckBox);
            }
        }

        for (CheckBox checkBox : checkBoxes) {
            btnGroup.add(checkBox);
            this.addActor(checkBox);
        }

        btnGroup.uncheckAll();
        btnGroup.setMinCheckCount(0);

        if (questionType == QuizQuestion.QuizQuestionType.MULTIPLE_CHOICE) {
            btnGroup.setMaxCheckCount(answers.length);
        } else if (questionType == QuizQuestion.QuizQuestionType.SINGLE_CHOICE) {
            btnGroup.setMaxCheckCount(1);
        }

        btnGroup.setUncheckLast(true);
        this.align(Align.left);
        this.left();
    }
    /**
     * Formatting of the text contents belonging to the respective checkboxes
     *
     * @param arrayOfMessages Text content for checkboxes
     */
    private static void formatStringForWindow(String[] arrayOfMessages) {

        if (arrayOfMessages != null && arrayOfMessages.length != 0) {
            String infoMsg = arrayOfMessages[0];
            infoMsg = infoMsg.replaceAll("\n", " ");

            String[] words = infoMsg.split(" ");
            String formattedMsg = Constants.EMPTY_MESSAGE;
            int sumLength = 0;

            for (String word : words) {
                sumLength += word.length();
                formattedMsg = formattedMsg.concat(word).concat(" ");

                if (sumLength > MAX_ROW_LENGTH) {
                    formattedMsg += "\n";
                    sumLength = 0;
                }
            }
            arrayOfMessages[0] = formattedMsg;
        }
    }
}
