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
import core.utils.IVoidFunction;
import dslToGame.DslFileLoader;
import dslToGame.DummyDSLFunctions;
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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TaskGenerationTest {
    static DSLInterpreter interpreter = new DSLInterpreter();
    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Game.hero(EntityFactory.newHero());
        LevelSystem.levelSize(LevelSize.SMALL);
        Game.loadConfig(
            "dungeon_config.json",
            contrib.configuration.KeyboardConfig.class,
            core.configuration.KeyboardConfig.class);
        Game.disableAudio(true);
        Game.userOnLevelLoad(
            new IVoidFunction() {
                @Override
                public void execute() {
                    Set<File> files = DslFileLoader.dslFiles();
                    List<String> fileContents =
                        files.stream()
                            .filter(f -> f.getName().endsWith("task_test.dng"))
                            .map(DslFileLoader::fileToString)
                            .toList();
                    /*Set<Map<String, String>> configs =
                        fileContents.stream()
                            .map(DummyDSLFunctions::getConfigs)
                            .collect(Collectors.toSet());

                    AtomicReference<String> f = new AtomicReference<>("");
                    files.forEach(v -> f.set(f.get() + v + System.lineSeparator()));
                    UITools.generateNewTextDialog(f.get(), "Ok", "Files");*/

                    // for the start: print on console
                    // TODO: call to DSLInterpreter -> result: Quiz
                    buildScenarios(fileContents.get(0));
                }
            });

        Game.windowTitle("Task Test");
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        Game.addSystem(new XPSystem());
        Game.addSystem(new ProjectileSystem());

        Game.run();
    }

    private static void buildScenarios(String dslFileContent) {
        QuestConfig config = (QuestConfig)interpreter.getQuestConfig(dslFileContent);
        for (Task task : config.tasks()) {
            try{
                questWizard((Quiz)task);
            } catch (IOException e) {
                // oh well
            }
        }
    }


    //private static void questWizard(Quiz quiz) throws IOException {
    private static void questWizard(Quiz quiz) throws IOException {
        Random random = new Random();
        String texture = "character/wizard";
        if (random.nextInt() % 2 == 0) {
            texture = "character/blue_knight";
        }

        Entity wizard = new Entity("Quest Wizard");
        new PositionComponent(wizard);
        new DrawComponent(wizard, texture);
        new TaskComponent(wizard, quiz);
        new InteractionComponent(
            wizard, 1, true, UIAnswerCallback.askOnInteraction(quiz, showAnswersOnHud()));
    }

    private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
        return (task, taskContents) -> {
            float score = task.scoringFunction().apply(task, taskContents);
            UITools.generateNewTextDialog("Your score: " + score, "Ok", "Given answer");
        };
    }

}
