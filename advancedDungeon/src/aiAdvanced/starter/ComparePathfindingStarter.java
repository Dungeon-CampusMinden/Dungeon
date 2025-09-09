package aiAdvanced.starter;

import aiAdvanced.level.AiMazeLevel;
import aiAdvanced.pathfinding.BFSPathFinding;
import aiAdvanced.pathfinding.DFSPathFinding;
import aiAdvanced.pathfinding.PathfindingLogic;
import aiAdvanced.systems.PathfindingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import contrib.entities.*;
import contrib.systems.EventScheduler;
import contrib.systems.LevelTickSystem;
import contrib.systems.PathSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.CameraSystem;
import core.systems.InputSystem;
import core.systems.LevelSystem;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * This class starts a comparator for the pathfinding algorithms.
 *
 * <p>Usage: run with the Gradle task {@code runPathFindingComparison}.
 */
public class ComparePathfindingStarter {
  private static final Entity[] RUNNERS = new Entity[2];

  private static final Class<? extends PathfindingLogic> pathFindingA = BFSPathFinding.class;
  private static final Class<? extends PathfindingLogic> pathFindingB = DFSPathFinding.class;
  private static final String GAME_TITEL = "KI_Dungeon";

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
    // build and start game
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          ((Lwjgl3Graphics) Gdx.graphics).getWindow().maximizeWindow();

          DungeonLoader.addLevel(Tuple.of("bfs", AiMazeLevel.class));
          createSystems();

          RUNNERS[0] = createHero();

          Game.system(
              LevelSystem.class, ls -> ls.onEndTile(ComparePathfindingStarter::loadNextLevel));
          Game.remove(InputSystem.class);
          loadNextLevel();

          // Wait for Level to Load
          EventScheduler.scheduleAction(ComparePathfindingStarter::adjustWindowSize, 10);
        });
  }

  private static void adjustWindowSize() {
    Tile[][] layout = Game.currentLevel().orElse(null).layout();
    int levelHeight = layout.length;

    Point topTile = layout[0][0].coordinate().translate(Vector2.of(0, 2)).toPoint();
    Point bottomTile =
        layout[levelHeight - 1][0].coordinate().translate(Vector2.of(0, 2)).toPoint();

    // Zoom out until the whole level is visible
    int currentTries = 0;
    int maxTries = 10000; // fail-safe
    while (currentTries <= maxTries) {
      if (CameraSystem.isPointInFrustum(topTile) && CameraSystem.isPointInFrustum(bottomTile)) {
        break;
      }

      CameraSystem.camera().zoom = CameraSystem.camera().zoom + 0.01f;

      // Update rendering
      Game.system(LevelSystem.class, LevelSystem::execute);
      Game.system(CameraSystem.class, CameraSystem::execute);

      currentTries++;
    }
  }

  private static void loadNextLevel() {
    DungeonLoader.loadNextLevel();
    Tile[][] curLevel = Game.currentLevel().orElse(null).layout();

    // Create duplicated level layout
    LevelElement[][] newLevel = duplicateLevelVertically(curLevel);

    // Set up new level with original and duplicated layouts
    int rows = curLevel.length;
    Coordinate orgStart = Game.startTile().orElseThrow().coordinate();
    Coordinate newStart = orgStart.translate(Vector2.of(0, rows + 1));
    Game.currentLevel(
        new AiMazeLevel(
            newLevel,
            Game.currentLevel().flatMap(ILevel::designLabel).orElse(DesignLabel.DEFAULT),
            Game.currentLevel().map(ILevel::customPoints).orElse(Collections.emptyList())));

    // Position the runners
    Debugger.TELEPORT(orgStart.toPoint());

    RUNNERS[1] = createRunnerMob(newStart.toPoint());
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

            Coordinate spawn = runner.fetch(PositionComponent.class).orElseThrow().coordinate();
            Coordinate end = Game.endTile().orElseThrow().coordinate();

            if (i == 1) {
              end = end.translate(Vector2.of(0, rows + 1));
            }

            PathfindingLogic algo =
                instancePathfindingLogic(i == 0 ? pathFindingA : pathFindingB, spawn, end);

            algorithms.add(Tuple.of(algo, runner));
          }
          pfs.updatePathfindingAlgorithm(algorithms.toArray(Tuple[]::new));
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
        GAME_TITEL + " " + pathFindingA.getSimpleName() + " vs " + pathFindingB.getSimpleName());
  }

  private static void createSystems() {
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new EventScheduler());
    Game.add(new PathfindingSystem());
  }

  /**
   * Creates and adds a new hero entity to the game.
   *
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new hero is
   * generated using the {@link HeroFactory} and the {@link CameraComponent} of the hero is removed.
   *
   * @return The newly created hero entity
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static Entity createHero() {
    Entity hero;
    try {
      hero = HeroFactory.newHero();
      hero.remove(CameraComponent.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
    return hero;
  }

  /**
   * Creates and adds a new runnerMob entity to the game. This monster looks and moves like the
   * hero.
   *
   * @param startPoint Spawn Point of the runner,
   * @return The newly created runnerMob
   */
  private static Entity createRunnerMob(Point startPoint) {
    return new MonsterBuilder<>()
        .name("KI Runner")
        .texture(new SimpleIPath("character/wizard"))
        .speed(HeroFactory.DEFAULT_HERO_CLASS.speed().x()) // same speed as hero
        .addToGame()
        .build(startPoint);
  }
}
