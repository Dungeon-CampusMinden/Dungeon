package task.tasktype.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import task.reporting.GradingFunctions;
import task.tasktype.Quiz;

/**
 * A {@link Quiz} that needs the player to select multiple answers from a collection of
 * possibilities.
 */
public class MultipleChoice extends Quiz {

  /**
   * WTF? .
   *
   * @param questionText foo
   * @param image foo
   */
  public MultipleChoice(String questionText, Image image) {
    super(questionText, image);
    scoringFunction(GradingFunctions.multipeChoiceGrading());
  }

  /**
   * WTF? .
   *
   * @param questionText foo
   */
  public MultipleChoice(String questionText) {
    super(questionText);
    scoringFunction(GradingFunctions.multipeChoiceGrading());
  }
}
