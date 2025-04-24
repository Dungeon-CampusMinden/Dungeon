package starter;

import com.sun.net.httpserver.HttpServer;
import components.AmmunitionComponent;
import contrib.crafting.Crafting;
import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.level.DevDungeonLoader;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.systems.CameraSystem;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.HeroTankControlledFactory;
import level.AiMazeLevel;
import level.MazeLevel;
import server.Server;
import systems.BlockSystem;
import systems.LevelEditorSystem;
import systems.PathfindingSystem;
import systems.TintTilesSystem;
import utils.CheckPatternPainter;
import utils.pathfinding.BFSPathFinding;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 */
public class ClientKiStarter {
  private static final boolean DRAW_CHECKER_PATTERN = true;

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
          DevDungeonLoader.addLevel(Tuple.of("kilevel", AiMazeLevel.class));
          createSystems();

          createHero();

          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DevDungeonLoader::loadNextLevel);

          DevDungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().layout());

          PathfindingSystem pathfindingSystem =
            (PathfindingSystem) ECSManagment.systems().get(PathfindingSystem.class);
          if (pathfindingSystem != null) {
            pathfindingSystem.autoStep(true);
            pathfindingSystem.updatePathfindingAlgorithm(
              new BFSPathFinding(),
              Game.currentLevel().startTile().coordinate(),
              Game.currentLevel().endTile().coordinate());
          }
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
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new hero is
   * generated using the {@link HeroTankControlledFactory} and is equipped with an {@link
   * AmmunitionComponent}.
   *
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static void createHero() {
    Entity hero;
    try {
      hero = HeroFactory.newHero();
      hero.remove(CameraComponent.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }
}
