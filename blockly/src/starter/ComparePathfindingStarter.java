package starter;

import com.badlogic.gdx.Gdx;
import contrib.level.DevDungeonLevel;
import contrib.level.DevDungeonLoader;
import contrib.systems.EventScheduler;
import contrib.systems.LevelEditorSystem;
import contrib.systems.LevelTickSystem;
import contrib.systems.PathSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.BlocklyMonster;
import entities.HeroTankControlledFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import level.AiMazeLevel;
import systems.PathfindingSystem;
import utils.CheckPatternPainter;
import utils.pathfinding.BFSPathFinding;
import utils.pathfinding.DFSPathFinding;
import utils.pathfinding.PathfindingLogic;

/** This class starts a comparator for the pathfinding algorithms. */
public class ComparePathfindingStarter {
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static final Entity[] RUNNERS = new Entity[2];

  private static final Class<? extends PathfindingLogic> pathFindingA = BFSPathFinding.class;
  private static final Class<? extends PathfindingLogic> pathFindingB = DFSPathFinding.class;

  /**
   * Starts the game and sets up the comparator for the pathfinding algorithms.
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
          Gdx.graphics.setWindowedMode(
              Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() * 1.9f));

          AiMazeLevel.ZOOM_LEVEL = 0.25f;

          DevDungeonLoader.addLevel(Tuple.of("bfs", AiMazeLevel.class));
          createSystems();

          RUNNERS[0] = createHero();

          Game.system(
              LevelSystem.class, ls -> ls.onEndTile(ComparePathfindingStarter::loadNextLevel));
          Game.remove(PlayerSystem.class);
          loadNextLevel();
        });
  }

  private static void loadNextLevel() {
    DevDungeonLoader.loadNextLevel();
    Tile[][] curLevel = Game.currentLevel().layout();

    // Create duplicated level layout
    LevelElement[][] newLevel = duplicateLevelVertically(curLevel);

    // Set up new level with original and duplicated layouts
    int rows = curLevel.length;
    Coordinate orgStart = Game.startTile().coordinate();
    Coordinate newStart = orgStart.add(new Coordinate(0, rows + 1));
    Game.currentLevel(
        new AiMazeLevel(
            newLevel,
            Game.currentLevel().startTile().designLabel(),
            ((DevDungeonLevel) Game.currentLevel()).customPoints()));

    // Position the runners
    Debugger.TELEPORT(orgStart.toCenteredPoint());
    RUNNERS[1] =
        BlocklyMonster.RUNNER
            .builder()
            .spawnPoint(newStart.toCenteredPoint())
            .addToGame()
            .build()
            .orElseThrow();

    setupPathFindingSystem(rows);
  }

  /**
   * Creates a new level with the original layout duplicated vertically below.
   *
   * @param curLevel The current level layout
   * @return The new duplicated level layout
   */
  private static LevelElement[][] duplicateLevelVertically(Tile[][] curLevel) {
    int rows = curLevel.length;
    int cols = curLevel[0].length;
    LevelElement[][] newLevel = new LevelElement[rows * 2 + 1][cols];

    // Copy current level layout and duplicate it below
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        LevelElement element = curLevel[i][j].levelElement();
        newLevel[i][j] = element;
        newLevel[i + rows + 1][j] = element;
      }
    }

    // Fill gaps with walls
    for (int i = 0; i < newLevel.length; i++) {
      for (int j = 0; j < newLevel[i].length; j++) {
        if (newLevel[i][j] == null) {
          newLevel[i][j] = LevelElement.WALL;
        }
      }
    }

    return newLevel;
  }

  /**
   * Sets up the pathfinding system for both runners.
   *
   * @param rows The number of rows in the original level (used for offset calculation)
   */
  private static void setupPathFindingSystem(int rows) {
    Game.system(
        PathfindingSystem.class,
        (pfs) -> {
          pfs.autoStep(true);

          List<Tuple<PathfindingLogic, Entity>> algorithms = new ArrayList<>();
          for (int i = 0; i < RUNNERS.length; i++) {
            Entity runner = RUNNERS[i];
            if (runner == null) continue;

            Coordinate spawn =
                runner.fetch(PositionComponent.class).orElseThrow().position().toCoordinate();
            Coordinate end = Game.currentLevel().endTile().coordinate();

            if (i == 1) {
              end = end.add(new Coordinate(0, rows + 1));
            }

            PathfindingLogic algo =
                instancePathfindingLogic(i == 0 ? pathFindingA : pathFindingB, spawn, end);

            algorithms.add(Tuple.of(algo, runner));
          }
          pfs.updatePathfindingAlgorithm(algorithms.toArray(Tuple[]::new));
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().layout());
        });
  }

  private static PathfindingLogic instancePathfindingLogic(
      Class<? extends PathfindingLogic> clazz, Coordinate start, Coordinate end) {
    try {
      return clazz.getConstructor(Coordinate.class, Coordinate.class).newInstance(start, end);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of PathfindingLogic", e);
    }
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.windowTitle(
        "Blockly " + pathFindingA.getSimpleName() + " vs " + pathFindingB.getSimpleName());
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
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new hero is
   * generated using the {@link HeroTankControlledFactory} and the {@link CameraComponent} of the
   * hero is removed.
   *
   * @return The newly created hero entity
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static Entity createHero() {
    Entity hero;
    try {
      hero = HeroTankControlledFactory.newTankControlledHero();
      hero.remove(CameraComponent.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
    return hero;
  }
}
