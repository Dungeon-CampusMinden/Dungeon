package manual.taskgeneration;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.TextDialog;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.LevelSize;
import core.systems.LevelSystem;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import dsl.interpreter.DSLInterpreter;
import entrypoint.DSLFileLoader;
import entrypoint.DungeonConfig;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import task.Task;
import task.TaskContent;
import task.game.components.TaskComponent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;

/**
 * <a
 * href="file:../../../../doc/tasks/anleitung_task_test.md">../../../../doc/tasks/anleitung_task_test.md</a>
 *
 * <p>Use taskGenerationTest --args "dungeon/test_resources/task_test.dng" to run thus.
 */
public class TaskGenerationTest {
  static DSLInterpreter interpreter = new DSLInterpreter();

  /**
   * Generate the main game setup including initializing systems, loading configurations, adding
   * entities, and running the game.
   *
   * @param args the command-line arguments passed to the game
   * @throws IOException if an IO error occurs during the setup
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.ALL);
    LevelSystem.levelSize(LevelSize.MEDIUM);
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);

    Game.userOnSetup(
        () -> {
          try {
            Game.add(new AISystem());
            Game.add(new CollisionSystem());
            Game.add(new HealthSystem());
            Game.add(new ProjectileSystem());
            Game.add(new HealthBarSystem());
            Game.add(new HudSystem());
            Entity hero = EntityFactory.newHero();
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
              // oh well // WTF?
            }

            Set<Path> dslFilePaths = DSLFileLoader.processArguments(args);

            List<String> fileContents =
                dslFilePaths.stream().map(DSLFileLoader::fileToString).toList();
            buildScenarios(fileContents.get(0));
          }
        });

    Game.windowTitle("Task Test");
    Game.run();
  }

  private static void buildScenarios(String dslFileContent) {
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(dslFileContent);
    config
        .dependencyGraph()
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
    IPath texture = new SimpleIPath("character/wizard");
    if (random.nextInt() % 2 == 0) {
      texture = new SimpleIPath("character/blue_knight");
    }

    Entity wizard = new Entity("Quest Wizard");
    wizard.add(new PositionComponent());
    wizard.add(new DrawComponent(texture));
    wizard.add(new TaskComponent(quiz, wizard));
    wizard.add(
        new InteractionComponent(
            1, true, UIAnswerCallback.askOnInteraction(quiz, showAnswersOnHud())));
    Game.add(wizard);
  }

  private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
    return (task, taskContents) -> {
      float score = task.scoringFunction().apply(task, taskContents);
      TextDialog.textDialog("Your score: " + score, "Ok", "Given answer");
    };
  }
}
