package starter;

import contrib.entities.HeroController;
import contrib.systems.AISystem;
import contrib.systems.CollisionSystem;
import contrib.systems.FallingSystem;
import contrib.systems.HealthSystem;
import contrib.systems.LeverSystem;
import contrib.systems.ManaRestoreSystem;
import contrib.systems.PathSystem;
import contrib.systems.PressurePlateSystem;
import contrib.systems.ProjectileSystem;
import core.Game;
import core.System;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.game.SystemProfile;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.logging.DungeonLogLevel;
import core.utils.logging.DungeonLogger;
import core.utils.logging.DungeonLoggerConfig;
import java.util.function.Supplier;

/** The main class for the Multiplayer Server for development and testing purposes. */
public class MultiplayerServer {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(MultiplayerServer.class);

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
    ECSManagement.bootstrapDefaultSystems(SystemProfile.SERVER_SIMULATION);
    ECSManagement.bootstrapGameplaySystems(SystemProfile.SERVER_SIMULATION);

    ECSManagement.system(
      LevelSystem.class,
      levelSystem ->
        levelSystem.onLevelLoad(
          () -> {
            GameLoop.onLevelLoad.execute();
            Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
          }));

    registerIfAbsent(ProjectileSystem.class, ProjectileSystem::new);
    registerIfAbsent(HealthSystem.class, HealthSystem::new);
    registerIfAbsent(ManaRestoreSystem.class, ManaRestoreSystem::new);
    registerIfAbsent(PathSystem.class, PathSystem::new);
    registerIfAbsent(AISystem.class, AISystem::new);
    registerIfAbsent(CollisionSystem.class, CollisionSystem::new);
    registerIfAbsent(FallingSystem.class, FallingSystem::new);
    registerIfAbsent(LeverSystem.class, LeverSystem::new);
    registerIfAbsent(PressurePlateSystem.class, PressurePlateSystem::new);
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }

  private static <T extends System> void registerIfAbsent(
    Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
