package manual.taskgeneration;

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

import dslToGame.DslFileLoader;
import dslToGame.QuestConfig;

import interpreter.DSLInterpreter;

import task.Task;
import task.TaskComponent;
import task.TaskContent;
import task.quizquestion.Quiz;
import task.quizquestion.UIAnswerCallback;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * <a
 * href="file:../../../../doc/tasks/anleitung_task_test.md">../../../../doc/tasks/anleitung_task_test.md</a>
 */
public class TaskGenerationTest {
    static DSLInterpreter interpreter = new DSLInterpreter();

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        LevelSystem.levelSize(LevelSize.MEDIUM);
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.disableAudio(true);
        Game.userOnSetup(
                () -> {
                    try {
                        Entity hero = EntityFactory.newHero();
                        Game.hero(hero);
                        Game.add(hero);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        Game.userOnLevelLoad(
                (b) -> {
                    if (b) {
                        try {

                            Game.add(EntityFactory.randomMonster());
                            Game.add(EntityFactory.newChest());

                        } catch (IOException e) {
                            // oh well
                        }

                        Set<File> files = DslFileLoader.dslFiles();
                        List<String> fileContents =
                                files.stream()
                                        .filter(f -> f.getName().endsWith("task_test.dng"))
                                        .map(DslFileLoader::fileToString)
                                        .toList();

                        // for the start: print on console
                        buildScenarios(fileContents.get(0));
                    }
                });

        Game.windowTitle("Task Test");
        Game.add(new AISystem());
        Game.add(new CollisionSystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());

        Game.run();
    }

    private static void buildScenarios(String dslFileContent) {
        QuestConfig config = (QuestConfig) interpreter.getQuestConfig(dslFileContent);
        for (Task task : config.tasks()) {
            try {
                questWizard((Quiz) task);
            } catch (IOException e) {
                // oh well
            }
        }
    }

    // private static void questWizard(Quiz quiz) throws IOException {
    private static void questWizard(Quiz quiz) throws IOException {
        Random random = new Random();
        String texture = "character/wizard";
        if (random.nextInt() % 2 == 0) {
            texture = "character/blue_knight";
        }

        Entity wizard = new Entity("Quest Wizard");
        wizard.addComponent(new PositionComponent());
        wizard.addComponent(new DrawComponent(texture));
        wizard.addComponent(new TaskComponent(quiz));
        wizard.addComponent(
                new InteractionComponent(
                        1, true, UIAnswerCallback.askOnInteraction(quiz, showAnswersOnHud())));
        Game.add(wizard);
    }

    private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            float score = task.scoringFunction().apply(task, taskContents);
            UITools.generateNewTextDialog("Your score: " + score, "Ok", "Given answer");
        };
    }
}
