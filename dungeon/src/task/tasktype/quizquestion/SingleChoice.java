package task.tasktype.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import task.reporting.GradingFunctions;
import task.tasktype.Quiz;

/** A {@link Quiz} that needs the player to select one answer from a collection of possibilities. */
public class SingleChoice extends Quiz {
    public SingleChoice(String questionText, Image image) {
        super(questionText, image);
        scoringFunction(GradingFunctions.singleChoiceGrading());
    }

    public SingleChoice(String questionText) {
        super(questionText);
        scoringFunction(GradingFunctions.singleChoiceGrading());
    }
}
