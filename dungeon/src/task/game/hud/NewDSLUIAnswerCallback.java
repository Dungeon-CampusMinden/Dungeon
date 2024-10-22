package task.game.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.SnapshotArray;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.Game;
import newdsl.tasks.ChoiceAnswer;
import newdsl.tasks.SingleChoiceTask;
import newdsl.tasks.Task;
import task.TaskContent;
import task.tasktype.Quiz;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class NewDSLUIAnswerCallback {

    public static BiConsumer<Entity, Entity> askOnInteraction(SingleChoiceTask quiz, BiConsumer<Task, Set<ChoiceAnswer>> dslCallback) {
        return (questGiver, player) -> NewDSLQuizUI.showQuizDialog(quiz, (Entity hudEntity) -> uiCallback(quiz, hudEntity, dslCallback));
    }

    public static BiFunction<TextDialog, String, Boolean> uiCallback(Task<ChoiceAnswer> task, Entity hudEntity, BiConsumer<Task, Set<ChoiceAnswer>> dslCallback) {
        return (textDialog, id) -> {
            dslCallback.accept(task, getChoiceAnswer(task, answerSection(textDialog)));
            Game.remove(hudEntity);
            return true;
        };
    }

    private static VerticalGroup answerSection(TextDialog textDialog) {
        SnapshotArray<Actor> children = ((VerticalGroup) textDialog.getContentTable().getChildren().get(0)).getChildren();
        // find the answer section
        return (VerticalGroup) children.select((actor) -> Objects.equals(actor.getName(), NewDSLQuizDialogDesign.ANSWERS_GROUP_NAME)).iterator().next();
    }

    private static Set<ChoiceAnswer> getChoiceAnswer(Task<ChoiceAnswer> task, VerticalGroup answerSection) {
        Set<String> ans = checkboxAnswers(answerSection);
        return stringToContent(task, ans);
    }

    private static Set<ChoiceAnswer> stringToContent(Task<ChoiceAnswer> task, Set<String> answers) {
        Set<ChoiceAnswer> contentSet = new HashSet<>();
        task.getAnswers().stream().filter(answer -> answers.contains(answer.getText())).forEach(contentSet::add);


        return contentSet;
    }

    private static Set<String> checkboxAnswers(VerticalGroup answerSection) {
        Set<String> answers = new HashSet<>();

        for (Actor actor : ((VerticalGroup) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0)).getChildren().select((x) -> x instanceof CheckBox checkbox && checkbox.isChecked()))
            if (actor instanceof CheckBox checked) answers.add(checked.getText().toString());
        if (answers.size() == 0) answers.add("No Selection");
        return answers;
    }

    private static String freeTextAnswer(VerticalGroup answerSection) {
        return ((TextArea) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0)).getText();
    }
}
