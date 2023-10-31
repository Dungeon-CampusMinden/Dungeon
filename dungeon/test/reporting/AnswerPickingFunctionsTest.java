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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        TaskContent containerTaskContent = new Element<>(sc, chest);
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
        TaskContent containerTaskContent = new Element<>(sc, chest);
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
        TaskContent containerTaskContent = new Element<>(sc, chest);
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
        TaskContent containerTaskContent = new Element<>(sc, chest);
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
        TaskContent containerTaskContent = new Element<>(sc, chest);
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
    public void multiplechestpicker_questItemAnswers() {
        // setup question
        AssignTask ag = new AssignTask();

        Element answerA = new Element(ag, "A");
        Element answerB = new Element(ag, "B");
        Element answerC = new Element(ag, "C");
        Element answerD = new Element(ag, "D");

        HashSet<Element> containerASet = new HashSet<>();
        Element containerA = new Element(ag, containerASet);
        containerASet.add(answerA);
        containerASet.add(answerB);
        ag.addContainer(containerA);

        HashSet<Element> containerBSet = new HashSet<>();
        Element containerB = new Element(ag, containerBSet);
        containerBSet.add(answerC);
        containerBSet.add(answerD);
        ag.addContainer(containerB);

        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(containerA, (Set<Element>) containerA.content());
        sol.put(containerB, (Set<Element>) containerB.content());
        ag.solution(sol);

        // setup Chests
        Entity chestA = new Entity("Chest A");
        InventoryComponent icA = new InventoryComponent(3);
        chestA.addComponent(icA);
        chestA.addComponent(new TaskContentComponent(containerA));
        Game.add(chestA);

        Entity chestB = new Entity("Chest B");
        InventoryComponent icB = new InventoryComponent(3);
        chestB.addComponent(icB);
        chestB.addComponent(new TaskContentComponent(containerB));
        Game.add(chestB);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
        TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);
        QuestItem answerCItem = new QuestItem(null, null, null, answerCComponent);
        QuestItem answerDItem = new QuestItem(null, null, null, answerDComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.multipleChestPicker();
        // add answer to chest
        icA.add(answerAItem);
        icA.add(answerBItem);
        icB.add(answerCItem);
        icB.add(answerDItem);

        Set<TaskContent> answer = callback.apply(ag);
        // wrapper
        assertEquals(1, answer.size());
        Element wrap = (Element) answer.stream().findFirst().get();
        Map<Element, Set<Element>> givenSol = (Map<Element, Set<Element>>) wrap.content();
        assertEquals(givenSol, sol);
    }

    @Test
    public void multiplechestpicker_emtpy() {
        // setup question
        AssignTask ag = new AssignTask();

        Element answerA = new Element(ag, "A");
        Element answerB = new Element(ag, "B");
        Element answerC = new Element(ag, "C");
        Element answerD = new Element(ag, "D");

        HashSet<Element> containerASet = new HashSet<>();
        Element containerA = new Element(ag, containerASet);
        containerASet.add(answerA);
        containerASet.add(answerB);
        ag.addContainer(containerA);

        HashSet<Element> containerBSet = new HashSet<>();
        Element containerB = new Element(ag, containerBSet);
        containerBSet.add(answerC);
        containerBSet.add(answerD);
        ag.addContainer(containerB);

        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(containerA, (Set<Element>) containerA.content());
        sol.put(containerB, (Set<Element>) containerB.content());
        ag.solution(sol);

        // setup Chests
        Entity chestA = new Entity("Chest A");
        InventoryComponent icA = new InventoryComponent(3);
        chestA.addComponent(icA);
        chestA.addComponent(new TaskContentComponent(containerA));
        Game.add(chestA);

        Entity chestB = new Entity("Chest B");
        InventoryComponent icB = new InventoryComponent(3);
        chestB.addComponent(icB);
        chestB.addComponent(new TaskContentComponent(containerB));
        Game.add(chestB);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
        TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);
        QuestItem answerCItem = new QuestItem(null, null, null, answerCComponent);
        QuestItem answerDItem = new QuestItem(null, null, null, answerDComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.multipleChestPicker();

        Set<TaskContent> answer = callback.apply(ag);
        // wrapper
        assertEquals(1, answer.size());
        Element wrap = (Element) answer.stream().findFirst().get();
        Map<Element, Set<Element>> givenSol = (Map<Element, Set<Element>>) wrap.content();
        Map<Element, Set<Element>> expectedSol = new HashMap<>();
        expectedSol.put(containerA, new HashSet<>());
        expectedSol.put(containerB, new HashSet<>());
        assertEquals(expectedSol, givenSol);
    }

    @Test
    public void multiplechestpicker_normalItems() {
        // setup question
        AssignTask ag = new AssignTask();

        Element answerA = new Element(ag, "A");
        Element answerB = new Element(ag, "B");
        Element answerC = new Element(ag, "C");
        Element answerD = new Element(ag, "D");

        HashSet<Element> containerASet = new HashSet<>();
        Element containerA = new Element(ag, containerASet);
        containerASet.add(answerA);
        containerASet.add(answerB);
        ag.addContainer(containerA);

        HashSet<Element> containerBSet = new HashSet<>();
        Element containerB = new Element(ag, containerBSet);
        containerBSet.add(answerC);
        containerBSet.add(answerD);
        ag.addContainer(containerB);

        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(containerA, (Set<Element>) containerA.content());
        sol.put(containerB, (Set<Element>) containerB.content());
        ag.solution(sol);

        // setup Chests
        Entity chestA = new Entity("Chest A");
        InventoryComponent icA = new InventoryComponent(3);
        chestA.addComponent(icA);
        chestA.addComponent(new TaskContentComponent(containerA));
        Game.add(chestA);

        Entity chestB = new Entity("Chest B");
        InventoryComponent icB = new InventoryComponent(3);
        chestB.addComponent(icB);
        chestB.addComponent(new TaskContentComponent(containerB));
        Game.add(chestB);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
        TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);
        QuestItem answerCItem = new QuestItem(null, null, null, answerCComponent);
        QuestItem answerDItem = new QuestItem(null, null, null, answerDComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.multipleChestPicker();

        // add items
        icA.add(Mockito.mock(Item.class));
        icA.add(Mockito.mock(Item.class));
        icB.add(Mockito.mock(Item.class));
        icB.add(Mockito.mock(Item.class));
        Set<TaskContent> answer = callback.apply(ag);
        // wrapper
        assertEquals(1, answer.size());
        Element wrap = (Element) answer.stream().findFirst().get();
        Map<Element, Set<Element>> givenSol = (Map<Element, Set<Element>>) wrap.content();
        Map<Element, Set<Element>> expectedSol = new HashMap<>();
        expectedSol.put(containerA, new HashSet<>());
        expectedSol.put(containerB, new HashSet<>());
        assertEquals(expectedSol, givenSol);
    }

    @Test
    public void multiplechestpicker_QuestItemAndNormalItemMix() {
        // setup question
        AssignTask ag = new AssignTask();

        Element answerA = new Element(ag, "A");
        Element answerB = new Element(ag, "B");
        Element answerC = new Element(ag, "C");
        Element answerD = new Element(ag, "D");

        HashSet<Element> containerASet = new HashSet<>();
        Element containerA = new Element(ag, containerASet);
        containerASet.add(answerA);
        containerASet.add(answerB);
        ag.addContainer(containerA);

        HashSet<Element> containerBSet = new HashSet<>();
        Element containerB = new Element(ag, containerBSet);
        containerBSet.add(answerC);
        containerBSet.add(answerD);
        ag.addContainer(containerB);

        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(containerA, (Set<Element>) containerA.content());
        sol.put(containerB, (Set<Element>) containerB.content());
        ag.solution(sol);

        // setup Chests
        Entity chestA = new Entity("Chest A");
        InventoryComponent icA = new InventoryComponent(3);
        chestA.addComponent(icA);
        chestA.addComponent(new TaskContentComponent(containerA));
        Game.add(chestA);

        Entity chestB = new Entity("Chest B");
        InventoryComponent icB = new InventoryComponent(3);
        chestB.addComponent(icB);
        chestB.addComponent(new TaskContentComponent(containerB));
        Game.add(chestB);

        // setup quest items
        TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
        TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
        TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
        TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
        QuestItem answerAItem = new QuestItem(null, null, null, answerAComponent);
        QuestItem answerBItem = new QuestItem(null, null, null, answerBComponent);
        QuestItem answerCItem = new QuestItem(null, null, null, answerCComponent);
        QuestItem answerDItem = new QuestItem(null, null, null, answerDComponent);

        Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.multipleChestPicker();
        // add answer to chest
        icA.add(answerAItem);
        icA.add(answerBItem);
        icA.add(Mockito.mock(Item.class));
        icB.add(answerCItem);
        icB.add(answerDItem);
        icB.add(Mockito.mock(Item.class));
        Set<TaskContent> answer = callback.apply(ag);
        // wrapper
        assertEquals(1, answer.size());
        Element wrap = (Element) answer.stream().findFirst().get();
        Map<Element, Set<Element>> givenSol = (Map<Element, Set<Element>>) wrap.content();
        Map<Element, Set<Element>> expectedSol = new HashMap<>();
        assertEquals(sol, givenSol);
    }
}
