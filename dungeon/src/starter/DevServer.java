package starter;

import contrib.entities.HeroController;
import contrib.systems.*;
import core.Game;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.Tuple;
import core.utils.logging.DungeonLogLevel;
import core.utils.logging.DungeonLogger;
import core.utils.logging.DungeonLoggerConfig;

/** The main class for the Multiplayer Server for development and testing purposes. */
public class DevServer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DevServer.class);

  /**
   * Main method to start the development server.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.initWithLevel(DungeonLogLevel.DEBUG);
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));

    Game.userOnSetup(DevServer::onSetup);
    Game.userOnFrame(DevServer::onFrame);

    Game.run();
  }

  private static void onSetup() {
    Game.add(new PositionSystem());
    Game.add(
        new LevelSystem(
            () -> {
              GameLoop.onLevelLoad.execute();
              Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
            }));
    Game.add(new VelocitySystem());
    Game.add(new FrictionSystem());
    Game.add(new MoveSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new PathSystem());
    Game.add(new AISystem());
    Game.add(new CollisionSystem());
    Game.add(new FallingSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new DrawSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new EventScheduler());
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
