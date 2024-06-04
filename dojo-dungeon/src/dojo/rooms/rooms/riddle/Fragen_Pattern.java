package dojo.rooms.rooms.riddle;

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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
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
 * werden. Die erkannten Design Patterns müssen dann dem Schamanen mitgeteilt werden.
 */
public class Fragen_Pattern extends Room {
  private final String FILE_NAME_PREFIX =
      "dojo-dungeon/todo-assets/Fragen_Pattern/UML_Klassendiagramm";
  private final String[] EXPECTED_PATTERNS = {
    ".*?none.*?",
    ".*?observer.*?",
    ".*?visitor.*?",
    ".*?(composite)|(kompositum)|(none).*?",
    ".*?(adapter)|(none).*?",
    ".*?(singleton)|(none).*?",
    ".*?observer.*?",
    ".*?visitor.*?",
    ".*?(strategy)|(strategie)|(none).*?",
  };
  private final int MIN_NUMBER_OF_CORRECT_ANSWERS = 1;
  private final int MAX_NUMBER_OF_WRONG_ANSWERS = 2;
  private List<Integer> patternIndices =
      new ArrayList<>(IntStream.range(0, EXPECTED_PATTERNS.length).boxed().toList());

  {
    Collections.shuffle(patternIndices);
  }

  private int currentPatternIndicesIndex = 0;
  private int currentPatternIndex = 0;
  private int correctAnswerCount = 0;
  private Entity zauberer;

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public Fragen_Pattern(
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

  private void generate() throws IOException {
    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    // add boss
    roomEntities.add(questBoss());

    addRoomEntities(roomEntities);
  }

  private Entity questBoss() throws IOException {
    // add boss
    zauberer = new Entity("Schamane der Patterns");
    zauberer.add(new PositionComponent());
    zauberer.add(new DrawComponent(new SimpleIPath("character/monster/orc_shaman")));

    setNextTask();

    return zauberer;
  }

  private void setNextTask() {
    Quiz question = newFreeText();

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
    String title = "Antworten";
    String[] texts = {
      "Ihre Antwort ist korrekt!",
      "Die Tür wird geöffnet, aber Sie können auch noch weiter Fragen beantworten.",
      "Die Tür wird geöffnet. Bitte weitergehen, alle Fragen wurden bereits gestellt.",
    };

    return (task, taskContents) -> {
      String rawAnswer =
          taskContents.stream().map(t -> (Quiz.Content) t).findFirst().orElseThrow().content();
      String answer = rawAnswer.toLowerCase();

      if (answer.matches(EXPECTED_PATTERNS[currentPatternIndex])) {
        // Correct answer
        correctAnswerCount++;

        if (correctAnswerCount == MIN_NUMBER_OF_CORRECT_ANSWERS) {
          openDoors();
          if (hasNextPattern()) {
            setNextTask();
            OkDialog.showOkDialog(
                texts[0], title, () -> OkDialog.showOkDialog(texts[1], title, () -> {}));
          } else {
            OkDialog.showOkDialog(
                texts[0], title, () -> OkDialog.showOkDialog(texts[2], title, () -> {}));
          }
        } else {
          if (hasNextPattern()) {
            setNextTask();
            OkDialog.showOkDialog(texts[0], title, () -> {});
          } else {
            // Should not happen: no more questions but door isn't opened yet
            OkDialog.showOkDialog(
                texts[0], title, () -> OkDialog.showOkDialog(texts[2], title, () -> {}));
            openDoors();
          }
        }
      } else {
        // Wrong answer
        if (hasNextPattern()) {
          setNextTask();
          decreaseHerosHealthAtWrongTry(MAX_NUMBER_OF_WRONG_ANSWERS, () -> {});
        } else {
          // Should not happen: no more questions but door isn't opened yet
          decreaseHerosHealthAtWrongTry(
              MAX_NUMBER_OF_WRONG_ANSWERS, () -> OkDialog.showOkDialog(texts[2], title, () -> {}));
          openDoors();
        }
      }
    };
  }

  private Quiz newFreeText() {
    nextPattern();
    String questionText =
        "Welches Design-Pattern wird in dem UML-Klassendiagramm unter \""
            + FILE_NAME_PREFIX
            + currentPatternIndex
            + ".png\" dargestellt? Es reicht das Wort ohne den Zusatz Pattern! Wenn Sie kein Pattern erkennen, geben Sie bitte \"none\" ein.";
    return new FreeText(questionText);
  }

  private boolean hasNextPattern() {
    return currentPatternIndicesIndex < EXPECTED_PATTERNS.length;
  }

  private void nextPattern() {
    if (hasNextPattern()) {
      currentPatternIndex = patternIndices.get(currentPatternIndicesIndex);
      currentPatternIndicesIndex++;
    }
  }
}
