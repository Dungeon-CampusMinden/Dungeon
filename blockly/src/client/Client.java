package client;

import com.sun.net.httpserver.HttpServer;
import components.AmmunitionComponent;
import contrib.crafting.Crafting;
import contrib.entities.HeroFactory;
import contrib.level.DevDungeonLoader;
import contrib.systems.*;
import contrib.systems.BlockSystem;
import contrib.utils.CheckPatternPainter;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.systems.PositionSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.HeroTankControlledFactory;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import level.produs.*;
import server.Server;
import systems.TintTilesSystem;
import utils.BlocklyCodeRunner;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 */
public class Client {

  private static final boolean DEBUG_MODE = false;
  private static final boolean KEYBOARD_DEACTIVATION = !DEBUG_MODE;
  private static final boolean DRAW_CHECKER_PATTERN = true;
  private static volatile boolean scheduleRestart = false;

  private static HttpServer httpServer;

  /**
   * Setup and run the game. Also start the server that is listening to the requests from blockly
   * frontend.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    // start the game
    configGame();
    // Set up components and level
    onSetup();

    if (DEBUG_MODE) onFrame(debugger);

    onLevelLoad();

    // build and start game
    try {
      Game.run();
    } finally {
      // Ensure that the server is stopped when the game ends
      if (httpServer != null) {
        httpServer.stop(0);
      }
    }
  }

  private static void onFrame(Debugger debugger) {
    Game.userOnFrame(debugger::execute);
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          // chapter 1
          DevDungeonLoader.addLevel(Tuple.of("level1", Level1.class));
          DevDungeonLoader.addLevel(Tuple.of("level2", Level2.class));
          DevDungeonLoader.addLevel(Tuple.of("level3", Level3.class));
          DevDungeonLoader.addLevel(Tuple.of("level4", Level4.class));
          DevDungeonLoader.addLevel(Tuple.of("level5", Level5.class));
          DevDungeonLoader.addLevel(Tuple.of("level6", Level6.class));
          DevDungeonLoader.addLevel(Tuple.of("level7", Level7.class));
          DevDungeonLoader.addLevel(Tuple.of("level8", Level8.class));
          DevDungeonLoader.addLevel(Tuple.of("level9", Level9.class));
          DevDungeonLoader.addLevel(Tuple.of("level10", Level10.class));
          DevDungeonLoader.addLevel(Tuple.of("level11", Level11.class));

          // chapter 2
          DevDungeonLoader.addLevel(Tuple.of("level12", Level12.class));
          DevDungeonLoader.addLevel(Tuple.of("level13", Level13.class));
          DevDungeonLoader.addLevel(Tuple.of("level14", Level14.class));
          DevDungeonLoader.addLevel(Tuple.of("level15", Level15.class));
          DevDungeonLoader.addLevel(Tuple.of("level16", Level16.class));

          // chapter 3
          DevDungeonLoader.addLevel(Tuple.of("level17", Level17.class));
          DevDungeonLoader.addLevel(Tuple.of("level18", Level18.class));
          DevDungeonLoader.addLevel(Tuple.of("level19", Level19.class));
          DevDungeonLoader.addLevel(Tuple.of("level20", Level20.class));
          DevDungeonLoader.addLevel(Tuple.of("level21", Level21.class));
          DevDungeonLoader.addLevel(Tuple.of("level22", Level22.class));

          createSystems();

          HeroFactory.heroDeath(
              entity -> {
                restart();
              });

          createHero();
          Crafting.loadRecipes();

          startServer();

          Crafting.loadRecipes();

          if (KEYBOARD_DEACTIVATION) {
            Game.remove(PlayerSystem.class);
          }

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
          BlocklyCodeRunner.instance().stopCode();
          Game.hero()
              .flatMap(e -> e.fetch(AmmunitionComponent.class))
              .map(AmmunitionComponent::resetCurrentAmmunition);
        });
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.resizeable(true);
    Game.windowTitle("Blockly Dungeon");
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new TintTilesSystem());
    Game.add(new EventScheduler());
    Game.add(new FogSystem());
    Game.add(
        new System() {
          @Override
          public void execute() {
            if (scheduleRestart) {
              scheduleRestart = false;
              restart();
            }
          }
        });
  }

  private static void startServer() {
    try {
      httpServer = Server.instance().start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
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
    Game.entityStream(Set.of(PlayerComponent.class)).forEach(e -> Game.remove(e));
    Entity hero;
    try {
      hero = HeroTankControlledFactory.newTankControlledHero();
      hero.add(new AmmunitionComponent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  /**
   * Restarts the game by removing all entities, recreating the hero, and reloading the current
   * level.
   *
   * <p>If this method is called from a thread other than the main thread, the restart is scheduled
   * to occur on the next game tick. This is to ensure thread safety and prevent race conditions.
   *
   * <p>During the restart, the {@link PositionSystem} is stopped and then run again to ensure that
   * the hero is placed correctly in the level. This prevents a race condition where the hero might
   * be placed before the level is fully loaded.
   */
  public static void restart() {

    // if not the main thread, schedule restart
    if (!Thread.currentThread().getName().equals("main")) {
      scheduleRestart = true;
      Server.waitDelta(); // wait for the next tick to execute the restart
      return;
    }

    Game.removeAllEntities();
    Game.system(PositionSystem.class, System::stop);
    createHero();
    DevDungeonLoader.reloadCurrentLevel();
    Game.system(PositionSystem.class, System::run);
  }
}
