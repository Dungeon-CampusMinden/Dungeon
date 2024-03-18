package tasks.level_1;

import contrib.components.InteractionComponent;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
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
import tasks.TaskRoomGenerator;

public class Room_1_3_Generator extends TaskRoomGenerator {
  private static final String[] regexes = {
    "Wort", "\\d+", "((public|private|protected) )?class \\w+Class \\{(.*\\n*)*\\}"
  };
  private static final int chosenRegex = new Random().nextInt(regexes.length);
  private static Quiz question = freeText();

  public Room_1_3_Generator(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) {
    super(gen, room, nextNeighbour);
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.SMALL, getRoom().neighbours()), DesignLabel.FOREST));

    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    // add boss
    roomEntities.add(questBoss());
    // add the entities as payload to the LevelNode
    getRoom().entities(roomEntities);
    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    getRoom().level().onFirstLoad(() -> getRoom().entities().forEach(Game::add));
  }

  private static Entity questBoss() throws IOException {
    // choose random regex for question

    Entity bossOgrex = new Entity("OgreX");
    bossOgrex.add(new PositionComponent());
    bossOgrex.add(new DrawComponent(new SimpleIPath("character/monster/ogre")));
    bossOgrex.add(new TaskComponent(question, bossOgrex));
    bossOgrex.add(
        new InteractionComponent(
            1,
            true,
            (e, who) -> {
              UIAnswerCallback.askOnInteraction(question, showAnswersOnHud()).accept(e, who);
            }));
    return bossOgrex;
  }

  private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
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

      if (cleanedAnswer.matches(regexes[chosenRegex])) {
        OkDialog.showOkDialog("Ihre Antwort ist korrekt!", "Antwort", () -> {});
      } else {
        OkDialog.showOkDialog("Ihre Antwort ist nicht korrekt!", "Ok", () -> {});
      }
    };
  }

  public static Quiz freeText() {
    String questionText =
        "Gib einen String ein, der durch das RegEx pattern '"
            + regexes[chosenRegex]
            + "' gematcht wird.";

    return new FreeText(questionText);
  }
}
