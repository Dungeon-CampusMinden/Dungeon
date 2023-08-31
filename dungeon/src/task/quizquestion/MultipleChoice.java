package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * A {@link Quiz} that needs the player to select multiple answers from a collection of
 * possibilities.
 */
public class MultipleChoice extends Quiz {
    public MultipleChoice(String questionText, Image image) {
        super(questionText, image);
    }

    public MultipleChoice(String questionText) {
        super(questionText);
    }
}
