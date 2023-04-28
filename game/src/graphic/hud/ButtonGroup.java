package graphic.hud;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;
import tools.Constants;

import java.util.ArrayList;
// Package com.badlogic.gdx.scenes.scene2d.ui
// https://www.demo2s.com/java/badlogic-gdx-buttongroup-tutorial-with-examples.html
public class ButtonGroup extends VerticalGroup{

    public ButtonGroup(Skin skin, QuizQuestion quizQuestion ) {
        super();
        final QuizQuestion.QuizQuestionType questioType = quizQuestion.type();
        final QuizQuestionContent[] answers = quizQuestion.answers();
        ArrayList< CheckBox > checkBoxes = new ArrayList<CheckBox>();
        com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup btnGroup = new com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup();

        for (QuizQuestionContent answer : answers ) {
            if( answer.type() != QuizQuestionContent.QuizQuestionContentType.IMAGE ) {
                String[] answerContent = {answer.content()};
                formatStringForWindow(answerContent);
                checkBoxes.add(new CheckBox(answerContent[0], skin));
            }
        }

        for (CheckBox b : checkBoxes) {
            this.addActor(b);
            btnGroup.add(b);
        }

        btnGroup.uncheckAll();
        btnGroup.setMinCheckCount(0);

        if( questioType == QuizQuestion.QuizQuestionType.MULTIPLE_CHOICE)
        {
            btnGroup.setMaxCheckCount(answers.length);
        }
        else if( questioType == QuizQuestion.QuizQuestionType.SINGLE_CHOICE)
            btnGroup.setMaxCheckCount(1);

        btnGroup.setUncheckLast(true);
        this.align( Align.topLeft );
    }

    private static void formatStringForWindow(String[] arrayOfMessages) {
        String[] words = arrayOfMessages[0].split(" ");
        String formatedMsg = Constants.EMPTY_MESSAGE;
        int sumLength = 0;

        for (String word : words) {
            sumLength += word.length();
            formatedMsg = formatedMsg.concat(word).concat(" ");

            if (sumLength > 40) {
                formatedMsg += "\n";
                sumLength = 0;
            }
        }

        for( int length = formatedMsg.length(); length < 40; length++ )
        {
            formatedMsg+= " ";
        }

        arrayOfMessages[0] = formatedMsg;
    }
}
