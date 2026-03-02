package starter;

import contrib.hud.dialogs.DialogFactory;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import level.LastHourLevelClient;
import modules.computer.ComputerFactory;
import modules.computer.LastHourDialogTypes;
import modules.trash.TrashMinigameUI;
import util.ui.BlackFadeCutscene;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class LastHourClient {

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player1");

    registerCustomDialogs();

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevelClient.class));

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
    Game.windowTitle("Dev Client - " + PreRunConfiguration.username());
    Game.userOnSetup(
        () -> {
          Game.add(new Debugger());
          System.out.println("DevClient started");
        });

    // Start the game
    Game.run();
  }

  private static void registerCustomDialogs() {
    DialogFactory.register(LastHourDialogTypes.COMPUTER, ComputerFactory::build);
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    DialogFactory.register(LastHourDialogTypes.TEXT_CUTSCENE, BlackFadeCutscene::build);
  }
}
