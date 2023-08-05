package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Entity;
import core.Game;
import core.hud.TextDialog;
import core.hud.UITools;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class QuizUI {

    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     *
     * <p>BlackMagic number which can be tweaked for better line break VirtualWindowWidth / FontSize
     * = MAX_ROW_LENGTH 480 / 12 = 40
     */
    private static final int MAX_ROW_LENGTH = 40;

    /**
     * Display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) on the HUD.
     *
     * @param question Question to show on the HUD
     * @param resulthandlerLinker callback function
     * @return the Entity that stores the {@link core.components.UIComponent} with the UI-Elements.
     */
    public static Entity showQuizDialog(
            Quiz question,
            Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {
        return showQuizDialog(
                question,
                formatStringForDialogWindow(question.taskText()),
                core.hud.UITools.DEFAULT_DIALOG_CONFIRM,
                core.hud.UITools.DEFAULT_DIALOG_TITLE,
                resulthandlerLinker);
    }

    /**
     * Display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) on the HUD.
     *
     * <p>Use default callback method, that will delete the hud-entity from the game.
     *
     * @param question Question to show on the HUD
     * @return the Entity that stores the {@link core.components.UIComponent} with the UI-Elements.
     */
    public static Entity showQuizDialog(Quiz question) {
        return showQuizDialog(
                question,
                (entity) ->
                        createResultHandlerQuiz(entity, core.hud.UITools.DEFAULT_DIALOG_CONFIRM));
    }

    /**
     * If no Quiz-Dialogue is created, a new dialogue is created according to the event key. Pause
     * all systems except DrawSystem
     *
     * <p>display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) in the Dialog
     *
     * @param question Various question configurations
     */
    private static Entity showQuizDialog(
            Quiz question,
            String questionMsg,
            String buttonMsg,
            String dialogTitle,
            Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {
        Entity entity = new Entity();

        core.hud.UITools.show(
                () -> {
                    Dialog quizDialog =
                            createQuizDialog(
                                    UITools.DEFAULT_SKIN,
                                    question,
                                    questionMsg,
                                    buttonMsg,
                                    dialogTitle,
                                    resulthandlerLinker.apply(entity));
                    UITools.centerActor(quizDialog);
                    return quizDialog;
                },
                entity);

        return entity;
    }

    /**
     * Factory for a generic Quizquestion.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     * @param resultHandler a callback method which is called when the confirm button is pressed
     * @return the fully configured Dialog which then can be added where it is needed
     */
    private static Dialog createQuizDialog(
            Skin skin,
            Quiz quizQuestion,
            String outputMsg,
            String buttonMsg,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, resultHandler);
        textDialog
                .getContentTable()
                .add(QuizDialogDesign.createQuizQuestion(quizQuestion, skin, outputMsg))
                .grow()
                .fill(); // changes size based on childrens;
        textDialog.button(buttonMsg, buttonMsg);

        textDialog.pack(); // resizes to size
        return textDialog;
    }

    /**
     * creates line breaks after a word once a certain char count is reached
     *
     * @param string which should be reformatted.
     */
    public static String formatStringForDialogWindow(String string) {
        String[] words = string.split(" ");
        StringBuilder formattedMsg = new StringBuilder(string.length());
        int sumLength = 0;

        for (String word : words) {
            sumLength += word.length();
            formattedMsg.append(word);
            formattedMsg.append(" ");

            if (sumLength > MAX_ROW_LENGTH) {
                formattedMsg.append("\n");
                sumLength = 0;
            }
        }
        return formattedMsg.toString().trim();
    }

    /**
     * Create a default callback-function that will delete the entity that stores the hud-component.
     */
    public static BiFunction<TextDialog, String, Boolean> createResultHandlerQuiz(
            final Entity entity, final String closeButtonID) {
        return (d, id) -> {
            if (Objects.equals(id, closeButtonID)) {
                Game.remove(entity);
                return true;
            }
            return false;
        };
    }
}
