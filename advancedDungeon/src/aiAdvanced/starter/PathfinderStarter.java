package aiAdvanced.starter;

import aiAdvanced.level.AiMazeLevel;
import aiAdvanced.pathfinding.BFSPathFinding;
import aiAdvanced.pathfinding.DFSPathFinding;
import aiAdvanced.pathfinding.PathfindingLogic;
import aiAdvanced.pathfinding.SusPathFinding;
import aiAdvanced.systems.MazeEditorSystem;
import aiAdvanced.systems.PathfindingSystem;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.InputComponent;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class starts the dungeon Ai level to visualize the DFS and BFS.
 *
 * <p>Usage: run with the Gradle task {@code runPathfinder}.
 */
public class PathfinderStarter {
  private static final Logger LOGGER = Logger.getLogger(PathfinderStarter.class.getName());
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static Tuple<PathfindingLogic, PathfindingLogic> pathfindings = Tuple.of(null, null);
  private static final PathfindingSystem pathfindingSystem = new PathfindingSystem();
  private static final String GAME_TITEL = "KI_Dungeon";

  /**
   * This method sets up and runs the game. It allows the user to choose between various pathfinding
   * algorithms and observe how they navigate through the maze.
   *
   * @param args The command line arguments.
   * @throws IOException If an error occurs while loading.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);

    // start the game
    configGame();
    // Set up components and level
    onSetup();

    onLevelLoad();

    // build and start game
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(
              Tuple.of("dfs", AiMazeLevel.class), Tuple.of("bfs", AiMazeLevel.class));
          createSystems();

          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Game.system(LevelSystem.class, ls -> ls.onEndTile(DungeonLoader::loadNextLevel));
          DungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().orElse(null).layout());

          // Rest pathfinding algos
          pathfindingSystem.reset();
          pathfindings = Tuple.of(null, null);
          Game.updateWindowTitle(GAME_TITEL + " – No Algorithm selected");

          Coordinate startCoords = Game.startTile().orElseThrow().coordinate();
          Coordinate endTileCoords = Game.endTile().orElseThrow().coordinate();

          Game.hero()
              .ifPresent(
                  hero -> {
                    hero.fetch(InputComponent.class)
                        .ifPresent(
                            ic -> {
                              ic.registerCallback(
                                  KeyboardConfig.SELECT_DFS.value(),
                                  caller -> {
                                    selectPathfindingAlgorithm(
                                        new DFSPathFinding(startCoords, endTileCoords),
                                        false,
                                        hero);
                                  });
                              ic.registerCallback(
                                  KeyboardConfig.SELECT_BFS.value(),
                                  caller -> {
                                    selectPathfindingAlgorithm(
                                        new BFSPathFinding(startCoords, endTileCoords),
                                        false,
                                        hero);
                                  });
                              ic.registerCallback(
                                  KeyboardConfig.SELECT_SUS_ALGO.value(),
                                  caller -> {
                                    selectPathfindingAlgorithm(
                                        new SusPathFinding(startCoords, endTileCoords), true, hero);
                                  });
                            });
                  });
        });
  }

  private static void selectPathfindingAlgorithm(
      PathfindingLogic pathfinding, boolean studyAlgo, Entity hero) {
    if (pathfindingSystem.isEveryAlgorithmRunning() || pathfindingSystem.isEveryAlgorithmFinished())
      return;

    if (studyAlgo) {
      pathfindings = Tuple.of(null, pathfinding);
    } else {
      pathfindings = Tuple.of(pathfinding, null);
    }
    try {
      switchToAlgorithm(pathfinding, hero);
    } catch (UnsupportedOperationException e) {
      LOGGER.info(e.getMessage());
      pathfindings = Tuple.of(null, null);
    }
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.windowTitle(GAME_TITEL);
  }

  private static void createSystems() {
    Game.add(new MazeEditorSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new EventScheduler());
    Game.add(pathfindingSystem);
  }

  /**
   * Creates and adds a new hero entity to the game.
   *
   * <p>The new hero is generated using the {@link HeroFactory} and the {@link CameraComponent} of
   * the hero is removed. And movement callbacks are removed.
   *
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static void createHero() throws IOException {
    Entity hero;
    hero = HeroFactory.newHero();
    hero.remove(CameraComponent.class);
    Game.add(hero);

    hero.fetch(InputComponent.class).ifPresent(InputComponent::removeCallbacks);
  }

  /**
   * Changes the pathfinding algorithm used in the game to the specified one.
   *
   * <p>This method updates the active pathfinding algorithm in the {@link PathfindingSystem} and
   * updates the game window title to reflect the newly selected algorithm.
   *
   * @param algorithm The new pathfinding algorithm to be used (e.g., DFS, BFS, SuS).
   * @param hero The entity using the algorithm
   */
  private static void switchToAlgorithm(PathfindingLogic algorithm, Entity hero) {
    Game.system(
        PathfindingSystem.class,
        pfs -> {
          pfs.autoStep(true);
          pfs.updatePathfindingAlgorithm(Tuple.of(algorithm, hero));
        });
    Game.updateWindowTitle(GAME_TITEL + " – " + algorithm.getClass().getSimpleName());
  }
}
