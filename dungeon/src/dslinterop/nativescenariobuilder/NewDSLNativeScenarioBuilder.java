package dslinterop.nativescenariobuilder;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.path.SimpleIPath;
import newdsl.tasks.*;
import task.TaskContent;
import task.game.components.NewDSLTaskComponent;
import task.game.hud.NewDSLQuizUI;
import task.game.hud.NewDSLYesNoDialog;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * WTF? .
 */
public class NewDSLNativeScenarioBuilder {
    public static Set<Set<Entity>> buildSingleChoiceTask(SingleChoiceTask sc) {
        Entity questowner = new Entity("Questgeber");
        questowner.add(new PositionComponent());
        try {
            questowner.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new NewDSLTaskComponent(sc, questowner);

        questowner.add(askOnInteractionQuiz(sc));

        Set<Set<Entity>> returnSet = new HashSet<>();
        Set<Entity> roomSet = new HashSet<>();

        roomSet.add(questowner);

        returnSet.add(roomSet);
        returnSet.add(fillerSet());
        return returnSet;
    }

    public static Set<Set<Entity>> buildMultipleChoiceTask(MultipleChoiceTask mc) {
        Entity questowner = new Entity("Questgeber");
        questowner.add(new PositionComponent());
        try {
            questowner.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new NewDSLTaskComponent(mc, questowner);

        questowner.add(askOnInteractionQuiz(mc));

        Set<Set<Entity>> returnSet = new HashSet<>();
        Set<Entity> roomSet = new HashSet<>();

        roomSet.add(questowner);

        returnSet.add(roomSet);
        returnSet.add(fillerSet());
        return returnSet;
    }

    private static InteractionComponent askOnInteractionQuiz(newdsl.tasks.Task<?> task) {
        return new InteractionComponent(
            1,
            true,
            (thisEntity, otherEntity) -> {
                if (task.getState().equals(TaskState.ACTIVE)
                    || task.getState().equals(TaskState.PROCESSING_ACTIVE)
                    || task.getState().equals(TaskState.INACTIVE)) {
                    switch (task.getType()) {
                        case SINGLE_CHOICE -> {
                            NewDSLQuizUI.askSingleChoiceQuizOnHud((SingleChoiceTask) task);
                        }
                        case MULTIPLE_CHOICE -> {
                            NewDSLQuizUI.askMultipleChoiceQuizOnHud((MultipleChoiceTask) task);
                        }
                    }
                } else {
                    OkDialog.showOkDialog("Du hast die Aufgabe schon bearbeitet.", "Info", () -> {
                    });
                }
            });
    }

    private static InteractionComponent askOnInteractionYesNo(Task task) {
        return new InteractionComponent(
            1,
            true,
            (thisEntity, otherEntity) -> {
                if (task.getState().equals(TaskState.ACTIVE)
                    || task.getState().equals(TaskState.PROCESSING_ACTIVE)) {
                    NewDSLYesNoDialog.showYesNoDialog(task);
                } else {
                    OkDialog.showOkDialog("Du hast die Aufgabe schon bearbeitet.", "Info", () -> {
                    });
                }
            });
    }

    private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            float score = task.gradeTask(taskContents);
            TextDialog.textDialog("Your score: " + score, "Ok", "Given answer");
        };
    }

    private static Set<Entity> fillerSet() {
        try {
            return Set.of(
                EntityFactory.newChest(),
                EntityFactory.randomMonster(),
                EntityFactory.randomMonster(),
                EntityFactory.randomMonster());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
