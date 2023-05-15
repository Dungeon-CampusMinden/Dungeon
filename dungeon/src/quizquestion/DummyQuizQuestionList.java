package quizquestion;

import core.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class contains a Collection of simple QuizQuestion of each Type (Text, Image, Image + Text.
 * It can be used for testing as long as the QuizQuestion can not be loaded over the dsl input Use
 * {@code DummyQuizQuestionList.getRandomQuestion} to get a random QuizQuestion.
 *
 * @see QuizQuestion
 */
public class DummyQuizQuestionList {

    private static final List<QuizQuestion> questions =
            new ArrayList<>() {
                {
                    // "Einfache" Testfrage für SingleChoice
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Was ist kein Ziel von Refactoring?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Lesbarkeit von Code verbessern"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Verständlichkeit von Code verbessern"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Wartbarkeit von Code verbessern"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Fehler im Code ausmerzen"),
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));

                    // Testfrage für Multiple Choice
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Welche der hier genannten Komponenten sind \"atomare Komponenten\"?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Buttons"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Frames"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Label"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Panels"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Groups"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "EventListener"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Events"),
                                    },
                                    QuizQuestion.QuizQuestionType.MULTIPLE_CHOICE));

                    // Testfrage für Antwortmöglichkeiten mit bis zu 45 Buchstaben
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Welche Methode/n muss der Observer mindestens implementieren?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Eine update-Methode und eine register-Methode"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Eine notify-Methode und eine register-Methode"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Eine notify-Methode"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Eine register-Methode"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Eine update-Methode"),
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));

                    // Testfrage für Freitextfrage und lange Quizfragen mit bis zu 156 Buchstaben
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Mit welchem Befehl kann man sich Dateien in der Workingcopy anzeigen "
                                                    + "lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?"),
                                    new QuizQuestionContent[] {},
                                    QuizQuestion.QuizQuestionType.FREETEXT));

                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.IMAGE,
                                            Constants.EMPTY_MESSAGE),
                                    new QuizQuestionContent[] {},
                                    QuizQuestion.QuizQuestionType.FREETEXT));
                    // Testfrage mit Bild als Fragestellung und einer Antwort als SINGLE_CHOICE
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType
                                                    .TEXT_AND_IMAGE,
                                            "Was ist \"Game Loop\"in LibGDX und was macht diese?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Launcher ruft abwechselnd die Methoden update und render auf."),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Durch Vererbung erzeugte Objekte werden über eine Game-Loop verwaltet."),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "ECS ist ein Software Architektur Pattern, das vor allem in der Spieleprogrammierung Anwendung findet."),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "ECS folgt der Komposition ueber Vererbung -Prinzip."),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Alle ECS funktionieren mit einer Engine (Haupteinheit), bei der Entitäten und Systeme registriert werden.")
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));

                    // Testfrage für maximal zwei Antwortmöglichkeiten
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Mit git log kann man sich eine Liste aller Commits anzeigen lassen."),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Wahr"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Falsch"),
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));

                    // Testfrage für lange Quizfragen mit bis zu 130 Buchstaben
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Über welche Methode kann ein Thread thread1 darauf warten, dass ein "
                                                    + "anderer Thread thread2 ihn über ein Objekt obj benachrichtigt?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "obj.wait()"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "obj.wait(otherThread)"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "obj.waitFor(otherThread)"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Thread.wait(obj, otherThread)"),
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));

                    // Testfrage für sehr lange Antwortmöglichkeiten mit bis zu 77 Buchstaben
                    // (optional)
                    add(
                            new QuizQuestion(
                                    new QuizQuestionContent(
                                            QuizQuestionContent.QuizQuestionContentType.TEXT,
                                            "Was macht die notify()-Methode?"),
                                    new QuizQuestionContent[] {
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Sie benachrichtigt alle Threads, die \"auf\" einem Objekt warten"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Sie benachrichtigt einen Thread, der \"auf\" einem Objekt wartet"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Sie benachrichtigt ein Objekt über den Zugriff eines Threads"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Sie benachrichtigt ein Objekt über Zustandsänderungen in einem anderen Objekt"),
                                        new QuizQuestionContent(
                                                QuizQuestionContent.QuizQuestionContentType.TEXT,
                                                "Sie benachrichtigt den ersten Thread in der Warteliste auf einem Objekt"),
                                    },
                                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE));
                }
            };

    /**
     * Returns the QuizQuestion at the specified index.
     *
     * @param index the index of the QuizQuestion to return
     * @return the QuizQuestion at the specified index
     * @apiNote Use this method when you want to retrieve a specific QuizQuestion from the list of
     *     questions. This can be useful when testing a specific type of question or when you want
     *     to reference a particular question in your code.
     */
    public static QuizQuestion getQuestionByIndex(int index) {
        if (index < 0 || index >= questions.size())
            throw new NullPointerException("Invalid question index");
        return questions.get(index);
    }

    /**
     * Returns a random QuizQuestion from the list of questions.
     *
     * @return a random QuizQuestion
     * @apiNote Use this method when you want to retrieve a QuizQuestion at random from the list of
     *     questions.
     */
    public static QuizQuestion getRandomQuestion() {
        Random rnd = new Random();
        return questions.get(rnd.nextInt(questions.size()));
    }
}
