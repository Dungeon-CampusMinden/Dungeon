package manual.quizquestion;

import static task.quizquestion.QuizDialogDesign.ANSWERS_GROUP_NAME;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.SnapshotArray;

import contrib.components.InteractionComponent;
import contrib.configuration.ItemConfig;
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
import task.quizquestion.Quiz;
import task.quizquestion.QuizUI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class WizardQuizTest {

    public static Quiz singleChoiceDummy() {
        Quiz question =
                new Quiz(
                        Quiz.Type.SINGLE_CHOICE,
                        Quiz.Content.Type.TEXT,
                        "Was ist kein Ziel von Refactoring?");
        question.addAnswer(
                new Quiz.Content(Quiz.Content.Type.TEXT, "Lesbarkeit von Code verbessern"));
        question.addAnswer(
                new Quiz.Content(Quiz.Content.Type.TEXT, "Verständlichkeit von Code verbessern"));
        question.addAnswer(
                new Quiz.Content(Quiz.Content.Type.TEXT, "Wartbarkeit von Code verbessern"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Fehler im Code ausmerzen"));
        return question;
    }

    public static Quiz multipleChoiceDummy() {
        Quiz question =
                new Quiz(
                        Quiz.Type.MULTIPLE_CHOICE,
                        Quiz.Content.Type.TEXT,
                        "Welche der hier genannten Komponenten sind \"atomare Komponenten\"?");
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Buttons"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Frames"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Label"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Panels"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Groups"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "EventListener"));
        question.addAnswer(new Quiz.Content(Quiz.Content.Type.TEXT, "Events"));
        return question;
    }

    public static Quiz freeTextDummy() {
        return new Quiz(
                Quiz.Type.FREETEXT,
                Quiz.Content.Type.TEXT,
                "Mit welchem Befehl kann man sich Dateien in der Working copy anzeigen lassen, die unversioniert sind oder in denen es Änderungen seit dem letzten Commit gab?");
    }

    //todo extract
    private static final Consumer<Entity> wizardInteractionCallback =
            wizzard -> {
                System.out.println("INTERACTION");
                Task quest = wizzard.fetch(TaskComponent.class).orElseThrow().task();
                // build GUI with its callback
                //todo extract
                QuizUI.showQuizDialog(
                        (Quiz) quest,
                        (Entity hudEntity) -> {
                            // TODO answer=
                            return (textDialog, id) -> {
                                if (Objects.equals(id, core.hud.UITools.DEFAULT_DIALOG_CONFIRM)) {
                                    SnapshotArray<Actor> children =
                                            ((VerticalGroup)
                                                            textDialog
                                                                    .getContentTable()
                                                                    .getChildren()
                                                                    .get(0))
                                                    .getChildren();
                                    // find the answersection .added a name to it for easier search
                                    var answerSection =
                                            (VerticalGroup)
                                                    children.select(
                                                                    (actor) ->
                                                                            Objects.equals(
                                                                                    actor.getName(),
                                                                                    ANSWERS_GROUP_NAME))
                                                            .iterator()
                                                            .next();
                                    // do some magic    //todo extract
                                    switch (((Quiz) quest).type()) {
                                        case SINGLE_CHOICE -> UITools.generateNewTextDialog(
                                                getSingleChoiceAnswer(answerSection),
                                                "Ok",
                                                "The answer was");
                                        case MULTIPLE_CHOICE -> {
                                            Set<String> answers =
                                                    getMultipleChoiceAnswer(answerSection);
                                            System.out.println("DEBUG " + answers.size());
                                            AtomicReference<String> antworten =
                                                    new AtomicReference<>("");
                                            answers.forEach(
                                                    s ->
                                                            antworten.set(
                                                                    antworten.get()
                                                                            + s
                                                                            + System
                                                                                    .lineSeparator()));
                                            UITools.generateNewTextDialog(
                                                    antworten.get(), "Ok", "The answer was");
                                        }
                                        case FREETEXT -> UITools.generateNewTextDialog(
                                                getFreeTextAnswer(answerSection),
                                                "Ok",
                                                "The answer was");
                                    }
                                    ;
                                    Game.removeEntity(hudEntity);

                                    return true;
                                }
                                return false;
                            };
                        });
            };


    //todo extract
    private static String getSingleChoiceAnswer(VerticalGroup answerSection) {
        return ((VerticalGroup)
                                        ((ScrollPane) answerSection.getChildren().get(0))
                                                .getChildren()
                                                .get(0))
                                .getChildren()
                                .select(
                                        (x) ->
                                                x instanceof CheckBox checkbox
                                                        && checkbox.isChecked())
                                .iterator()
                                .next()
                        instanceof CheckBox checked
                ? checked.getText().toString()
                : "No Selection";
    }

    //todo extract
    private static Set<String> getMultipleChoiceAnswer(VerticalGroup answerSection) {
        Set<String> answers = new HashSet<>();

        Iterator iterator =
                ((VerticalGroup)
                                ((ScrollPane) answerSection.getChildren().get(0))
                                        .getChildren()
                                        .get(0))
                        .getChildren()
                        .select((x) -> x instanceof CheckBox checkbox && checkbox.isChecked())
                        .iterator();

        while (iterator.hasNext())
            if (iterator.next() instanceof CheckBox checked)
                answers.add(checked.getText().toString());
        if (answers.size() == 0) answers.add("No Selection");
        return answers;
    }
    //todo extract

    private static String getFreeTextAnswer(VerticalGroup answerSection) {
        return ((TextArea) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0))
                .getText();
    }




    public static void main(String[] args) throws IOException {
        // start the game
        Game.hero(EntityFactory.newHero());
        LevelSystem.levelSize(LevelSize.SMALL);
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class,
                ItemConfig.class);
        Game.frameRate(30);
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
        new TaskComponent(wizard, TEST_QUIZ);
        new InteractionComponent(wizard, 1, false, wizardInteractionCallback);
    }

    public static final Quiz TEST_QUIZ = multipleChoiceDummy();
}
