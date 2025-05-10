package starter;

import com.badlogic.gdx.Gdx;
import contrib.entities.HeroFactory;
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
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.utils.MissingHeroException;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.BlocklyMonster;
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
public class ComparePathfindigsStarter {
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static final Entity[] RUNNERS = new Entity[2];

  private static final Class<? extends PathfindingLogic> pathFindingA = BFSPathFinding.class;
  private static final Class<? extends PathfindingLogic> pathFindingB = DFSPathFinding.class;

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
          Gdx.graphics.setWindowedMode(
              Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() * 1.9f));

          AiMazeLevel.ZOOM_LEVEL = 0.25f;

          DevDungeonLoader.addLevel(Tuple.of("bfs", AiMazeLevel.class));
          createSystems();

          try {
            createHero();
            RUNNERS[0] = Game.hero().orElseThrow(MissingHeroException::new);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Game.system(
              LevelSystem.class, ls -> ls.onEndTile(ComparePathfindigsStarter::loadNextLevel));
          Game.remove(PlayerSystem.class);
          loadNextLevel();
        });
  }

  private static void loadNextLevel() {
    DevDungeonLoader.loadNextLevel();
    Tile[][] curLevel = Game.currentLevel().layout();
    // copy the same layout below the current level with Wall Tiles between
    LevelElement[][] newLevel = new LevelElement[curLevel.length * 2 + 1][curLevel[0].length];
    for (int i = 0; i < curLevel.length; i++) {
      for (int j = 0; j < curLevel[i].length; j++) {
        LevelElement newElement = curLevel[i][j].levelElement();
        newLevel[i][j] = newElement;
        newLevel[i + curLevel.length + 1][j] = newElement;
      }
    }
    for (int i = 0; i < newLevel.length; i++) {
      for (int j = 0; j < newLevel[i].length; j++) {
        if (newLevel[i][j] == null) {
          newLevel[i][j] = LevelElement.WALL;
        }
      }
    }
    Coordinate orgStart = Game.startTile().coordinate();
    Coordinate newStart = orgStart.add(new Coordinate(0, curLevel.length + 1));
    Game.currentLevel(
        new AiMazeLevel(
            newLevel,
            Game.currentLevel().startTile().designLabel(),
            ((DevDungeonLevel) Game.currentLevel()).customPoints()));
    Debugger.TELEPORT(orgStart.toCenteredPoint());
    RUNNERS[1] =
        BlocklyMonster.RUNNER
            .builder()
            .spawnPoint(newStart.toCenteredPoint())
            .addToGame()
            .build()
            .get();

    Game.system(
        PathfindingSystem.class,
        (pfs) -> {
          pfs.autoStep(true);

          List<Tuple<PathfindingLogic, Entity>> pathfindingAlgorithms = new ArrayList<>();
          for (int i = 0; i < RUNNERS.length; i++) {
            Entity runner = RUNNERS[i];
            if (runner == null) {
              continue;
            }
            Coordinate spawn =
                runner.fetch(PositionComponent.class).get().position().toCoordinate();
            Coordinate end = Game.currentLevel().endTile().coordinate();
            PathfindingLogic algo;
            if (i == 0) {
              algo = instancePathfindingLogic(pathFindingA, spawn, end);
            } else {
              algo =
                  instancePathfindingLogic(
                      pathFindingB, spawn, end.add(new Coordinate(0, curLevel.length + 1)));
            }

            pathfindingAlgorithms.add(Tuple.of(algo, runner));
          }
          pfs.updatePathfindingAlgorithm(pathfindingAlgorithms.toArray(new Tuple[0]));
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
}
