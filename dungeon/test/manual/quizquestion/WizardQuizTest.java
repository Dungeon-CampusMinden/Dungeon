package manual.quizquestion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.systems.*;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.hud.UITools;
import core.level.utils.LevelSize;
import core.systems.LevelSystem;

import task.Task;
import task.TaskComponent;
import task.TaskContent;
import task.quizquestion.Quiz;
import task.quizquestion.UIAnswerCallback;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

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
public class WizardQuizTest {
    private static Quiz question = multipleChoiceDummy();

    private static void toggleQuiz() {
        Game.initBaseLogger();
        switch (question.type()) {
            case FREETEXT -> question = singleChoiceDummy();
            case SINGLE_CHOICE -> question = multipleChoiceDummy();
            case MULTIPLE_CHOICE -> question = freeTextDummy();
        }
    }

    public static void main(String[] args) throws IOException {
        // start the game
        Game.hero(EntityFactory.newHero());
        LevelSystem.levelSize(LevelSize.SMALL);
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.userOnFrame(
                () -> {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.V)) toggleQuiz();
                });
        Game.userOnLevelLoad(
                () -> {
                    try {
                        questWizard();
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                });
        Game.windowTitle("Quest Wizard");
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        Game.addSystem(new XPSystem());
        Game.addSystem(new ProjectileSystem());

        // build and start game
        Game.run();
    }

    private static void questWizard() throws IOException {
        Entity wizard = new Entity("Quest Wizard");
        new PositionComponent(wizard);
        new DrawComponent(wizard, "character/wizard");
        new TaskComponent(wizard, question);
        new InteractionComponent(
                wizard,
                1,
                false,
                (entity, who) ->
                        UIAnswerCallback.askOnInteraction(question, showAnswersOnHud())
                                .accept(entity, who));
    }

    private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            AtomicReference<String> answers = new AtomicReference<>("");
            taskContents.stream()
                    .map(t -> (Quiz.Content) t)
                    .forEach(
                            t -> answers.set(answers.get() + t.content() + System.lineSeparator()));
            UITools.generateNewTextDialog(answers.get(), "Ok", "Given answer");
        };
    }

    public static Quiz singleChoiceDummy() {
        Quiz question = new Quiz(Quiz.Type.SINGLE_CHOICE, "Was ist kein Ziel von Refactoring?");
        question.addAnswer(new Quiz.Content("Lesbarkeit von Code verbessern"));
        question.addAnswer(new Quiz.Content("Verständlichkeit von Code verbessern"));
        question.addAnswer(new Quiz.Content("Wartbarkeit von Code verbessern"));
        question.addAnswer(new Quiz.Content("Fehler im Code ausmerzen"));
        return question;
    }

    public static Quiz multipleChoiceDummy() {
        Quiz question =
                new Quiz(
                        Quiz.Type.MULTIPLE_CHOICE,
                        "Welche der hier genannten Komponenten sind \"atomare Komponenten\"?");
        question.addAnswer(new Quiz.Content("Buttons"));
        question.addAnswer(new Quiz.Content("Frames"));
        question.addAnswer(new Quiz.Content("Label"));
        question.addAnswer(new Quiz.Content("Panels"));
        question.addAnswer(new Quiz.Content("Groups"));
        question.addAnswer(new Quiz.Content("EventListener"));
        question.addAnswer(new Quiz.Content("Events"));
        return question;
    }

    public static Quiz freeTextDummy() {
        return new Quiz(
                Quiz.Type.FREETEXT,
                "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
    }
}
