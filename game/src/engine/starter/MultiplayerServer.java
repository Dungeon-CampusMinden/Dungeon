package engine.starter;

import engine.Game;
import engine.game.ECSManagement;
import engine.game.PreRunConfiguration;
import engine.level.DungeonLevel;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.logging.DungeonLogLevel;
import engine.utils.logging.DungeonLogger;
import engine.utils.logging.DungeonLoggerConfig;
import feature.entities.HeroController;
import feature.systems.AISystem;
import feature.systems.HealthSystem;
import feature.systems.LeverSystem;
import feature.systems.ManaRestoreSystem;
import feature.systems.PathSystem;
import feature.systems.PressurePlateSystem;
import feature.systems.ProjectileSystem;

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
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));

    Game.userOnSetup(MultiplayerServer::onSetup);
    Game.userOnFrame(MultiplayerServer::onFrame);

    Game.run();
  }

  private static void onSetup() {
    ECSManagement.add(new ProjectileSystem());
    ECSManagement.add(new HealthSystem());
    ECSManagement.add(new ManaRestoreSystem());
    ECSManagement.add(new PathSystem());
    ECSManagement.add(new AISystem());
    ECSManagement.add(new LeverSystem());
    ECSManagement.add(new PressurePlateSystem());
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
