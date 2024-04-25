package task.tasktype.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import task.tasktype.Quiz;

/** A {@link Quiz} that needs the player to enter a text. Note: Not yet implemented */
public class FreeText extends Quiz {

  /**
   * WTF? .
   *
   * @param questionText foo
   * @param image foo
   */
  public FreeText(String questionText, Image image) {
    super(questionText, image);
  }

  /**
   * WTF? .
   *
   * @param questionText foo
   */
  public FreeText(String questionText) {
    super(questionText);
  }
}
