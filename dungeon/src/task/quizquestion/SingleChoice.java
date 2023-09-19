package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import task.Quiz;

/** A {@link Quiz} that needs the player to select one answer from a collection of possibilities. */
public class SingleChoice extends Quiz {
    public SingleChoice(String questionText, Image image) {
        super(questionText, image);
    }

    public SingleChoice(String questionText) {
        super(questionText);
    }
}
