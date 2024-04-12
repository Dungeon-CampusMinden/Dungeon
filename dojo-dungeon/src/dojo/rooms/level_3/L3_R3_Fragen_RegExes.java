package dojo.rooms.level_3;

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
import java.util.Random;
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
 * <p>In diesem Raum muss ein String eingegeben werden, der zum regulären Ausdruck passt. Wenn der
 * passende String eingegeben wurde, lässt OgreX den Spieler zur nächsten Ebene weitergehen.
 */
public class L3_R3_Fragen_RegExes extends Room {
  private final String[] regexes;

  {
    Random r = new Random();
    int min = r.nextInt(4) + 2; // 2-5
    int max = r.nextInt(5) + min; // 2-9
    regexes =
        new String[] {
          ".",
          String.format(".{%d,%d}", min, max),
          "\\d+",
          String.format("\\d{%d,%d}", min, max),
          "\\D+",
          String.format("\\D{%d,%d}", min, max),
          "\\s+",
          String.format("\\s{%d,%d}", min, max),
          "\\S+",
          String.format("\\S{%d,%d}", min, max),
          "\\w+",
          String.format("\\w{%d,%d}", min, max),
          "\\W+",
          String.format("\\W{%d,%d}", min, max),
          "Word",
          "(dog){3}",
          "((public|private|protected) )?class \\w+",
          "^.(?=.*[a-z].)(?=.*[0-9].)(?=.*[@#$,.].).{6,}$",
        };
  }

  private Entity bossOgrex;

  private String currentRegex;

  private int correctAnswerCount = 0;

  public L3_R3_Fragen_RegExes(
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
    bossOgrex = new Entity("OgreX");
    bossOgrex.add(new PositionComponent());
    bossOgrex.add(new DrawComponent(new SimpleIPath("character/monster/ogre")));

    setNextTask();

    return bossOgrex;
  }

  private void setNextTask() {
    // choose random regex for question
    nextRegex();
    final Quiz question = newFreeText();

    bossOgrex.add(new TaskComponent(question, bossOgrex));
    bossOgrex.add(
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

      String cleanedAnswer = answer.substring(0, answer.length() - 1);

      System.out.println(System.getProperty("os.name"));
      if (System.getProperty("os.name").startsWith("Windows")) {

        cleanedAnswer = cleanedAnswer.substring(0, cleanedAnswer.length() - 1);
      }

      if (cleanedAnswer.matches(getCurrentRegex())) {
        OkDialog.showOkDialog("Ihre Antwort ist korrekt!", "Antwort", () -> {});
        correctAnswerCount++;
        if (correctAnswerCount >= 3) {
          openDoors();
        }
        setNextTask();
      } else {
        OkDialog.showOkDialog("Ihre Antwort ist nicht korrekt!", "Ok", () -> {});
      }
    };
  }

  private Quiz newFreeText() {
    String questionText =
        "Gib einen String ein, der durch das RegEx pattern '"
            + getCurrentRegex()
            + "' gematcht wird.";

    return new FreeText(questionText);
  }

  private void nextRegex() {
    currentRegex = regexes[new Random().nextInt(regexes.length)];
  }

  private String getCurrentRegex() {
    if (currentRegex == null) {
      nextRegex();
    }
    return currentRegex;
  }
}
