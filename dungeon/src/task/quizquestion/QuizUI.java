package task.quizquestion;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.UIComponent;
import contrib.hud.OkDialog;
import contrib.hud.TextDialog;
import contrib.hud.UITools;

import core.Entity;
import core.Game;

import task.Quiz;
import task.Task;
import task.TaskContent;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
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
     * Ask a Quizquestion on the HUD and trigger the grading function, after the player confirmed
     * the answers.
     *
     * @param quiz Question to ask
     * @return the entity that stores the hud elements for the dialog window.
     */
    public static Entity askQuizOnHud(final Quiz quiz) {
        return QuizUI.showQuizDialog(
                quiz,
                (Entity hudEntity) ->
                        UIAnswerCallback.uiCallback(
                                quiz,
                                hudEntity,
                                new BiConsumer<Task, Set<TaskContent>>() {
                                    @Override
                                    public void accept(Task task, Set<TaskContent> taskContents) {
                                        float score = task.gradeTask(taskContents);
                                        StringBuilder output = new StringBuilder();
                                        output.append("Du hast ")
                                                .append(score)
                                                .append("/")
                                                .append(task.points())
                                                .append(" Punkte erreicht")
                                                .append(System.lineSeparator())
                                                .append("Die Aufgabe ist damit ");
                                        if (task.state() == Task.TaskState.FINISHED_CORRECT)
                                            output.append("korrekt ");
                                        else output.append("falsch ");
                                        output.append("gelöst");
                                        OkDialog.showOkDialog(
                                                output.toString(),
                                                "Ergebnis",
                                                () -> {
                                                    // if task was finisehd wrong show correct
                                                    // answers
                                                    if (score < task.points()) {
                                                        OkDialog.showOkDialog(
                                                                task.correctAnswersAsString(),
                                                                "Korrekte Antwort",
                                                                () -> {});
                                                    }
                                                });
                                    }
                                }));
    }

    /**
     * Display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) on the HUD.
     *
     * @param question Question to show on the HUD
     * @param resulthandlerLinker callback function
     * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
     *     already be added to the game by this method.
     */
    public static Entity showQuizDialog(
            Quiz question,
            Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {

        String title = question.taskName();
        Entity entity =
                showQuizDialog(
                        question,
                        formatStringForDialogWindow(question.taskText()),
                        UITools.DEFAULT_DIALOG_CONFIRM,
                        title,
                        resulthandlerLinker);
        Game.add(entity);
        return entity;
    }

    /**
     * Display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) on the HUD.
     *
     * <p>Use default callback method, that will delete the hud-entity from the game.
     *
     * @param question Question to show on the HUD
     * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
     *     already be added to the game by this method.
     */
    public static Entity showQuizDialog(Quiz question) {
        return showQuizDialog(
                question,
                (entity) ->
                        createResultHandlerQuiz(
                                entity,
                                UITools.DEFAULT_DIALOG_CONFIRM,
                                UITools.DEFAULT_DIALOG_ABORT));
    }

    /**
     * If no Quiz-Dialogue is created, a new dialogue is created according to the event key. Pause
     * all systems except DrawSystem
     *
     * <p>display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) in the Dialog
     *
     * @param question Various question configurations
     * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
     *     already be added to the game by this method.
     */
    private static Entity showQuizDialog(
            Quiz question,
            String questionMsg,
            String buttonMsg,
            String dialogTitle,
            Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {
        Entity entity = new Entity();

        UITools.show(
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
        Game.add(entity);
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
        Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
        textDialog
                .getContentTable()
                .add(QuizDialogDesign.createQuizQuestion(quizQuestion, skin, outputMsg))
                .grow()
                .fill(); // changes size based on childrens;
        textDialog.button(UITools.DEFAULT_DIALOG_ABORT, UITools.DEFAULT_DIALOG_ABORT);
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
        StringBuilder formattedMsg = new StringBuilder();
        String[] lines = string.split(System.lineSeparator());

        for (String line : lines) {
            String[] words = line.split(" ");
            int sumLength = 0;

            for (String word : words) {
                sumLength += word.length();
                formattedMsg.append(word);
                formattedMsg.append(" ");

                if (sumLength > MAX_ROW_LENGTH) {
                    formattedMsg.append(System.lineSeparator());
                    sumLength = 0;
                }
            }
            formattedMsg.append(System.lineSeparator());
        }
        return formattedMsg.toString().trim();
    }

    /**
     * Create a default callback-function that will delete the entity that stores the hud-component.
     */
    public static BiFunction<TextDialog, String, Boolean> createResultHandlerQuiz(
            final Entity entity, final String confirmButtonID, String abortButtonID) {
        return (d, id) -> {
            if (Objects.equals(id, confirmButtonID)) {
                Game.remove(entity);
                return true;
            }
            if (Objects.equals(id, abortButtonID)) {
                Game.remove(entity);
                return true;
            }
            return false;
        };
    }
}
