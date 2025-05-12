package starter;

import client.KeyboardConfig;
import contrib.entities.HeroFactory;
import contrib.level.DevDungeonLoader;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PlayerComponent;
import core.level.utils.Coordinate;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import level.AiMazeLevel;
import systems.PathfindingSystem;
import utils.CheckPatternPainter;
import utils.pathfinding.BFSPathFinding;
import utils.pathfinding.DFSPathFinding;
import utils.pathfinding.PathfindingLogic;
import utils.pathfinding.SusPathFinding;

/** This class starts the dungeon Ai level to visualize the DFS and BFS. */
public class PathfinderStarter {
  private static final Logger LOGGER = Logger.getLogger(PathfinderStarter.class.getName());
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static Tuple<PathfindingLogic, PathfindingLogic> pathfindings = Tuple.of(null, null);

  /**
   * Setup and run the game. Also start the server that is listening to the requests from blockly
   * frontend.
   *
   * @param args
   * @throws IOException
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
          DevDungeonLoader.addLevel(Tuple.of("dfs", AiMazeLevel.class));
          createSystems();

          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Game.system(LevelSystem.class, ls -> ls.onEndTile(DevDungeonLoader::loadNextLevel));
          DevDungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().layout());

          Coordinate startCoords = Game.currentLevel().startTile().coordinate();
          Coordinate endTileCoords = Game.currentLevel().endTile().coordinate();
          PlayerComponent pc =
              Game.hero()
                  .get()
                  .fetch(PlayerComponent.class)
                  .orElseThrow(
                      () ->
                          MissingComponentException.build(
                              Game.hero().get(), PlayerComponent.class));
          pc.registerCallback(
              KeyboardConfig.SELECT_DFS.value(),
              entity -> {
                if (pathfindings.a() == null && pathfindings.b() == null) {
                  pathfindings = Tuple.of(new DFSPathFinding(startCoords, endTileCoords), null);
                  switchToAlgorithm(pathfindings.a());
                }
              });
          pc.registerCallback(
              KeyboardConfig.SELECT_BFS.value(),
              entity -> {
                if (pathfindings.a() == null && pathfindings.b() == null) {
                  pathfindings = Tuple.of(new BFSPathFinding(startCoords, endTileCoords), null);
                  switchToAlgorithm(pathfindings.a());
                }
              });
          pc.registerCallback(
              KeyboardConfig.SELECT_SUS_ALGO.value(),
              entity -> {
                if (pathfindings.a() == null && pathfindings.b() == null) {
                  try {
                    pathfindings = Tuple.of(null, new SusPathFinding(startCoords, endTileCoords));
                    switchToAlgorithm(pathfindings.b());
                  } catch (UnsupportedOperationException e) {
                    LOGGER.info(e.getMessage());
                    pathfindings = Tuple.of(null, null);
                  }
                }
              });
        });
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.windowTitle("Blockly KI-Dungeon");
  }

  private static void createSystems() {
    Game.add(new LevelEditorSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new EventScheduler());
    Game.add(new PathfindingSystem());
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

    hero.remove(PlayerComponent.class);
    hero.add(new PlayerComponent());
  }

  /**
   * Changes the pathfinding algorithm used in the game to the specified one.
   *
   * <p>This method updates the active pathfinding algorithm in the {@link PathfindingSystem} and
   * updates the game window title to reflect the newly selected algorithm.
   *
   * @param algorithm The new pathfinding algorithm to be used (e.g., DFS, BFS, SuS).
   */
  private static void switchToAlgorithm(PathfindingLogic algorithm) {
    Game.system(
        PathfindingSystem.class,
        pfs -> {
          pfs.autoStep(true);
          pfs.updatePathfindingAlgorithm(algorithm);
        });
    Game.updateWindowTitle("Blockly KI-Dungeon – " + algorithm.getClass().getSimpleName());
  }
}
