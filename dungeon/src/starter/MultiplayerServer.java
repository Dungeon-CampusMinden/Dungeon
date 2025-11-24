package starter;

import contrib.entities.HeroController;
import contrib.systems.*;
import core.Game;
import core.game.ECSManagement;
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
public class MultiplayerServer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MultiplayerServer.class);

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

    Game.userOnSetup(MultiplayerServer::onSetup);
    Game.userOnFrame(MultiplayerServer::onFrame);

    Game.run();
  }

  private static void onSetup() {
    ECSManagement.system(
        LevelSystem.class,
        levelSystem ->
            levelSystem.onLevelLoad(
                () -> {
                  GameLoop.onLevelLoad.execute();
                  Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                }));

    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());

    ECSManagement.add(new ProjectileSystem());
    ECSManagement.add(new HealthSystem());
    ECSManagement.add(new ManaRestoreSystem());
    ECSManagement.add(new PathSystem());
    ECSManagement.add(new AISystem());
    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new FallingSystem());
    ECSManagement.add(new LeverSystem());
    ECSManagement.add(new PressurePlateSystem());
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
