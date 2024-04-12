package dojo.rooms.level_1;

import contrib.components.InteractionComponent;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import task.Task;
import task.TaskContent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum muss der Spieler alle Fragen eines NPCs zu Lambda-Ausdrücken und
 * Funktionsinterfaces richtig beantworten, um in den nächsten Raum zu gelangen.
 */
public class L1_R3_Fragen_Lambda extends Room {
  private record QuestionAndAnswers(Quiz question, Set<Quiz.Content> correctAnswers) {}

  private static QuestionAndAnswers question;

  // to toggle between question types
  private static int toggle = -1;

  public L1_R3_Fragen_Lambda(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);

    try {
      generate();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to generate: " + getClass().getName() + ": " + e.getMessage(), e);
    }
  }

  public void generate() throws IOException {
    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    // add boss
    roomEntities.add(npcQuestioner());

    addRoomEntities(roomEntities);
  }

  private Entity npcQuestioner() throws IOException {
    Entity questioner = new Entity("Questioner");
    questioner.add(new PositionComponent());
    questioner.add(new DrawComponent(new SimpleIPath("character/monster/pumpkin_dude")));

    toggleQuiz();
    questioner.add(
        new InteractionComponent(
            1,
            true,
            (e, who) ->
                UIAnswerCallback.askOnInteraction(question.question(), showAnswersOnHud())
                    .accept(e, null)));

    return questioner;
  }

  private BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
    return (task, taskContents) -> {
      AtomicReference<String> answers = new AtomicReference<>("");
      taskContents.stream()
          .map(t -> (Quiz.Content) t)
          .forEach(t -> answers.set(answers.get() + t.content() + System.lineSeparator()));

      // Check if the player's answers are correct
      if (checkAnswers(question.correctAnswers(), taskContents)) {
        OkDialog.showOkDialog("Ihre Antwort ist korrekt!", "JAAA", this::openDoors);
      } else {
        OkDialog.showOkDialog("Ihre Antwort ist nicht korrekt!", "NEIIN", () -> {});
      }
      toggleQuiz();
    };
  }

  private void toggleQuiz() {
    toggle = (toggle + 1) % 3;
    switch (toggle) {
      case 0 -> {
        question = singleChoice(); // Index 3 ist die richtige Antwort
      }
      case 1 -> {
        question = multipleChoice(); // Index 0, 1, 2 sind die richtigen Antworten
      }
      case 2 -> {
        question = multipleChoice2(); // Index 0, 1, 2, 4 sind die richtigen Antworten
      }
    }
  }

  private QuestionAndAnswers singleChoice() {
    SingleChoice question = new SingleChoice("Was ist ein Lambda-Ausdruck in Java?");
    Quiz.Content c1 = new Quiz.Content("Eine Methode, die ohne einen Namen definiert wird");
    Quiz.Content c2 = new Quiz.Content("Eine anonyme Klasse");
    Quiz.Content c3 = new Quiz.Content("Ein spezieller Datentyp");
    Quiz.Content c4 = new Quiz.Content("Ein Interface mit nur einer Methode");
    question.addAnswer(c1);
    question.addAnswer(c2);
    question.addAnswer(c3);
    question.addAnswer(c4);
    return new QuestionAndAnswers(question, Set.of(c4));
  }

  private QuestionAndAnswers multipleChoice() {
    Quiz question = new MultipleChoice("Welche Methoden definiert das Interface Predicate?");
    // TODO: quiz content must be not longer than 40 characters ...
    Quiz.Content c1 = new Quiz.Content("apply");
    Quiz.Content c2 = new Quiz.Content("accept");
    Quiz.Content c3 = new Quiz.Content("test");
    Quiz.Content c4 = new Quiz.Content("get");
    Quiz.Content c5 = new Quiz.Content("set");
    question.addAnswer(c1);
    question.addAnswer(c2);
    question.addAnswer(c3);
    question.addAnswer(c4);
    question.addAnswer(c5);
    return new QuestionAndAnswers(question, Set.of(c3));
  }

  private QuestionAndAnswers multipleChoice2() {
    Quiz question =
        new MultipleChoice("Welche der folgenden Interfaces sind funktionale Interfaces in Java?");
    Quiz.Content c1 = new Quiz.Content("Runnable");
    Quiz.Content c2 = new Quiz.Content("Callable");
    Quiz.Content c3 = new Quiz.Content("Comparator");
    Quiz.Content c4 = new Quiz.Content("EventListener");
    Quiz.Content c5 = new Quiz.Content("ActionListener");
    question.addAnswer(c1);
    question.addAnswer(c2);
    question.addAnswer(c3);
    question.addAnswer(c4);
    question.addAnswer(c5);
    return new QuestionAndAnswers(question, Set.of(c1, c2, c3, c5));
  }

  private boolean checkAnswers(Set<Quiz.Content> correctAnswers, Set<TaskContent> playerAnswers) {
    Set<String> s1 =
        correctAnswers.stream().map(Quiz.Content::toString).collect(Collectors.toSet());
    Set<String> s2 = playerAnswers.stream().map(Object::toString).collect(Collectors.toSet());
    return s1.containsAll(s2) && s2.containsAll(s1);
  }
}
