package reporting;

import static org.junit.Assert.assertEquals;

import contrib.components.InventoryComponent;
import contrib.item.Item;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

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
    public void singlechestpicker_twoQuestItems() {
        // setup question
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // setup Chest
        Entity chest = new Entity("Chest");
        InventoryComponent ic = new InventoryComponent(3);
        chest.addComponent(ic);
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));
        Game.add(chest);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
        // add answer to chest
        ic.add(answerAItem);
        ic.add(answerBItem);
        assertEquals(2, callback.apply(sc).size());
    }

    @Test
    public void singlechestpicker_twoQuestItemsOneItem() {
        // setup question
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // setup Chest
        Entity chest = new Entity("Chest");
        InventoryComponent ic = new InventoryComponent(3);
        chest.addComponent(ic);
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));
        Game.add(chest);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
        // add answer to chest
        ic.add(answerAItem);
        ic.add(answerBItem);
        ic.add(Mockito.mock(Item.class));
        assertEquals(2, callback.apply(sc).size());
    }

    @Test
    public void singlechestpicker_zeroQuestItems() {
        // setup question
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // setup Chest
        Entity chest = new Entity("Chest");
        InventoryComponent ic = new InventoryComponent(3);
        chest.addComponent(ic);
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));
        Game.add(chest);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();

        assertEquals(0, callback.apply(sc).size());
    }

    @Test
    public void singlechestpicker_zeroQuestItemsTwoItems() {
        // setup question
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // setup Chest
        Entity chest = new Entity("Chest");
        InventoryComponent ic = new InventoryComponent(3);
        chest.addComponent(ic);
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));
        Game.add(chest);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
        ic.add(Mockito.mock(Item.class));
        ic.add(Mockito.mock(Item.class));
        assertEquals(0, callback.apply(sc).size());
    }

    @Test
    public void singlechestpicker_empty() {
        // setup question
        SingleChoice sc = new SingleChoice("Dummy");
        Quiz.Content answerA = new Quiz.Content("A");
        Quiz.Content answerB = new Quiz.Content("B");
        sc.addAnswer(answerA);
        sc.addAnswer(answerB);
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.answerPickingFunction(AnswerPickingFunctions.singleChestPicker());

        // setup Chest
        Entity chest = new Entity("Chest");
        InventoryComponent ic = new InventoryComponent(3);
        chest.addComponent(ic);
        TaskContent containerTaskContent = new Element<>(sc, "Chest");
        sc.addContainer(containerTaskContent);
        chest.addComponent(new TaskContentComponent(containerTaskContent));
        Game.add(chest);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
        assertEquals(0, callback.apply(sc).size());
    }
}
