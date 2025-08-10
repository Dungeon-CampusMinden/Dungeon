package reporting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import contrib.components.InventoryComponent;
import contrib.entities.HeroFactory;
import contrib.item.Item;
import core.Entity;
import core.Game;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import task.Task;
import task.TaskContent;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;
import task.reporting.AnswerPickingFunctions;
import task.reporting.GradingFunctions;
import task.tasktype.AssignTask;
import task.tasktype.Element;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;

/** WTF? . */
public class AnswerPickingFunctionsTest {

  /** Cleanup function to be executed after each test case. */
  @AfterEach
  public void cleanup() {
    Task.cleanupAllTask();
    Game.removeAllEntities();
  }

  /** WTF? . */
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    assertEquals(2, callback.apply(sc).size());
  }

  /**
   * Generate a test scenario where a single chest is picked with two quest items and one regular
   * item.
   */
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    ic.add(Mockito.mock(Item.class));
    assertEquals(2, callback.apply(sc).size());
  }

  /** A test case for the single chest picker function with zero quest items. */
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();

    assertEquals(0, callback.apply(sc).size());
  }

  /** Generate a test scenario with zero quest items and two items in a single chest. */
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
    ic.add(Mockito.mock(Item.class));
    ic.add(Mockito.mock(Item.class));
    assertEquals(0, callback.apply(sc).size());
  }

  /** WTF? . */
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
    assertEquals(0, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void singlechestpicker_otherQuestsItem() {
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
    chest.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, chest);
    sc.addContainer(containerTaskContent);
    chest.add(new TaskContentComponent(containerTaskContent));
    Game.add(chest);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    // second question
    SingleChoice sc2 = new SingleChoice("Dummy 2");
    Quiz.Content answerA2 = new Quiz.Content("A2");
    Quiz.Content answerB2 = new Quiz.Content("B2");
    sc2.addAnswer(answerA2);
    sc2.addAnswer(answerB2);
    // setup quest items second question
    TaskContentComponent answerA2Component = new TaskContentComponent(answerA2);
    TaskContentComponent answerB2Component = new TaskContentComponent(answerB2);
    QuestItem answerA2Item = new QuestItem(null, answerA2Component);
    QuestItem answerB2Item = new QuestItem(null, answerB2Component);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.singleChestPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    ic.add(answerA2Item);
    ic.add(answerB2Item);
    assertEquals(2, callback.apply(sc).size());
  }

  /** WTF? . */
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
    chestA.add(icA);
    chestA.add(new TaskContentComponent(containerA));
    Game.add(chestA);

    Entity chestB = new Entity("Chest B");
    InventoryComponent icB = new InventoryComponent(3);
    chestB.add(icB);
    chestB.add(new TaskContentComponent(containerB));
    Game.add(chestB);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
    TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);
    QuestItem answerCItem = new QuestItem(null, answerCComponent);
    QuestItem answerDItem = new QuestItem(null, answerDComponent);

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

  /** Generate a test for the multiplechestpicker_emtpy function. */
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
    chestA.add(icA);
    chestA.add(new TaskContentComponent(containerA));
    Game.add(chestA);

    Entity chestB = new Entity("Chest B");
    InventoryComponent icB = new InventoryComponent(3);
    chestB.add(icB);
    chestB.add(new TaskContentComponent(containerB));
    Game.add(chestB);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
    TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);
    QuestItem answerCItem = new QuestItem(null, answerCComponent);
    QuestItem answerDItem = new QuestItem(null, answerDComponent);

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

  /** Test case for the multipleChestPicker() method. */
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
    chestA.add(icA);
    chestA.add(new TaskContentComponent(containerA));
    Game.add(chestA);

    Entity chestB = new Entity("Chest B");
    InventoryComponent icB = new InventoryComponent(3);
    chestB.add(icB);
    chestB.add(new TaskContentComponent(containerB));
    Game.add(chestB);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
    TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);
    QuestItem answerCItem = new QuestItem(null, answerCComponent);
    QuestItem answerDItem = new QuestItem(null, answerDComponent);

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

  /** WTF? . */
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
    chestA.add(icA);
    chestA.add(new TaskContentComponent(containerA));
    Game.add(chestA);

    Entity chestB = new Entity("Chest B");
    InventoryComponent icB = new InventoryComponent(3);
    chestB.add(icB);
    chestB.add(new TaskContentComponent(containerB));
    Game.add(chestB);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
    TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);
    QuestItem answerCItem = new QuestItem(null, answerCComponent);
    QuestItem answerDItem = new QuestItem(null, answerDComponent);

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

  /**
   * Setup the question, chests, and quest items for the multiple chest picker other quests item
   * test.
   */
  @Test
  public void multiplechestpicker_otherQuestsItem() {
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
    chestA.add(icA);
    chestA.add(new TaskContentComponent(containerA));
    Game.add(chestA);

    Entity chestB = new Entity("Chest B");
    InventoryComponent icB = new InventoryComponent(3);
    chestB.add(icB);
    chestB.add(new TaskContentComponent(containerB));
    Game.add(chestB);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    TaskContentComponent answerCComponent = new TaskContentComponent(answerC);
    TaskContentComponent answerDComponent = new TaskContentComponent(answerD);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);
    QuestItem answerCItem = new QuestItem(null, answerCComponent);
    QuestItem answerDItem = new QuestItem(null, answerDComponent);

    // second question
    SingleChoice sc2 = new SingleChoice("Dummy 2");
    Quiz.Content answerA2 = new Quiz.Content("A2");
    Quiz.Content answerB2 = new Quiz.Content("B2");
    sc2.addAnswer(answerA2);
    sc2.addAnswer(answerB2);
    // setup quest items second question
    TaskContentComponent answerA2Component = new TaskContentComponent(answerA2);
    TaskContentComponent answerB2Component = new TaskContentComponent(answerB2);
    QuestItem answerA2Item = new QuestItem(null, answerA2Component);
    QuestItem answerB2Item = new QuestItem(null, answerB2Component);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.multipleChestPicker();
    // add answer to chest
    icA.add(answerAItem);
    icA.add(answerBItem);
    icB.add(answerCItem);
    icB.add(answerDItem);
    icA.add(answerA2Item);
    icB.add(answerB2Item);

    Set<TaskContent> answer = callback.apply(ag);
    // wrapper
    assertEquals(1, answer.size());
    Element wrap = (Element) answer.stream().findFirst().get();
    Map<Element, Set<Element>> givenSol = (Map<Element, Set<Element>>) wrap.content();
    assertEquals(givenSol, sol);
  }

  /** WTF? . */
  public void heroinventorypicker_twoQuestItems() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    assertEquals(2, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void heroinventorypicker_twoQuestItemsOneItem() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    Game.add(hero);
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    ic.add(Mockito.mock(Item.class));
    assertEquals(2, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void heroinventorypicker_zeroQuestItems() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();

    assertEquals(0, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void heroinventorypicker_zeroQuestItemsTwoItems() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();
    ic.add(Mockito.mock(Item.class));
    ic.add(Mockito.mock(Item.class));
    assertEquals(0, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void heroinventorypicker_empty() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();
    assertEquals(0, callback.apply(sc).size());
  }

  /** WTF? . */
  @Test
  public void heroinventorypicker_otherQuestsItem() throws IOException {
    // setup question
    SingleChoice sc = new SingleChoice("Dummy");
    Quiz.Content answerA = new Quiz.Content("A");
    Quiz.Content answerB = new Quiz.Content("B");
    sc.addAnswer(answerA);
    sc.addAnswer(answerB);
    sc.addCorrectAnswerIndex(1);
    sc.scoringFunction(GradingFunctions.singleChoiceGrading());
    sc.answerPickingFunction(AnswerPickingFunctions.heroInventoryPicker());

    // setup hero
    Entity hero = HeroFactory.newHero();
    InventoryComponent ic = new InventoryComponent(3);
    hero.add(ic);
    TaskContent containerTaskContent = new Element<>(sc, hero);
    sc.addContainer(containerTaskContent);
    hero.add(new TaskContentComponent(containerTaskContent));
    Game.add(hero);

    // setup quest items
    TaskContentComponent answerAComponent = new TaskContentComponent(answerA);
    TaskContentComponent answerBComponent = new TaskContentComponent(answerB);
    QuestItem answerAItem = new QuestItem(null, answerAComponent);
    QuestItem answerBItem = new QuestItem(null, answerBComponent);

    // second question
    SingleChoice sc2 = new SingleChoice("Dummy 2");
    Quiz.Content answerA2 = new Quiz.Content("A2");
    Quiz.Content answerB2 = new Quiz.Content("B2");
    sc2.addAnswer(answerA2);
    sc2.addAnswer(answerB2);
    // setup quest items second question
    TaskContentComponent answerA2Component = new TaskContentComponent(answerA2);
    TaskContentComponent answerB2Component = new TaskContentComponent(answerB2);
    QuestItem answerA2Item = new QuestItem(null, answerA2Component);
    QuestItem answerB2Item = new QuestItem(null, answerB2Component);
    Function<Task, Set<TaskContent>> callback = AnswerPickingFunctions.heroInventoryPicker();
    // add answer to chest
    ic.add(answerAItem);
    ic.add(answerBItem);
    ic.add(answerA2Item);
    ic.add(answerB2Item);
    assertEquals(2, callback.apply(sc).size());
  }
}
