package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import task.Quiz;

/** A {@link Quiz} that needs the player to enter a text. */
public class FreeText extends Quiz {
    public FreeText(String questionText, Image image) {
        super(questionText, image);
    }

    public FreeText(String questionText) {
        super(questionText);
    }
}
