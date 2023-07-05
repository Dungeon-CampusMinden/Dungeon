package manual.quizquestion;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import core.hud.TextDialog;
import core.hud.UITools;

import quest.Quest;
import quest.QuestContent;
import quest.TaskReferenceComponent;
import quest.quizquestion.QuizQuestion;
import quest.quizquestion.QuizQuestionContent;
import quest.quizquestion.QuizQuestionUI;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static quest.quizquestion.QuizDialogDesign.ANSWERS_GROUP_NAME;

public class QuizQuestionWizardTest {

    private static final QuizQuestion SINGLE_CHOICE_QUESTION =
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
                    QuizQuestion.QuizQuestionType.SINGLE_CHOICE);
    private static final Consumer<Entity> wizardConsumer =
            entity -> {
                Quest quest = entity.fetch(TaskReferenceComponent.class).orElseThrow().quest();
                // build GUI with its callback
                QuizQuestionUI.showQuizDialog(SINGLE_CHOICE_QUESTION, (Entity entity1)->{

                    // TODO answer=
                   return (textDialog, id) -> {
                        if (Objects.equals(id, core.hud.UITools.DEFAULT_DIALOG_CONFIRM)) {
                            SnapshotArray<Actor> children = textDialog.getContentTable().getChildren();
                            // find the answersection .added a name to it for easier search
                            var answerSection = (VerticalGroup)children.select((actor) -> Objects.equals(actor.getName(), ANSWERS_GROUP_NAME)).iterator().next();

                            // do some magic
                            var answerText = switch ((QuizQuestion.QuizQuestionType)quest.type()){
                                case SINGLE_CHOICE -> getSingleChoiceText(answerSection);
                                case MULTIPLE_CHOICE -> "";
                                case FREETEXT -> getFreeTextAnswer(answerSection);
                            };



                            QuizQuestionContent answer = null;
                            answer = new QuizQuestionContent(QuizQuestionContent.QuizQuestionContentType.TEXT, answerText);
                            callback(quest, answer);
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
        new TaskReferenceComponent(wizard, SINGLE_CHOICE_QUESTION.task(), SINGLE_CHOICE_QUESTION);
        new InteractionComponent(wizard, 1, true, wizardConsumer);
    }

    private static final void callback(Quest quest, QuestContent... answers) {
        String content = "Given answer for Quest " + quest + " is " + answers[0];
        Entity entity = UITools.generateNewTextDialog(content, "Continue", "Answer");
        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
    }
}
