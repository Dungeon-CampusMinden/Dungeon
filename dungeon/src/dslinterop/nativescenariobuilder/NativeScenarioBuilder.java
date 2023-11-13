package dslinterop.nativescenariobuilder;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.UITools;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;

import task.Task;
import task.TaskContent;
import task.components.TaskComponent;
import task.tasktype.AssignTask;
import task.tasktype.Quiz;
import task.utils.hud.UIAnswerCallback;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class NativeScenarioBuilder {
    public static Set<Set<Entity>> quizOnHud(Quiz quiz) {
        Entity questowner = new Entity("Questowner");
        questowner.addComponent(new PositionComponent());
        try {
            questowner.addComponent(new DrawComponent("character/knight"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        questowner.addComponent(askOnInteraction(quiz));
        new TaskComponent(quiz, questowner);

        Set<Set<Entity>> returnSet = new HashSet<>();
        Set<Entity> roomSet = new HashSet<>();

        roomSet.add(questowner);

        returnSet.add(roomSet);
        returnSet.add(fillerSet());
        returnSet.add(fillerSet());
        returnSet.add(fillerSet());
        return returnSet;
    }

    private HashSet<HashSet<Entity>> assignTaskA(AssignTask task) {
        return null;
    }

    private static InteractionComponent askOnInteraction(Quiz quiz) {
        return new InteractionComponent(
            1, true, UIAnswerCallback.askOnInteraction(quiz, showAnswersOnHud()));
    }

    private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            float score = task.gradeTask(taskContents);
            task.managerEntity().get().removeComponent(InteractionComponent.class);
            UITools.generateNewTextDialog("Your score: " + score, "Ok", "Given answer");
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
