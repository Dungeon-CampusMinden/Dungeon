package client;

import blockly.dgir.vm.dialect.dg.DgActionGateway;
import blockly.dgir.vm.dialect.dg.DungeonDialectRunner;
import coderunner.BlocklyCodeRunner;
import coderunner.DgHeroActionGateway;
import com.badlogic.gdx.Gdx;
import com.sun.net.httpserver.HttpServer;
import components.AmmunitionComponent;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.level.loader.DungeonLoader;
import core.network.server.DialogTracker;
import core.systems.PositionSystem;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import dgir.vm.api.DialectRunner;
import entities.HeroTankControlledFactory;
import level.produs.*;
import level.sandbox.SandboxLevel;
import server.Server;
import systems.HeroActionTickSystem;
import systems.TintTilesSystem;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect.
 *
 * <p>Usage: run with the Gradle task {@code runBlockly}.
 *
 * <p>The HTTP server (default port 8080) always starts automatically. Point the Blockly browser
 * frontend at {@code http://localhost:8080} — no special flag is required.
 *
 * <p>For the Web-UI you have to start the frontend yourself.
 */
public class Client {

  /** The name of the blockly player. */
  public static final String WIZARD_NAME = "Algorim";

  /** Force to apply for movement of all entities. */
  public static float MOVEMENT_FORCE = 7.5f;

  private static final AtomicBoolean restartQueued = new AtomicBoolean(false);

  private static HttpServer httpServer;

  public static boolean SHOOT_AT_PLAYER = true;

  /**
   * If {@code true}, extra debug systems are activated at startup:
   *
   * <ul>
   *   <li>{@link contrib.utils.components.Debugger} – renders hitboxes and component info
   *   <li>{@code DebugDrawSystem} – draws pathfinding graphs and tile borders
   *   <li>{@code LevelEditorSystem} – allows editing tiles at runtime
   *   <li>Tank controls for the hero are enabled
   * </ul>
   *
   * <p>Enable via the {@code --debug} command-line argument or the {@code runBlocklyDebug} Gradle
   * task.
   */
  public static boolean debugMode = false;

  /**
   * If {@code true}, the game runs in sandbox mode: only the empty {@link SandboxLevel} is loaded,
   * all Blockly blocks are unlocked, popups are suppressed, and extra debug systems are active.
   *
   * <p>Enable via the {@code --sandbox} command-line argument or the {@code runBlocklySandbox}
   * Gradle task.
   */
  public static boolean sandboxMode = false;

  /**
   * Setup and run the game. Also starts the HTTP server that the Blockly browser frontend connects
   * to.
   *
   * <p>Recognised command-line arguments:
   *
   * <ul>
   *   <li>{@code --debug} – activate debug systems (hitboxes, tile editor, …)
   *   <li>{@code --sandbox} – start on the empty sandbox level with all blocks unlocked and no
   *       popups; implies {@code --debug}
   * </ul>
   *
   * @param args CLI arguments
   * @throws IOException if textures can not be loaded.
   */
  public static void main(String[] args) throws IOException {
    for (String arg : args) {
      if (arg.equalsIgnoreCase("--debug")) {
        debugMode = true;
      }
      if (arg.equalsIgnoreCase("--sandbox")) {
        sandboxMode = true;
      }
    }

    StateMachine.setResetFrame(false);
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
      BlocklyCodeRunner.instance().stopExecution();
    }
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          if (sandboxMode) {
            // Sandbox: only expose the empty sandbox level – no story progression, no popups.
            DungeonLoader.addLevel(Tuple.of("sandbox", SandboxLevel.class));
          } else {
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
          }

          createHero();
          createSystems();

          startServer();

          DungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          BlocklyCodeRunner.instance().stopExecution();
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
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.resizeable(true);
    Game.windowTitle(sandboxMode ? "Blockly Dungeon [SANDBOX]" : "Blockly Dungeon");
  }

  private static void createSystems() {
    // Register the dgir default dialects (builtin, scf, func, …).
    DialectRunner.registerAllDialects();
    // Register the dungeon dialect runners (move, turn, use, push, pull, drop, pickup, …).
    DungeonDialectRunner.get().register();
    // Register the game-side action gateway so DgRunners can schedule hero actions.
    DgActionGateway.register(new DgHeroActionGateway());

    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new HeroActionTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new TintTilesSystem());
    EventScheduler.setPausable(false);
    Game.add(new FogSystem());
    Game.add(new PressurePlateSystem());
    if (debugMode) Game.add(new Debugger());
    if (debugMode) {
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
    Entity hero = HeroTankControlledFactory.blocklyHero(debugMode);
    hero.add(new AmmunitionComponent());
    Game.add(hero);
  }

  /**
   * Restarts the game by removing all entities, recreating the player, and reloading the current
   * level.
   *
   * <p>Restart work is always dispatched to the LibGDX application thread via {@link
   * com.badlogic.gdx.Application#postRunnable(Runnable)}. This is robust even when tests start the
   * game loop on a non-"main" thread.
   *
   * <p>During the restart, the {@link PositionSystem} is stopped and then run again to ensure that
   * the player is placed correctly in the level. This prevents a race condition where the player
   * might be placed before the level is fully loaded.
   */
  public static void restart() {
    // Coalesce concurrent restart requests; one queued restart is enough.
    if (!restartQueued.compareAndSet(false, true)) return;

    Runnable restartTask =
        () -> {
          try {
            performRestart();
          } finally {
            restartQueued.set(false);
          }
        };

    if (Gdx.app != null) {
      Gdx.app.postRunnable(restartTask);
      return;
    }

    // Fallback for tests without a LibGDX app instance.
    restartTask.run();
  }

  private static void performRestart() {
    BlocklyCodeRunner.instance().stopExecution();
    Game.removeAllEntities();
    Game.system(PositionSystem.class, System::stop);
    createHero();
    DungeonLoader.reloadCurrentLevel();
    Game.system(PositionSystem.class, System::run);
    DialogTracker.instance().clear();
  }
}
