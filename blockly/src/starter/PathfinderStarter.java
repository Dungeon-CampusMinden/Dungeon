package starter;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.entities.HeroFactory;
import contrib.level.DevDungeonLoader;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.level.utils.Coordinate;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import javax.naming.OperationNotSupportedException;
import level.AiMazeLevel;
import systems.PathfindingSystem;
import utils.CheckPatternPainter;
import utils.pathfinding.BFSPathFinding;
import utils.pathfinding.DFSPathFinding;

/** This class starts the dungeon Ai level to visualize the DFS and BFS. */
public class PathfinderStarter {
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static boolean noPathfindingSelected = true;
  private static final boolean noSuSAlgorithmAvailable = true;

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
    // be able to choose between the available pathfinding algorithms
    Game.userOnFrame(PathfinderStarter::checkForAlgorithmSwitch);

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
    // Game.add(new LevelEditorSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new EventScheduler());
    Game.add(new PathfindingSystem());
  }

  /**
   * Creates and adds a new hero entity to the game.
   *
   * <p>The new hero is generated using the {@link HeroFactory} and the {@link CameraComponent} of
   * the hero is removed.
   *
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static void createHero() throws IOException {
    Entity hero;
    hero = HeroFactory.newHero();
    hero.remove(CameraComponent.class);
    Game.add(hero);
  }

  /**
   * Checks if any key for selecting a pathfinding algorithm has been pressed.
   *
   * <p>This method listens for specific key presses for DFS, BFS, and other pathfinding algorithms.
   * It switches the current algorithm accordingly and prevents further input until a new algorithm
   * is selected. If the "SuS Pathfinding" algorithm is selected but not implemented, an {@link
   * OperationNotSupportedException} is thrown to indicate the feature is not available.
   */
  private static void checkForAlgorithmSwitch() {
    try {
      Coordinate startCoords = Game.currentLevel().startTile().coordinate();
      Coordinate endTileCoords = Game.currentLevel().endTile().coordinate();
      if (noPathfindingSelected && Gdx.input.isKeyJustPressed(KeyboardConfig.SELECT_DFS.value())) {
        switchToAlgorithm(new DFSPathFinding(startCoords, endTileCoords), "DFS-Pathfinding");
      } else if (noPathfindingSelected
          && Gdx.input.isKeyJustPressed(KeyboardConfig.SELECT_BFS.value())) {
        switchToAlgorithm(new BFSPathFinding(startCoords, endTileCoords), "BFS-Pathfinding");
      } else if (noPathfindingSelected
          && Gdx.input.isKeyJustPressed(KeyboardConfig.SELECT_SUS_ALGO.value())) {
        if (noSuSAlgorithmAvailable) {
          throw new OperationNotSupportedException(
              "The SuS Pathfinding algorithm is not implemented yet.");
        }
        // switchToAlgorithm(new SuSPathFinding(startCoords, endTileCoords), "SuS-Pathfinding");
      }
    } catch (OperationNotSupportedException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Changes the pathfinding algorithm used in the game to the specified one.
   *
   * <p>This method updates the active pathfinding algorithm in the {@link PathfindingSystem} and
   * updates the game window title to reflect the newly selected algorithm.
   *
   * @param algorithm The new pathfinding algorithm to be used (e.g., DFS, BFS, SuS).
   * @param name The name of the algorithm to display in the window title for the player's
   *     reference.
   */
  private static void switchToAlgorithm(utils.pathfinding.PathfindingLogic algorithm, String name) {
    Game.system(
        PathfindingSystem.class,
        pfs -> {
          pfs.autoStep(true);
          pfs.updatePathfindingAlgorithm(algorithm);
        });
    Gdx.graphics.setTitle("Blockly KI-Dungeon – " + name);
    noPathfindingSelected = false;
  }
}
