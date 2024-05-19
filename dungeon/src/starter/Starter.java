package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.components.HealthComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import dsl.interpreter.DSLEntryPointFinder;
import dsl.interpreter.DSLInterpreter;
import entrypoint.DSLEntryPoint;
import entrypoint.DSLFileLoader;
import entrypoint.DungeonConfig;
import graph.TaskGraphConverter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import task.Task;

/**
 * Generic Game starter.Starter for a game that uses DSL inputs.
 *
 * <p>This will set up a basic game with all systems and a hero.
 *
 * <p>It reads command line arguments that are paths to DSL files or jars.
 *
 * <p>Not yet implemented: Letting the player select a starting point (essentially a level) from the
 * input DSL files and loading the game.
 *
 * <p>Start with "./gradlew start --args "dungeon/assets/scripts/task_test.dng" " or with other dsl
 * paths.
 */
public class Starter {

  private static int loadCounter = 0;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final DSLInterpreter dslInterpreter = new DSLInterpreter();

  private static boolean realGameStarted = false;
  private static long startTime = 0;
  private static final Consumer<Entity> showQuestLog =
      entity -> {
        StringBuilder questLogBuilder = new StringBuilder();
        Task.allTasks()
            .filter(t -> t.state() == Task.TaskState.PROCESSING_ACTIVE)
            .forEach(
                task ->
                    questLogBuilder
                        .append(task.taskText())
                        .append(" (name '")
                        .append(task.taskName())
                        .append("')")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator()));
        String questLog = questLogBuilder.toString();
        OkDialog.showOkDialog(questLog, "Quest log", () -> {});
      };
  private static final Consumer<Entity> showInfos =
      entity -> {
        StringBuilder infos = new StringBuilder();
        long playTime = (System.currentTimeMillis() - startTime) / 60000;
        infos
            .append("Spielzeit: ")
            .append(playTime)
            .append(" min")
            .append("          ") // for better hud scale
            .append(System.lineSeparator());

        // the task with the id=0 is the quest selector task we will not show that
        String scenarioID =
            Task.allSolvedTaskInOrder()
                .filter(task -> task.id() != 0) // Exclude task with ID 0
                .map(task -> task.taskName().substring(0, 1)) // Extract the first character
                .collect(Collectors.joining());

        Task.allSolvedTaskInOrder()
            .forEach(
                task -> {
                  if (task.id() != 0) { // Exclude task with ID 0
                    infos
                        .append(task.taskName())
                        .append(" ")
                        .append(new DecimalFormat("#.#").format(task.achievedPoints()))
                        .append(" P")
                        .append(System.lineSeparator());
                  }
                });

        String tableString = infos.toString();
        OkDialog.showOkDialog(tableString, "Szenario ID " + scenarioID, () -> {});
        // show scenario id
        // show list for task: reached points
      };

  /**
   * A method to start the main game loop and handle exceptions.
   *
   * @param args array of file names supplied on the command line
   */
  public static void main(String[] args) {
    try {
      // if file names have been supplied on CLI, let's use these
      // otherwise try to get a file name of a configuration file interactively
      String[] dslFileNames = args.length > 0 ? args : new String[] {selectSingleDngFile()};

      // read in DSL-Files
      Set<DSLEntryPoint> entryPoints = processCLIArguments(dslFileNames);

      // some game Setup
      configGame();
      // will load the level to select the task/DSL-Entrypoint on Game start
      taskSelectorOnSetup(entryPoints);

      // will generate the TaskDependencyGraph, execute the TaskBuilder, generate and set the
      // Level and generate the PetriNet after the player selected an DSLEntryPoint
      onEntryPointSelection();
      startTime = System.currentTimeMillis();

      // let's do this
      Game.run();

    } catch (Exception e) {
      // Something went wrong
      System.err.println(e.getMessage());
      System.err.println("Exiting ...");
    }
  }

  /*
   * Select a single DNG file using a JFileChooser dialog.
   *
   * @return the absolute path of the selected DNG file
   * @throws IOException if no file was selected
   */
  private static String selectSingleDngFile() throws IOException {
    AtomicReference<String> path = new AtomicReference<>(null);
    Runnable fileChooser =
        () -> {
          JFileChooser fileChooser1 = new JFileChooser();
          fileChooser1.setDialogTitle("Dungeon: Please select a .dng file");
          fileChooser1.setCurrentDirectory(new File(System.getProperty("user.dir")));
          fileChooser1.setMultiSelectionEnabled(false);
          fileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser1.setFileFilter(new FileNameExtensionFilter(".dng file", "dng"));
          fileChooser1.setAcceptAllFileFilterUsed(false);
          if (fileChooser1.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            path.set(fileChooser1.getSelectedFile().getAbsolutePath());
          }
        };

    try {
      SwingUtilities.invokeAndWait(fileChooser);
    } catch (Exception e) {
      throw new IOException(String.join(" ", "No .dng file selected.", e.getMessage()));
    }

    return path.get();
  }

  private static void onEntryPointSelection() {
    Game.userOnFrame(
        () -> {
          // the player selected a Task/DSL-Entrypoint but it´s not loaded yet:
          if (!realGameStarted && TaskSelector.selectedDSLEntryPoint != null) {
            realGameStarted = true;

            DungeonConfig config =
                dslInterpreter.interpretEntryPoint(TaskSelector.selectedDSLEntryPoint);
            ILevel level = TaskGraphConverter.convert(config.dependencyGraph(), dslInterpreter);

            Game.currentLevel(level);
          }
        });
  }

  private static void taskSelectorOnSetup(Set<DSLEntryPoint> entryPoints) {
    Game.userOnSetup(
        () -> {
          createHero();
          createSystems();
          Game.currentLevel(TaskSelector.taskSelectorLevel());
          setupMusic();
          Crafting.loadRecipes();
        });

    // load the task selector level
    Game.userOnLevelLoad(
        (firstTime) -> {
          loadCounter++;
          // this will be at the start of the game
          if (firstTime && TaskSelector.selectedDSLEntryPoint == null) {
            try {
              Game.add(TaskSelector.npc(TaskSelector.selectTaskQuestion(entryPoints)));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } else if (loadCounter == 5) {
            try {
              Game.add(EntityFactory.newCraftingCauldron());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  private static Set<DSLEntryPoint> processCLIArguments(String[] args) throws ParseException {
    Set<DSLEntryPoint> entryPoints = new HashSet<>();
    DSLEntryPointFinder finder = new DSLEntryPointFinder();
    DSLFileLoader.processArguments(args)
        .forEach(path -> finder.getEntryPoints(path).ifPresent(entryPoints::addAll)); // WTF?

    if (entryPoints.isEmpty()) throw new ParseException("No entry points found.", 0);
    else return entryPoints;
  }

  private static void createHero() {
    Entity hero;
    try {
      hero = (EntityFactory.newHero());
      hero.fetch(PlayerComponent.class)
          .flatMap(
              fetch ->
                  fetch.registerCallback(
                      KeyboardConfig.QUESTLOG.value(), showQuestLog, false, true));
      hero.fetch(PlayerComponent.class)
          .flatMap(
              fetch ->
                  fetch.registerCallback(KeyboardConfig.INFOS.value(), showInfos, false, true));
      hero.fetch(HealthComponent.class)
          .orElseThrow(() -> MissingComponentException.build(hero, HealthComponent.class))
          .godMode(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Game.windowTitle("DSL Dungeon");
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class,
        starter.KeyboardConfig.class);
  }

  private static void createSystems() {
    Game.add(new AISystem());
    Game.add(new CollisionSystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.1f);
  }
}
