package client;

import coderunner.BlocklyCodeRunner;
import com.sun.net.httpserver.HttpServer;
import components.AmmunitionComponent;
import contrib.crafting.Crafting;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.loader.DungeonLoader;
import core.systems.PositionSystem;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import entities.HeroTankControlledFactory;
import java.io.IOException;
import java.util.Set;
import level.produs.*;
import server.Server;
import systems.BlocklyCommandExecuteSystem;
import systems.TintTilesSystem;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 *
 * <p>Usage: run with the Gradle task {@code runBlockly}.
 *
 * <p>For the Web-Ui you have to start the frontend yourself.
 */
public class Client {

  /** The name of the blockly player. */
  public static final String WIZARD_NAME = "Algorim";

  /** Force to apply for movement of all entities. */
  public static final Vector2 MOVEMENT_FORCE = Vector2.of(7.5, 7.5);

  private static final boolean DEBUG_MODE = false;
  private static final boolean ACTIVATE_TANKE_CONTROLLS = DEBUG_MODE;
  private static volatile boolean scheduleRestart = false;

  private static HttpServer httpServer;

  /**
   * If true, the Web interface Blockly is used for interaction with the Dunogen. Otherwise, the
   * Code API is used.
   */
  public static boolean runInWeb = false;

  /**
   * Setup and run the game. Also start the server that is listening to the requests from blockly
   * frontend.
   *
   * @param args CLI arguments
   * @throws IOException if textures can not be loaded.
   */
  public static void main(String[] args) throws IOException {
    for (String arg : args) {
      if (arg.equalsIgnoreCase("web=true")) {
        runInWeb = true;
      }
    }

    StateMachine.setResetFrame(false);
    Debugger debugger = new Debugger();
    // start the game
    configGame();
    // Set up components and level
    onSetup();

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

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          // chapter 1
          DungeonLoader.addLevel(Tuple.of("level001", Level001.class));
          DungeonLoader.addLevel(Tuple.of("level002", Level002.class));
          DungeonLoader.addLevel(Tuple.of("level003", Level003.class));
          DungeonLoader.addLevel(Tuple.of("level004", Level004.class));
          DungeonLoader.addLevel(Tuple.of("level005", Level005.class));
          DungeonLoader.addLevel(Tuple.of("level006", Level006.class));
          DungeonLoader.addLevel(Tuple.of("level007", Level007.class));
          DungeonLoader.addLevel(Tuple.of("level008", Level008.class));
          DungeonLoader.addLevel(Tuple.of("level009", Level009.class));
          DungeonLoader.addLevel(Tuple.of("level010", Level010.class));
          DungeonLoader.addLevel(Tuple.of("level011", Level011.class));
          DungeonLoader.addLevel(Tuple.of("level012", Level012.class));

          // chapter 2
          DungeonLoader.addLevel(Tuple.of("level013", Level013.class));
          DungeonLoader.addLevel(Tuple.of("level014", Level014.class));
          DungeonLoader.addLevel(Tuple.of("level015", Level015.class));
          DungeonLoader.addLevel(Tuple.of("level016", Level016.class));
          DungeonLoader.addLevel(Tuple.of("level017", Level017.class));

          // chapter 3
          DungeonLoader.addLevel(Tuple.of("level018", Level018.class));
          DungeonLoader.addLevel(Tuple.of("level019", Level019.class));
          DungeonLoader.addLevel(Tuple.of("level020", Level020.class));
          DungeonLoader.addLevel(Tuple.of("level021", Level021.class));
          DungeonLoader.addLevel(Tuple.of("level022", Level022.class));

          createHero();
          createSystems();
          Crafting.loadRecipes();

          startServer();

          Crafting.loadRecipes();

          DungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          BlocklyCodeRunner.instance().stopCode();
          Game.system(
              BlocklyCommandExecuteSystem.class,
              s -> {
                // stopping the system will also avoid adding new commands to the queue. The System
                // will be reactivated in BlocklyLevel#onTick
                s.fullStop();
                s.clear();
              });
          Game.player()
              .flatMap(e -> e.fetch(VelocityComponent.class))
              .ifPresent(
                  vc -> {
                    vc.clearForces();
                    vc.currentVelocity(Vector2.ZERO);
                  });
          Game.player()
              .flatMap(e -> e.fetch(AmmunitionComponent.class))
              .map(AmmunitionComponent::resetCurrentAmmunition);
        });
    // this makes sure a outsynced command will not replace the player and the player will always be
    // on
    // the starttile of the level
    Game.player()
        .flatMap(e -> e.fetch(PositionComponent.class))
        .ifPresent(
            positionComponent ->
                Game.startTile().ifPresent(tile -> positionComponent.position(tile.position())));
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
    EventScheduler.setPausable(false);
    Game.add(new FogSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new BlocklyCommandExecuteSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
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
    if (DEBUG_MODE) {
      Game.add(new DebugDrawSystem());
      Game.add(new LevelEditorSystem());
    }
  }

  private static void startServer() {
    try {
      httpServer = Server.instance().start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  /**
   * Creates and adds a new player entity to the game.
   *
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new player
   * is generated using the {@link HeroTankControlledFactory} and is equipped with an {@link
   * AmmunitionComponent}.
   *
   * @throws RuntimeException if an {@link IOException} occurs during player creation
   */
  public static void createHero() {
    Game.levelEntities(Set.of(PlayerComponent.class)).forEach(Game::remove);
    Entity hero = HeroTankControlledFactory.blocklyHero(ACTIVATE_TANKE_CONTROLLS);
    hero.add(new AmmunitionComponent());
    Game.add(hero);
  }

  /**
   * Restarts the game by removing all entities, recreating the player, and reloading the current
   * level.
   *
   * <p>If this method is called from a thread other than the main thread, the restart is scheduled
   * to occur on the next game tick. This is to ensure thread safety and prevent race conditions.
   *
   * <p>During the restart, the {@link PositionSystem} is stopped and then run again to ensure that
   * the player is placed correctly in the level. This prevents a race condition where the player
   * might be placed before the level is fully loaded.
   */
  public static void restart() {
    // if not the main thread, schedule restart
    if (!Thread.currentThread().getName().equals("main")) {
      scheduleRestart = true;
      Server.waitDelta(); // wait for the next tick to execute the restart
      return;
    }
    BlocklyCodeRunner.instance().stopCode();
    Game.removeAllEntities();
    Game.system(PositionSystem.class, System::stop);
    Game.system(BlocklyCommandExecuteSystem.class, s -> s.clear());
    createHero();
    DungeonLoader.reloadCurrentLevel();
    Game.system(PositionSystem.class, System::run);
  }
}
