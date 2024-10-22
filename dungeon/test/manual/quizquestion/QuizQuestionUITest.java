package manual.quizquestion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import contrib.systems.HudSystem;
import core.Game;
import java.util.Random;
import java.util.logging.Level;
import task.game.hud.QuizUI;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

/**
 * This is a manual test for the QuizQuestion-UI.
 *
 * <p>It sets up a basic game and will show a random QuizQuestion if "F" is pressed.
 *
 * <p>Use this to check if the UI is displayed correctly.
 *
 * <p>Use ./gradlew runManualQuizTest
 */
public class QuizQuestionUITest {

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Game.initBaseLogger(Level.ALL);
    Game.add(new HudSystem());
    Game.userOnFrame(
        () -> {
          if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            // Dialogue for quiz questions (display of quiz questions and the answer
            // area in test
            // mode)
            Quiz question = DummyQuizQuestionList.getRandomQuestion();
            question.scoringFunction(
                (task, contents) -> {
                  System.out.println("Given answers");
                  contents.forEach(System.out::println);
                  return 1f;
                });
            QuizUI.askQuizOnHud(question);
          }
        });

    // build and start game
    Game.run();
  }

  /**
   * This class contains a Collection of simple QuizQuestion of each Type (Text, Image, Image +
   * Text. It can be used for testing as long as the QuizQuestion can not be loaded over the dsl
   * input Use {@code DummyQuizQuestionList.getRandomQuestion} to get a random QuizQuestion.
   *
   * @see Quiz
   */
  public static class DummyQuizQuestionList {

    /**
     * Generate a single choice dummy quiz question.
     *
     * @return the generated single choice quiz question
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
     * Generate a multiple choice quiz question with specified components.
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
     * Creates and returns a dummy single-choice quiz with multiple answers.
     *
     * @return the quiz object with the question and answer options
     */
    public static Quiz singleChoiceDummy2() {
      Quiz question =
          new SingleChoice("Welche Methode/n muss der Observer mindestens implementieren?");
      question.addAnswer(new Quiz.Content("Eine update-Methode und eine register-Methode"));

      question.addAnswer(new Quiz.Content("Eine notify-Methode und eine register-Methode"));

      question.addAnswer(new Quiz.Content("Eine notify-Methode"));
      question.addAnswer(new Quiz.Content("Eine register-Methode"));
      question.addAnswer(new Quiz.Content("Eine update-Methode"));
      return question;
    }

    /**
     * Creates and returns a new instance of the FreeText class with a predefined question.
     *
     * @return A new instance of the FreeText class with a predefined question.
     */
    public static Quiz freeTextDummy() {
      return new FreeText(
          "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
    }

    /**
     * Generates a dummy quiz question with an image for the user to identify the color seen.
     *
     * @return the created quiz question
     */
    public static Quiz imageFreeTextDummy() {
      Quiz question =
          new SingleChoice(
              "Welche Farbe siehst du?", new Image(new Texture("logo/cat_logo_35x35.png")));
      question.addAnswer(new Quiz.Content("Weiß"));
      question.addAnswer(new Quiz.Content("Schwarz"));
      question.addAnswer(new Quiz.Content("Das ist eine Katze!"));
      return question;
    }

    /**
     * Generates a dummy single choice quiz with an image.
     *
     * @return The generated quiz with a dummy image.
     */
    public static Quiz imageSingleChoiceDummy() {
      return new FreeText("", new Image(new Texture("image_quiz/dummy.png")));
    }

    /**
     * Generate a single choice dummy quiz question.
     *
     * @return the created single choice quiz question
     */
    public static Quiz singleChoiceDummy3() {
      Quiz question = new SingleChoice("Was ist 'Game Loop' in LibGDX und was macht diese?");
      question.addAnswer(
          new Quiz.Content("Launcher ruft abwechselnd die Methoden update und render auf."));
      question.addAnswer(
          new Quiz.Content(
              "Durch Vererbung erzeugte Objekte werden über eine Game-Loop verwaltet."));
      question.addAnswer(
          new Quiz.Content(
              "ECS ist ein Software Architektur Pattern, das vor allem in der Spieleprogrammierung Anwendung findet."));
      question.addAnswer(new Quiz.Content("ECS folgt dem Komposition über Vererbung - Prinzip."));
      question.addAnswer(
          new Quiz.Content(
              "Alle ECS funktionieren mit einer Engine (Haupteinheit), bei der Entitäten und Systeme registriert werden."));
      return question;
    }

    /**
     * Generate a single choice dummy quiz question.
     *
     * @return the created single choice quiz question
     */
    public static Quiz singleChoiceDummy4() {
      Quiz question =
          new SingleChoice("Mit git log kann man sich eine Liste aller Commits anzeigen lassen.");
      question.addAnswer(new Quiz.Content("Wahr"));
      question.addAnswer(new Quiz.Content("Falsch"));
      return question;
    }

    /**
     * Generate a single choice dummy quiz question.
     *
     * @return the created single choice quiz question
     */
    public static Quiz singleChoiceDummy5() {
      Quiz question =
          new SingleChoice(
              "Über welche Methode kann ein Thread thread1 darauf warten, dass ein anderer Thread thread2 ihn über ein Objekt obj benachrichtigt?");
      question.addAnswer(new Quiz.Content("obj.wait()"));
      question.addAnswer(new Quiz.Content("obj.wait(otherThread)"));
      question.addAnswer(new Quiz.Content("obj.waitFor(otherThread)"));
      question.addAnswer(new Quiz.Content("Thread.wait(obj, otherThread)"));
      return question;
    }

    /**
     * Generate a single choice dummy quiz question.
     *
     * @return the created single choice quiz question
     */
    public static Quiz singleChoiceDummy6() {
      Quiz question = new SingleChoice("Was macht die notify()-Methode?");
      question.addAnswer(
          new Quiz.Content("Sie benachrichtigt alle Threads, die \"auf\" einem Objekt warten"));
      question.addAnswer(
          new Quiz.Content("Sie benachrichtigt einen Thread, der \"auf\" einem Objekt wartet"));
      question.addAnswer(
          new Quiz.Content("Sie benachrichtigt ein Objekt über den Zugriff eines Threads"));
      question.addAnswer(
          new Quiz.Content(
              "Sie benachrichtigt ein Objekt über Zustandsänderungen in einem anderen Objekt"));
      question.addAnswer(
          new Quiz.Content(
              "Sie benachrichtigt den ersten Thread in der Warteliste auf einem Objekt"));
      return question;
    }

    /**
     * Returns a random QuizQuestion from the list of questions.
     *
     * @return a random QuizQuestion
     * @apiNote Use this method when you want to retrieve a QuizQuestion at random from the list of
     *     questions.
     */
    public static Quiz getRandomQuestion() {
      Random rnd = new Random();
      int random = rnd.nextInt(9);
      switch (random) {
        case 0 -> {
          return freeTextDummy();
        }
        case 1 -> {
          return singleChoiceDummy();
        }
        case 2 -> {
          return singleChoiceDummy2();
        }
        case 3 -> {
          return singleChoiceDummy3();
        }
        case 4 -> {
          return singleChoiceDummy4();
        }
        case 5 -> {
          return singleChoiceDummy5();
        }
        case 6 -> {
          return imageSingleChoiceDummy();
        }
        case 7 -> {
          return multipleChoiceDummy();
        }
        default -> {
          return imageFreeTextDummy();
        }
      }
    }
  }
}
