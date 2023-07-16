package manual.quizquestion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import core.Game;

import task.quizquestion.Quiz;
import task.quizquestion.QuizUI;

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
                        QuizUI.showQuizDialog(DummyQuizQuestionList.getRandomQuestion());
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

        public static Quiz singleChoiceDummy() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Was ist kein Ziel von Refactoring?");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT, "Lesbarkeit von Code verbessern");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT, "Verständlichkeit von Code verbessern");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT, "Wartbarkeit von Code verbessern");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Fehler im Code ausmerzen");
            return question;
        }

        public static Quiz multipleChoiceDummy() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.MULTIPLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Welche der hier genannten Komponenten sind \"atomare Komponenten\"?");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Buttons");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Frames");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Label");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Panels");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Groups");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "EventListener");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Events");
            return question;
        }

        public static Quiz singleChoiceDummy2() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Welche Methode/n muss der Observer mindestens implementieren?");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Eine update-Methode und eine register-Methode");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Eine notify-Methode und eine register-Methode");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Eine notify-Methode");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Eine register-Methode");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Eine update-Methode");
            return question;
        }

        public static Quiz freeTextDummy() {
            return new Quiz(
                    Quiz.QuizType.FREETEXT,
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
        }

        public static Quiz imageFreeTextDummy() {
            return new Quiz(
                    Quiz.QuizType.FREETEXT,
                    Quiz.QuizContent.QuizContentType.IMAGE,
                    "dungeon/assets/image_quiz/dummy.png");
        }

        public static Quiz singleChoiceDummy3() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Was ist 'Game Loop' in LibGDX und was macht diese?");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Launcher ruft abwechselnd die Methoden update und render auf.");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Durch Vererbung erzeugte Objekte werden über eine Game-Loop verwaltet.");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "ECS ist ein Software Architektur Pattern, das vor allem in der Spieleprogrammierung Anwendung findet.");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "ECS folgt dem Komposition über Vererbung - Prinzip.");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Alle ECS funktionieren mit einer Engine (Haupteinheit), bei der Entitäten und Systeme registriert werden.");
            return question;
        }

        public static Quiz singleChoiceDummy4() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Mit git log kann man sich eine Liste aller Commits anzeigen lassen.");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Wahr");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "Falsch");
            return question;
        }

        public static Quiz singleChoiceDummy5() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Über welche Methode kann ein Thread thread1 darauf warten, dass ein anderer Thread thread2 ihn über ein Objekt obj benachrichtigt?");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "obj.wait()");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "obj.wait(otherThread)");
            question.addAnswer(Quiz.QuizContent.QuizContentType.TEXT, "obj.waitFor(otherThread)");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT, "Thread.wait(obj, otherThread)");
            return question;
        }

        public static Quiz singleChoiceDummy6() {
            Quiz question =
                    new Quiz(
                            Quiz.QuizType.SINGLE_CHOICE,
                            Quiz.QuizContent.QuizContentType.TEXT,
                            "Was macht die notify()-Methode?");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Sie benachrichtigt alle Threads, die \"auf\" einem Objekt warten");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Sie benachrichtigt einen Thread, der \"auf\" einem Objekt wartet");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Sie benachrichtigt ein Objekt über den Zugriff eines Threads");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
                    "Sie benachrichtigt ein Objekt über Zustandsänderungen in einem anderen Objekt");
            question.addAnswer(
                    Quiz.QuizContent.QuizContentType.TEXT,
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
