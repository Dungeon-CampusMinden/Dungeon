package reporting;

import static org.junit.Assert.assertEquals;

import contrib.components.InventoryComponent;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Test;

import task.*;
import task.components.TaskContentComponent;
import task.quizquestion.SingleChoice;

import java.util.Set;
import java.util.function.Function;

public class AnswerPickingFunctionsTest {

    @After
    public void cleanup() {
        Task.cleanupAllTask();
        Game.removeAllEntities();
    }

    @Test
    public void singlechestpicker() {
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // Chest
        Entity chest = new Entity("Chest");
        chest.addComponent(new InventoryComponent(3));
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));

        // QuestItems
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
        assertEquals(0, callback.apply(sc).size());
    }
}