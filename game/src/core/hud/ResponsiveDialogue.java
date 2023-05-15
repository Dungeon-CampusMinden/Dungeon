package core.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import core.utils.Constants;
import core.utils.controller.ScreenController;

import quizquestion.QuizQuestion;

/**
 * Creates a dialogue object, formats and passes the dialogue to the `ScreenController` so that the
 * dialogue can be displayed on the screen. For better structuring and separation of the different
 * dialogue contents
 */
public class ResponsiveDialogue<T extends Actor> extends ScreenController<T> {

    /**
     * Creates a new ResponsiveDialogue (for questions and answers), exclusively for the handling of
     * quiz questions,with a new Spritebatch. Differentiation from the previous constructor in that
     * the parameter "batch" appears. batch required to be able to display the dialogue on the
     * screen.
     *
     * @param batch to display the textures
     * @param skin Resources that can be used by UI widgets
     * @param msgColor colour of the text
     * @param question Various question configurations
     * @param arrayOfMessages Content 'msg'(message), which is to be output on the screen, optional
     *     the name of the button, as well as the label heading can be passed. [0] Content displayed
     *     in the label; [1] Button name; [2]label heading
     */
    public ResponsiveDialogue(
            SpriteBatch batch,
            Skin skin,
            Color msgColor,
            QuizQuestion question,
            String... arrayOfMessages) {
        super(batch);
        TextDialog dialog = createQuizDialog(skin, question, arrayOfMessages);
        add((T) dialog);
        formatDependingOnGameScreen(dialog, msgColor);
    }

    /**
     * Creates a new ResponsiveDialogue (for text information),with a new Spritebatch.
     * Differentiation from the previous constructor in that the parameter "batch" appears. batch
     * required to be able to display the dialogue on the screen.
     *
     * @param batch to display the textures
     * @param skin Resources that can be used by UI widgets
     * @param msgColor colour of the text
     * @param arrayOfMessages Content 'msg'(message), which is to be output on the screen, optional
     *     the name of the button, as well as the label heading can be passed. [0] Content displayed
     *     in the label; [1] Button name; [2]label heading
     */
    public ResponsiveDialogue(
            SpriteBatch batch, Skin skin, Color msgColor, String... arrayOfMessages) {
        super(batch);
        TextDialog dialog = createTextDialog(skin, arrayOfMessages);
        add((T) dialog);
        formatDependingOnGameScreen(dialog, msgColor);
    }

    /**
     * created dialogue for displaying the quiz questions
     *
     * @param skin Resources that can be used by UI widgets
     * @param question Various question configurations
     * @param arrayOfMessages Content 'msg'(message), which is to be output on the screen, optional
     *     the name of the button, as well as the label heading can be passed. [0] Content displayed
     *     in the label; [1] Button name; [2]label heading
     */
    private TextDialog createQuizDialog(
            Skin skin, QuizQuestion question, String... arrayOfMessages) {
        String[] formatIdentifier = new String[3];
        setupMessagesForIdentifier(formatIdentifier, arrayOfMessages);
        return new TextDialog(
                skin, question, formatIdentifier[0], formatIdentifier[1], formatIdentifier[2]);
    }

    /**
     * created dialogue for displaying the text-message
     *
     * @param skin Resources that can be used by UI widgets
     * @param arrayOfMessages Content 'msg'(message), which is to be output on the screen, optional
     *     the name of the button, as well as the label heading can be passed. [0] Content displayed
     *     in the label; [1] Button name; [2]label heading
     */
    private TextDialog createTextDialog(Skin skin, String... arrayOfMessages) {
        String[] formatIdentifier = new String[3];
        setupMessagesForIdentifier(formatIdentifier, arrayOfMessages);
        return new TextDialog(skin, formatIdentifier[0], formatIdentifier[1], formatIdentifier[2]);
    }

    /**
     * All values for the variable Identifier are correctly read from the parameter arrayOfMessages
     * and assigned to it.
     *
     * @param formatIdentifier Parameters that are passed to the dialogue
     * @param arrayOfMessages Parameters defined by the user
     */
    private void setupMessagesForIdentifier(String[] formatIdentifier, String... arrayOfMessages) {
        final int inputArraySize = arrayOfMessages.length;
        final int outputArraySize = formatIdentifier.length;
        final String[] defaultCaptions = {
            Constants.DEFAULT_MESSAGE, Constants.DEFAULT_BUTTON_MESSAGE, Constants.DEFAULT_HEADING
        };

        for (int counter = 0; counter < outputArraySize; counter++) {
            if (inputArraySize > counter
                    && arrayOfMessages[counter] != null
                    && arrayOfMessages[counter].length() > 0)
                formatIdentifier[counter] = arrayOfMessages[counter];
            else formatIdentifier[counter] = defaultCaptions[counter];
        }
    }

    /**
     * formats dialogue depending on the game screen and positions it in the screen centre
     *
     * @param dialog with info field and button to cancel play pause
     * @param msgColor Text colour
     */
    private void formatDependingOnGameScreen(TextDialog dialog, Color msgColor) {
        dialog.setColor(msgColor);
        dialog.setWidth(Constants.WINDOW_WIDTH - Constants.DIALOG_DIFFERENCE_MEASURE);
        dialog.setHeight(Constants.WINDOW_HEIGHT - Constants.DIALOG_DIFFERENCE_MEASURE);
        dialog.setPosition(
                (Constants.WINDOW_WIDTH) / 2f,
                (Constants.WINDOW_HEIGHT) / 2f,
                Align.center | Align.top / 2);
    }
}
