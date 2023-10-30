package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import reporting.GradingFunctions;

import task.Quiz;

/**
 * A {@link Quiz} that needs the player to select multiple answers from a collection of
 * possibilities.
 */
public class MultipleChoice extends Quiz {
    public MultipleChoice(String questionText, Image image) {
        super(questionText, image);
        scoringFunction(GradingFunctions.multipeChoiceGrading());
    }

    public MultipleChoice(String questionText) {
        super(questionText);
        scoringFunction(GradingFunctions.multipeChoiceGrading());
    }
}
