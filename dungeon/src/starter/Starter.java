package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import contrib.components.HealthComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.hud.OkDialog;
import contrib.systems.*;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import core.utils.components.MissingComponentException;

import dsl.interpreter.DSLEntryPointFinder;
import dsl.interpreter.DSLInterpreter;

import entrypoint.DSLEntryPoint;
import entrypoint.DSLFileLoader;
import entrypoint.DungeonConfig;

import graph.TaskGraphConverter;

import task.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
                OkDialog.showOkDialog(questLog, "Questlog", () -> {});
            };
    private static final Consumer<Entity> showInfos =
            entity -> {
                StringBuilder infos = new StringBuilder();
                long playTime = (System.currentTimeMillis() - startTime) / 60000;
                infos.append("Spielzeit: ")
                        .append(playTime)
                        .append(" min")
                        .append("          ") // for better hud scale
                        .append(System.lineSeparator());

                // the task with the id=0 is the quest selector task we will not show that
                String scenarioID =
                        Task.allSolvedTaskInOrder()
                                .filter(task -> task.id() != 0) // Exclude task with ID 0
                                .map(
                                        task ->
                                                task.taskName()
                                                        .substring(
                                                                0,
                                                                1)) // Extract the first character
                                .collect(Collectors.joining());

                Task.allSolvedTaskInOrder()
                        .forEach(
                                task -> {
                                    if (task.id() != 0) { // Exclude task with ID 0
                                        infos.append(task.taskName())
                                                .append(" ")
                                                .append(
                                                        new DecimalFormat("#.#")
                                                                .format(task.achievedPoints()))
                                                .append(" P")
                                                .append(System.lineSeparator());
                                    }
                                });

                String tableString = infos.toString();
                OkDialog.showOkDialog(tableString, "Szenario ID " + scenarioID, () -> {});
                // show scenario id
                // show list for task: reached points
            };

    public static void main(String[] args) throws IOException {
        // read in DSL-Files
        Set<DSLEntryPoint> entryPoints = processCLIArguments(args);

        // some game Setup
        configGame();
        // will load the level to select the task/DSL-Entrypoint on Game start
        taskSelectorOnSetup(entryPoints);

        // will generate the TaskDependencyGraph, execute the TaskBuilder, generate and set the
        // Level and generate the PetriNet after the player selected an DSLEntryPoint
        onEntryPointSelection();
        startTime = System.currentTimeMillis();
        Game.run();
    }

    private static void onEntryPointSelection() {
        Game.userOnFrame(
                () -> {
                    // the player selected a Task/DSL-Entrypoint but it´s not loaded yet:
                    if (!realGameStarted && TaskSelector.selectedDSLEntryPoint != null) {
                        realGameStarted = true;

                        DungeonConfig config =
                                dslInterpreter.interpretEntryPoint(
                                        TaskSelector.selectedDSLEntryPoint);
                        ILevel level =
                                TaskGraphConverter.convert(
                                        config.dependencyGraph(), dslInterpreter);

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
                });

        // load the task selector level
        Game.userOnLevelLoad(
                (firstTime) -> {
                    loadCounter++;
                    // this will be at the start of the game
                    if (firstTime && TaskSelector.selectedDSLEntryPoint == null) {
                        try {
                            Game.add(
                                    TaskSelector.npc(TaskSelector.selectTaskQuestion(entryPoints)));
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

    private static Set<DSLEntryPoint> processCLIArguments(String[] args) throws IOException {
        Set<DSLEntryPoint> entryPoints = new HashSet<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        DSLFileLoader.processArguments(args)
                .forEach(path -> finder.getEntryPoints(path).ifPresent(entryPoints::addAll));
        return entryPoints;
    }

    private static void createHero() {
        Entity hero;
        try {
            hero = (EntityFactory.newHero());
            hero.fetch(PlayerComponent.class)
                    .flatMap(
                            fetch ->
                                    fetch.registerCallback(
                                            KeyboardConfig.QUESTLOG.value(),
                                            showQuestLog,
                                            false,
                                            true));
            hero.fetch(PlayerComponent.class)
                    .flatMap(
                            fetch ->
                                    fetch.registerCallback(
                                            KeyboardConfig.INFOS.value(), showInfos, false, true));
            hero.fetch(HealthComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(hero, HealthComponent.class))
                    .godMode(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.add(hero);
        Game.hero(hero);
    }

    private static void configGame() throws IOException {
        Game.initBaseLogger();
        Game.windowTitle("DSL Dungeon");
        Game.frameRate(30);
        Game.disableAudio(false);
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Crafting.loadRecipes();
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
