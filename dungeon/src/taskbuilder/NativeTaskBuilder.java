package taskbuilder;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.UITools;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;

import task.AssignTask;
import task.Quiz;
import task.Task;
import task.TaskContent;
import task.components.TaskComponent;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;
import task.quizquestion.UIAnswerCallback;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A {@link ITaskBuilder} written native in Java. Can be used as an alternativ to the {@link
 * interpreter.DSLInterpreter}.
 */
public class NativeTaskBuilder implements ITaskBuilder {
    @Override
    public Optional<Object> buildTask(Task task) {
        if (task instanceof SingleChoice) return Optional.ofNullable(quizOnHud((Quiz) task));
        else if (task instanceof MultipleChoice) return Optional.ofNullable(quizOnHud((Quiz) task));
        else if (task instanceof AssignTask)
            return Optional.ofNullable(assignTaskA((AssignTask) task));
        // HashSet<HashSet<core.Entity>>.
        return Optional.empty();
    }

    private Set<Set<Entity>> quizOnHud(Quiz quiz) {
        Entity questowner = new Entity("Questowner");
        questowner.addComponent(new PositionComponent());
        try {
            questowner.addComponent(new DrawComponent("character/knight"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        questowner.addComponent(askOnInteraction(quiz));
        new TaskComponent(quiz, questowner);

        Set<Set<Entity>> outerSet = new HashSet<>();
        Set<Entity> innerSet = new HashSet<>();
        innerSet.add(questowner);
        outerSet.add(innerSet);
        outerSet.add(fillerSet());
        outerSet.add(fillerSet());
        outerSet.add(fillerSet());
        return outerSet;
    }

    private HashSet<HashSet<Entity>> assignTaskA(AssignTask task) {
        return null;
    }

    private InteractionComponent askOnInteraction(Quiz quiz) {
        return new InteractionComponent(
                1, true, UIAnswerCallback.askOnInteraction(quiz, showAnswersOnHud()));
    }

    private BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            float score = task.gradeTask(taskContents);
            task.managerEntity().get().removeComponent(InteractionComponent.class);
            UITools.generateNewTextDialog("Your score: " + score, "Ok", "Given answer");
        };
    }

    private Set<Entity> fillerSet() {
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
