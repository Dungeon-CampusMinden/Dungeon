package manual.quizquestion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.InteractionComponent;
import contrib.entities.HeroFactory;
import contrib.hud.dialogs.TextDialog;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import task.Task;
import task.TaskContent;
import task.game.components.TaskComponent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

/**
 * Test scenario for the UI Callbacks.
 *
 * <p>Will spawn a Wizard in each level that will ask you a question on the HUD and will show the
 * selected answers in the next HUD windows.
 *
 * <p>You have to interact with the wizard; you can only interact with the wizard once per level.
 *
 * <p>Press V to switch the question type that will be asked in the next level.
 *
 * <p>Start the test with gradle runCallbackTest.
 */
public class CallbackTest {

  private static Quiz question = multipleChoiceDummy();

  // to toggle between question types
  private static int toggle = 0;

  private static void toggleQuiz() {
    toggle = (toggle + 1) % 3;
    switch (toggle) {
      case 0 -> question = singleChoiceDummy();
      case 1 -> question = multipleChoiceDummy();
      case 2 -> question = freeTextDummy();
    }
  }

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.ALL);
    // start the game

    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.userOnSetup(
        () -> {
          try {
            Game.add(new AISystem());
            Game.add(new CollisionSystem());
            Game.add(new HealthSystem());
            Game.add(new ProjectileSystem());
            Game.add(new HudSystem());
            Entity hero = HeroFactory.newHero();
            Game.add(hero);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    Game.userOnFrame(
        () -> {
          if (Gdx.input.isKeyJustPressed(Input.Keys.V)) toggleQuiz();
        });
    Game.userOnLevelLoad(
        (loadFirstTime) -> {
          try {
            if (loadFirstTime) Game.add(questWizard());
          } catch (IOException e) {
            throw new RuntimeException();
          }
        });
    Game.windowTitle("Quest Wizard");

    // build and start game
    Game.run();
  }

  private static Entity questWizard() throws IOException {
    Entity wizard = new Entity("Quest Wizard");
    wizard.add(new PositionComponent());
    wizard.add(new DrawComponent(new SimpleIPath("character/wizard")));
    wizard.add(new TaskComponent(question, wizard));
    wizard.add(
        new InteractionComponent(
            1,
            false,
            (entity, who) ->
                UIAnswerCallback.askOnInteraction(question, showAnswersOnHud())
                    .accept(entity, who)));
    return wizard;
  }

  private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
    return (task, taskContents) -> {
      AtomicReference<String> answers = new AtomicReference<>("");
      taskContents.stream()
          .map(t -> (Quiz.Content) t)
          .forEach(t -> answers.set(answers.get() + t.content() + System.lineSeparator()));
      TextDialog.textDialog(answers.get(), "Ok", "Given answer");
    };
  }

  /**
   * Generates a single choice quiz question about the goals of Refactoring.
   *
   * @return The generated single choice quiz question.
   */
  public static Quiz singleChoiceDummy() {
    Quiz question = new SingleChoice("Was ist kein Ziel von Refactoring?");
    question.addAnswer(new Quiz.Content("Lesbarkeit von Code verbessern"));
    question.addAnswer(new Quiz.Content("Verständlichkeit von Code verbessern"));
    question.addAnswer(new Quiz.Content("Wartbarkeit von Code verbessern"));
    question.addAnswer(new Quiz.Content("Fehler im Code ausmerzen"));
    return question;
  }

  /**
   * A method that generates a multiple choice quiz question.
   *
   * @return the generated multiple choice quiz question
   */
  public static Quiz multipleChoiceDummy() {
    Quiz question =
        new MultipleChoice("Welche der hier genannten Komponenten sind \"atomare Komponenten\"?");
    question.addAnswer(new Quiz.Content("Buttons"));
    question.addAnswer(new Quiz.Content("Frames"));
    question.addAnswer(new Quiz.Content("Label"));
    question.addAnswer(new Quiz.Content("Panels"));
    question.addAnswer(new Quiz.Content("Groups"));
    question.addAnswer(new Quiz.Content("EventListener"));
    question.addAnswer(new Quiz.Content("Events"));
    return question;
  }

  /**
   * Generate a dummy FreeText Quiz object with a specific question.
   *
   * @return the FreeText Quiz object with the specified question
   */
  public static Quiz freeTextDummy() {
    return new FreeText(
        "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
  }
}
