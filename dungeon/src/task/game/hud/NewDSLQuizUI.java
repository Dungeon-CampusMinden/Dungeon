package task.game.hud;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.Game;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import core.utils.IVoidFunction;
import newdsl.events.EventHandler;
import newdsl.events.GradeEvent;
import newdsl.events.GradeReceivedEvent;
import newdsl.tasks.*;

public class NewDSLQuizUI {

    public static final String DEFAULT_DIALOG_CONFIRM = "Bestätigen";

    public static final String DEFAULT_DIALOG_ABORT = "Abbrechen";

    private static Consumer<GradeReceivedEvent> handleGradeReceivedEvent = gradeReceivedEvent -> {
        float score = gradeReceivedEvent.getPoints();
        Task task = gradeReceivedEvent.getTask();
        StringBuilder output = new StringBuilder();
        output.append("Du hast ").append(score).append(" Punkte erreicht").append(System.lineSeparator()).append("Die Aufgabe ist damit ");
        if (task.getState() == TaskState.FINISHED_CORRECT) output.append("korrekt ");
        else output.append("falsch ");
        output.append("gelöst");

        IVoidFunction showCorrectAnswer = () -> OkDialog.showOkDialog(task.correctAnswersAsString(), "Korrekte Antwort", () -> {
        });

        OkDialog.showOkDialog(output.toString(), "Ergebnis", () -> {
            // if task was finished wrong show correct answers
            if (task.getState().equals(TaskState.FINISHED_WRONG)) {
                if (!task.getExplanation().isBlank() && !task.getExplanation().equals(Task.DEFAULT_EXPLANATION)) {
                    OkDialog.showOkDialog(task.getExplanation(), "Erklärung", showCorrectAnswer);
                } else {
                    showCorrectAnswer.execute();
                }
            }
        });
    };

    public static Entity askSingleChoiceQuizOnHud(final SingleChoiceTask sc) {
        EventHandler.subscribe(GradeReceivedEvent.class, sc.getId(), handleGradeReceivedEvent);

        return NewDSLQuizUI.showQuizDialog(sc, (Entity hudEntity) -> NewDSLUIAnswerCallback.uiCallback(sc, hudEntity, (task, givenAnswers) -> {
            EventHandler.publish(new GradeEvent(task.getId(), givenAnswers), sc.getId());
        }));
    }

    public static Entity askMultipleChoiceQuizOnHud(final MultipleChoiceTask mc) {
        EventHandler.subscribe(GradeReceivedEvent.class, mc.getId(), handleGradeReceivedEvent);

        return NewDSLQuizUI.showQuizDialog(mc, (Entity hudEntity) -> NewDSLUIAnswerCallback.uiCallback(mc, hudEntity, (task, givenAnswers) -> {
            EventHandler.publish(new GradeEvent(task.getId(), givenAnswers), mc.getId());
        }));
    }


    public static Entity showQuizDialog(Task<ChoiceAnswer> question, Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {

        String title = question.getId();
        Entity entity = showQuizDialog(question, UIUtils.formatString(question.getTitle()), DEFAULT_DIALOG_CONFIRM, title, resulthandlerLinker);
        Game.add(entity);
        return entity;
    }

    private static Entity showQuizDialog(Task<ChoiceAnswer> question, String questionMsg, String buttonMsg, String dialogTitle, Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {
        Entity entity = new Entity();

        UIUtils.show(() -> {
            Dialog quizDialog = createQuizDialog(defaultSkin(), question, questionMsg, buttonMsg, dialogTitle, resulthandlerLinker.apply(entity));
            UIUtils.center(quizDialog);
            return quizDialog;
        }, entity);
        Game.add(entity);
        return entity;
    }


    private static Dialog createQuizDialog(Skin skin, Task<ChoiceAnswer> quizQuestion, String outputMsg, String buttonMsg, String title, BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
        textDialog.getContentTable().add(NewDSLQuizDialogDesign.createQuizQuestion(quizQuestion, skin, outputMsg)).grow().fill(); // changes size based on childrens;
        textDialog.button(DEFAULT_DIALOG_ABORT, DEFAULT_DIALOG_ABORT);
        textDialog.button(buttonMsg, buttonMsg);
        textDialog.pack(); // resizes to size
        return textDialog;
    }


    public static BiFunction<TextDialog, String, Boolean> createResultHandlerQuiz(final Entity entity, final String confirmButtonID, String abortButtonID) {
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
