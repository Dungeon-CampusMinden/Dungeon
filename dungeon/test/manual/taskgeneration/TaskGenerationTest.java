package manual.taskgeneration;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.UITools;
import contrib.systems.*;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.LevelSize;
import core.systems.LevelSystem;

import dsl.interpreter.DSLInterpreter;

import dslinput.DslFileLoader;
import dslinput.DungeonConfig;

import task.Task;
import task.TaskContent;
import task.components.TaskComponent;
import task.tasktype.Quiz;
import task.utils.hud.UIAnswerCallback;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * <a
 * href="file:../../../../doc/tasks/anleitung_task_test.md">../../../../doc/tasks/anleitung_task_test.md</a>
 *
 * <p>Use taskGenerationTest --args "dungeon/assets/scripts/task_test.dng" to run thus
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
                        Game.add(new AISystem());
                        Game.add(new CollisionSystem());
                        Game.add(new HealthSystem());
                        Game.add(new XPSystem());
                        Game.add(new ProjectileSystem());
                        Game.add(new HealthbarSystem());
                        Game.add(new HeroUISystem());
                        Game.add(new HudSystem());
                        Entity hero = EntityFactory.newHero();
                        Game.hero(hero);
                        Game.add(hero);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        Game.userOnLevelLoad(
                (loadFirstTime) -> {
                    if (loadFirstTime) {
                        try {

                            Game.add(EntityFactory.randomMonster());
                            Game.add(EntityFactory.newChest());
                        } catch (IOException e) {
                            // oh well
                        }

                        Set<Path> dslFilePaths;
                        try {
                            dslFilePaths = DslFileLoader.processArguments(args);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        List<String> fileContents =
                                dslFilePaths.stream().map(DslFileLoader::fileToString).toList();
                        buildScenarios(fileContents.get(0));
                    }
                });

        Game.windowTitle("Task Test");
        Game.run();
    }

    private static void buildScenarios(String dslFileContent) {
        DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(dslFileContent);
        config.dependencyGraph()
                .nodeIterator()
                .forEachRemaining(
                        node -> {
                            try {
                                questWizard((Quiz) node.task());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
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
        wizard.addComponent(new TaskComponent(quiz, wizard));
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
