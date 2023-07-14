package manual.quizquestion;

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
import core.components.UIComponent;
import core.hud.UITools;
import core.level.utils.LevelSize;
import core.systems.LevelSystem;
import task.Task;
import task.TaskComponent;
import task.quizquestion.QuizQuestion;
import task.quizquestion.QuizUI;


import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static task.quizquestion.QuizDialogDesign.ANSWERS_GROUP_NAME;

public class WizardQuizTest {


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


    private static final Consumer<Entity> wizardInteractionCallback =
        entity -> {
            Task quest = entity.fetch(TaskComponent.class).orElseThrow().task();
            // build GUI with its callback
            QuizUI.showQuizDialog((QuizQuestion) quest, (Entity wizard)->{
            System.out.println("DEBUG WIZARD: 58" );
                // TODO answer=
                return (textDialog, id) -> {
                    if (Objects.equals(id, core.hud.UITools.DEFAULT_DIALOG_CONFIRM)) {
                        SnapshotArray<Actor> children = textDialog.getContentTable().getChildren();
                        // find the answersection .added a name to it for easier search
                        var answerSection = (VerticalGroup)children.select((actor) -> Objects.equals(actor.getName(), ANSWERS_GROUP_NAME)).iterator().next();
                        System.out.println("DEBUG WIZARD: 65" + answerSection);
                        // do some magic
                        var answerText = switch (((QuizQuestion)quest).type()){
                            case SINGLE_CHOICE -> getSingleChoiceText(answerSection);
                            case MULTIPLE_CHOICE -> ""; //TODO ???
                            case FREETEXT -> getFreeTextAnswer(answerSection);
                        };
                        System.out.println("DEBUG WIZARD: 70" );
                        //antwort steht in answerText
                        //UITools.generateNewTextDialog(answerText,"Ok","The answer was");
                        System.out.println(answerText);
                        Game.removeEntity(entity);
                        return true;
                    }
                    return false;

                };
            });

        };

    private static String getSingleChoiceText(VerticalGroup answerSection) {
        return ((VerticalGroup)
            ((ScrollPane) answerSection.getChildren().get(0))
                .getChildren().get(0)).getChildren().select((x) -> x instanceof CheckBox checkbox && checkbox.isChecked())
            .iterator()
            .next() instanceof CheckBox checked ? checked.getName() : "No Selection";
    }

    private static String getFreeTextAnswer(VerticalGroup answerSection) {
        return ((TextArea) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0)).getText();
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
        new TaskComponent(wizard, singleChoiceDummy());
        new InteractionComponent(wizard, 1, true, wizardInteractionCallback);
    }

}
