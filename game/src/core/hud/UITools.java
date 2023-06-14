package core.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Entity;
import core.Game;
import core.components.UIComponent;
import core.utils.Constants;

import quizquestion.QuizQuestion;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Formatting of the window or dialog and controls the creation of a dialogue object depending on an
 * event.
 */
public class UITools {
    public static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG));
    public static final String DEFAULT_DIALOG_CONFIRM = "confirm";
    public static final String DEFAULT_DIALOG_TITLE = "Question";

    /**
     * display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) in the Dialog
     *
     * @param question Various question configurations
     */
    public static void showQuizDialog(QuizQuestion question) {
        String questionContent =
                QuizQuestionFormatted.formatStringForDialogWindow(question.question().content());
        generateQuizDialogue(
                question, questionContent, DEFAULT_DIALOG_CONFIRM, DEFAULT_DIALOG_TITLE);
    }

    /**
     * If no Quiz-Dialogue is created, a new dialogue is created according to the event key. Pause
     * all systems except DrawSystem
     *
     * @param question Various question configurations
     */
    private static Entity generateQuizDialogue(
            QuizQuestion question, String questionMsg, String buttonMsg, String dialogTitle) {
        Entity e = new Entity();
        Dialog dialog =
                DialogFactory.createQuizDialog(
                        DEFAULT_SKIN,
                        question,
                        questionMsg,
                        buttonMsg,
                        dialogTitle,
                        getResultHandler(e, buttonMsg));
        new UIComponent(e, dialog, true);
        return e;
    }

    /**
     * created dialog for displaying the text-message
     *
     * @param content text which should be shown in the body of the dialog
     * @param buttonText text which should be shown in the button for closing the TextDialog
     * @param windowText text which should be shown as the name for the TextDialog
     */
    public static Entity generateNewTextDialog(
            String content, String buttonText, String windowText) {
        Entity e = new Entity();
        Dialog textDialog =
                DialogFactory.createTextDialog(
                        DEFAULT_SKIN,
                        content,
                        buttonText,
                        windowText,
                        getResultHandler(e, buttonText));
        new UIComponent(e, textDialog, true);
        return e;
    }

    private static BiFunction<TextDialog, String, Boolean> getResultHandler(
            final Entity entity, final String closeButtonMsg) {
        return (d, id) -> {
            if (Objects.equals(id, closeButtonMsg)) {
                Game.removeEntity(entity);
                return true;
            }
            return false;
        };
    }
}
