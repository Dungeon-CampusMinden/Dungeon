package starter;

import contrib.client.DefaultClientLoopHostFactory;
import contrib.configuration.DebugKeyboardConfig;
import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityOverlaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import contrib.editor.level.LevelEditorSystem;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ECSManagement;
import core.game.PreRunConfiguration;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Supplier;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class MultiplayerClient {

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player1");

    // Game Settings
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"), KeyboardConfig.class, DebugKeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(30);
    Game.windowTitle("Dev Client - " + PreRunConfiguration.username());
    Game.userOnSetup(
        () -> {
          installDevClientDebugSystems();
          System.out.println("DevClient started");
        });

    // Start the game
    DefaultClientLoopHostFactory.installDefaultLoopHost();
    Game.run();
  }

  private static void installDevClientDebugSystems() {
    addIfAbsent(DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    addIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
    addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    addIfAbsent(DebugEntityOverlaySystem.class, DebugEntityOverlaySystem::new);
  }

  private static <T extends core.System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
