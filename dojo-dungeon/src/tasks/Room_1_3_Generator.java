package tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.InteractionComponent;
import contrib.hud.dialogs.TextDialog;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import task.Task;
import task.TaskContent;
import task.game.components.TaskComponent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

public class Room_1_3_Generator extends TaskRoomGenerator {
  private static Quiz question = multipleChoice2();

  // to toggle between question types
  private static int toggle = 0;

  public Room_1_3_Generator(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) {
    super(gen, room, nextNeighbour);
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.SMALL, getRoom().neighbours()),
                DesignLabel.randomDesign()));

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
    Entity bossOgrex = new Entity("OgreX");
    bossOgrex.add(new PositionComponent());
    bossOgrex.add(new DrawComponent(new SimpleIPath("character/monster/ogre")));
    bossOgrex.add(new TaskComponent(question, bossOgrex));
    bossOgrex.add(
        new InteractionComponent(
            1,
            true,
            (e, who) -> {
              if (Gdx.input.isKeyJustPressed(Input.Keys.E))
                toggleQuiz(); // toggle between question types
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
      TextDialog.textDialog(answers.get(), "Ok", "Ihre Antwort");
    };
  }

  private static void toggleQuiz() {
    toggle = (toggle + 1) % 4;
    switch (toggle) {
      case 0 -> question = singleChoice();
      case 1 -> question = multipleChoice();
      case 2 -> question = multipleChoice2();
      case 3 -> question = freeText();
    }
  }

  public static Quiz singleChoice() {
    SingleChoice question = new SingleChoice("Wofür werden reguläre Ausdrücke nicht genutzt?");
    question.addAnswer(new Quiz.Content("Zum Finden von Bestandteilen in Zeichenketten"));
    question.addAnswer(new Quiz.Content("Zum Validieren textueller Eingaben"));
    question.addAnswer(new Quiz.Content("Zum Aufteilen von Strings in Tokens"));
    question.addAnswer(new Quiz.Content("Zum Konkatenieren von Strings"));
    return question;
  }

  public static Quiz multipleChoice() {
    Quiz question =
        new MultipleChoice(
            "Welche der folgenden regulären Ausdrücke sind valide, wenn es darum geht,"
                + " eine dreistellige Zahl zu beschreiben?");
    question.addAnswer(new Quiz.Content("\\D[0-9]\\D"));
    question.addAnswer(new Quiz.Content("\t\\d\\d\\d"));
    question.addAnswer(new Quiz.Content("\\d\\D[0-9]"));
    question.addAnswer(new Quiz.Content("3[0-9]"));
    question.addAnswer(new Quiz.Content("3*\\d"));
    question.addAnswer(new Quiz.Content("[0-9]\\d\\d"));
    question.addAnswer(new Quiz.Content("\t\\d[0-9](3)"));
    return question;
  }

  public static Quiz multipleChoice2() {
    Quiz question =
        new MultipleChoice(
            "Welche der Strings entsprechen dem folgenden regulären Ausdruck: \"B[A-Z2-5a-e]\\\\.N\"");
    question.addAnswer(new Quiz.Content("BAN"));
    question.addAnswer(new Quiz.Content("B4\\NN"));
    question.addAnswer(new Quiz.Content("Aa\\rN"));
    question.addAnswer(new Quiz.Content("B3\\JN"));
    question.addAnswer(new Quiz.Content("BX\\en"));
    question.addAnswer(new Quiz.Content("Bl\\\\mathbb{N}"));
    question.addAnswer(new Quiz.Content("Be\\mathbb{N}"));
    question.addAnswer(new Quiz.Content("\"B6\\rN\""));
    question.addAnswer(new Quiz.Content("Be\\J"));
    question.addAnswer(new Quiz.Content("AA\\AN"));
    return question;
  }

  public static Quiz freeText() {
    return new FreeText("Was ist ein regulärer Ausdruck?");
  }
}
