package manual.quizquestion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import core.Game;

import task.quizquestion.QuizQuestion;
import task.quizquestion.QuizQuestionUI;

import java.util.Random;

/**
 * This is a manual test for the QuizQuestion-UI.
 *
 * <p>It sets up a basic game and will show a random QuizQuestion if "F" is pressed.
 *
 * <p>Use this to check if the UI is displayed correctly.
 */
public class QuizQuestionUITest {

    public static void main(String[] args) {
        Game.userOnFrame(
                () -> {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        // Dialogue for quiz questions (display of quiz questions and the answer
                        // area in test
                        // mode)
                        QuizQuestionUI.showQuizDialog(DummyQuizQuestionList.getRandomQuestion());
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
     * @see QuizQuestion
     */
    public static class DummyQuizQuestionList {

        public static QuizQuestion singleChoiceDummy() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Was ist kein Ziel von Refactoring?");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Lesbarkeit von Code verbessern");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT, "Verständlichkeit von Code verbessern");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT, "Wartbarkeit von Code verbessern");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Fehler im Code ausmerzen");
            return question;
        }

        public static QuizQuestion multipleChoiceDummy() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.MULTIPLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Welche der hier genannten Komponenten sind \"atomare Komponenten\"?");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Buttons");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Frames");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Label");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Panels");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Groups");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "EventListener");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Events");
            return question;
        }

        public static QuizQuestion singleChoiceDummy2() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Welche Methode/n muss der Observer mindestens implementieren?");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Eine update-Methode und eine register-Methode");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Eine notify-Methode und eine register-Methode");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Eine notify-Methode");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Eine register-Methode");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Eine update-Methode");
            return question;
        }

        public static QuizQuestion freeTextDummy() {
            return new QuizQuestion(
                    QuizQuestion.QuizType.FREETEXT,
                    QuizQuestion.QuizContentType.TEXT,
                    "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
        }

        public static QuizQuestion imageFreeTextDummy() {
            return new QuizQuestion(
                    QuizQuestion.QuizType.FREETEXT,
                    QuizQuestion.QuizContentType.IMAGE,
                    "dungeon/assets/image_quiz/dummy.png");
        }

        public static QuizQuestion singleChoiceDummy3() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Was ist 'Game Loop' in LibGDX und was macht diese?");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Launcher ruft abwechselnd die Methoden update und render auf.");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Durch Vererbung erzeugte Objekte werden über eine Game-Loop verwaltet.");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "ECS ist ein Software Architektur Pattern, das vor allem in der Spieleprogrammierung Anwendung findet.");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "ECS folgt dem Komposition über Vererbung - Prinzip.");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Alle ECS funktionieren mit einer Engine (Haupteinheit), bei der Entitäten und Systeme registriert werden.");
            return question;
        }

        public static QuizQuestion singleChoiceDummy4() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Mit git log kann man sich eine Liste aller Commits anzeigen lassen.");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Wahr");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Falsch");
            return question;
        }

        public static QuizQuestion singleChoiceDummy5() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Über welche Methode kann ein Thread thread1 darauf warten, dass ein anderer Thread thread2 ihn über ein Objekt obj benachrichtigt?");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "obj.wait()");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "obj.wait(otherThread)");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "obj.waitFor(otherThread)");
            question.addAnswer(QuizQuestion.QuizContentType.TEXT, "Thread.wait(obj, otherThread)");
            return question;
        }

        public static QuizQuestion singleChoiceDummy6() {
            QuizQuestion question =
                    new QuizQuestion(
                            QuizQuestion.QuizType.SINGLE_CHOICE,
                            QuizQuestion.QuizContentType.TEXT,
                            "Was macht die notify()-Methode?");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Sie benachrichtigt alle Threads, die \"auf\" einem Objekt warten");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Sie benachrichtigt einen Thread, der \"auf\" einem Objekt wartet");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Sie benachrichtigt ein Objekt über den Zugriff eines Threads");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Sie benachrichtigt ein Objekt über Zustandsänderungen in einem anderen Objekt");
            question.addAnswer(
                    QuizQuestion.QuizContentType.TEXT,
                    "Sie benachrichtigt den ersten Thread in der Warteliste auf einem Objekt");
            return question;
        }

        /**
         * Returns a random QuizQuestion from the list of questions.
         *
         * @return a random QuizQuestion
         * @apiNote Use this method when you want to retrieve a QuizQuestion at random from the list
         *     of questions.
         */
        public static QuizQuestion getRandomQuestion() {
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
                    return singleChoiceDummy6();
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
