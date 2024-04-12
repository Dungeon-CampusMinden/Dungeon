package dojo.rooms.level_4;

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
import task.Task;
import task.TaskContent;
import task.game.components.TaskComponent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum müssen verschiedene Design Patterns anhand eines UML-Klassendiagramms erkannt
 * werden. Die erkannten Design Patterns müssen dann dem Zauberer mitgeteilt werden.
 */
public class L4_R2_Fragen_Pattern extends Room {
  private final String[] expectedPatterns = {"Observer", "Visitor"};
  private int currentPatternIndex = 0;

  private Entity zauberer;
  private int correctAnswerCount = 0;

  public L4_R2_Fragen_Pattern(
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
    roomEntities.add(questBoss());

    addRoomEntities(roomEntities);
  }

  private Entity questBoss() throws IOException {
    // add boss
    zauberer = new Entity("Zauberer von Patternson");
    zauberer.add(new PositionComponent());
    zauberer.add(new DrawComponent(new SimpleIPath("character/wizard")));

    setNextTask();

    return zauberer;
  }

  private void setNextTask() {
    final Quiz question = newFreeText();

    zauberer.add(new TaskComponent(question, zauberer));
    zauberer.add(
        new InteractionComponent(
            1,
            true,
            (e, who) -> {
              UIAnswerCallback.askOnInteraction(question, showAnswersOnHud()).accept(e, who);
            }));
  }

  private BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
    return (task, taskContents) -> {
      AtomicReference<String> answers = new AtomicReference<>("");
      taskContents.stream()
          .map(t -> (Quiz.Content) t)
          .forEach(t -> answers.set(answers.get() + t.content() + System.lineSeparator()));

      // remove the automatically added \n from the answer string
      String answer = answers.get();
      String cleanedAnswer = answer.trim();

      if (cleanedAnswer.equals(expectedPatterns[currentPatternIndex])) {
        OkDialog.showOkDialog("Ihre Antwort ist korrekt!", "Antwort", () -> {});
        correctAnswerCount++;
        if (correctAnswerCount >= 2) {
          openDoors();
        }
        currentPatternIndex++;
        if (currentPatternIndex < expectedPatterns.length) {
          setNextTask();
        }
      } else {
        OkDialog.showOkDialog("Ihre Antwort ist nicht korrekt!", "Ok", () -> {});
      }
    };
  }

  private Quiz newFreeText() {
    String questionText =
        "Welches Design-Pattern wird in dem UML-Klassendiagramm unter \"dojo-dungeon/todo-assets/lvl4r2/UML_Klassendiagramm"
            + (currentPatternIndex + 1)
            + ".png\" dargestellt? Es reicht das Wort ohne den Zusatz Pattern!";
    return new FreeText(questionText);
  }
}
