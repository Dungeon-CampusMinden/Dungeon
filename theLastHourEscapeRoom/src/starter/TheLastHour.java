package starter;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.entities.CharacterClass;
import contrib.entities.HeroController;
import contrib.modules.emote.EmoteSystem;
import contrib.modules.worldTimer.WorldTimerSystem;
import contrib.systems.AttributeBarSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ClientStarter;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.GameStarter;
import core.game.MainMenu;
import core.game.PreRunConfiguration;
import core.game.ServerProcess;
import core.game.ServerStarter;
import core.language.Language;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.FrictionSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.NetworkUtils;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLoggerConfig;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import level.LastHourLevel;
import level.LastHourLevelClient;
import modules.computer.ComputerStateSyncSystem;
import modules.usbstick.UsbStickItem;
import network.LastHourEntitySpawnStrategy;
import network.LastHourSnapshotTranslator;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class TheLastHour {

  private static final String SERVER_STOP_REASON = "Server stopped from status window";
  private static final String MENU_BACKGROUND_IMAGE = "images/lasthour.png";
  private static final Color MENU_ACCENT_COLOR = new Color(0.56f, 0.87f, 1f, 1f);

  /** Enable or disable debug mode, which adds extra systems for debugging and level editing. */
  public static final boolean DEBUG_MODE = false;

  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03
  };

  /**
   * Main entry point for "The Last Hour".
   *
   * <p>Sets up logging and hands the menu/hosting, client, and server configuration to the {@link
   * MainMenu}, which decides (based on {@code args}) whether to run as a dedicated server or to
   * show the main menu and run as a client.
   *
   * @param args command-line arguments (a {@code --server} flag starts the dedicated server)
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    GameStarter game =
        GameStarter.builder("The Last Hour", TheLastHour.class)
            .backgroundImage(MENU_BACKGROUND_IMAGE)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.EN)
            .build();

    ServerStarter server =
        ServerStarter.builder(TheLastHour::serverSetup)
            .characterClasses(MULTIPLAYER_CHARACTER_CLASSES)
            .levels(Tuple.of("lasthour", LastHourLevel.class))
            .onConfigure(UsbStickItem::ensureRegistration)
            .config(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class)
            .snapshotTranslator(new LastHourSnapshotTranslator())
            .entitySpawnStrategy(new LastHourEntitySpawnStrategy())
            .onFrame(TheLastHour::onFrame)
            .build();

    ClientStarter client =
        ClientStarter.builder(LastHourClient::clientSetup)
            .levels(Tuple.of("lasthour", LastHourLevelClient.class))
            .onConfigure(LastHourClient::registerClientContent)
            .initLocalization(LastHourClient::initLocalization)
            .registerSettings(LastHourClient::registerSettings)
            .config(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class)
            .snapshotTranslator(new LastHourSnapshotTranslator())
            .entitySpawnStrategy(new LastHourEntitySpawnStrategy())
            .build();

    MainMenu.run(args, game, client, server);
  }

  /**
   * Server-side {@link Game#userOnSetup} callback: installs the authoritative systems, level-load
   * broadcast, and the standalone server status window.
   */
  private static void serverSetup() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.remove(AttributeBarSystem.class);

    ECSManagement.system(
        LevelSystem.class,
        levelSystem ->
            levelSystem.onLevelLoad(
                () -> {
                  GameLoop.onLevelLoad.execute();
                  Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                }));
    showServerStatusWindow();

    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());
    ECSManagement.add(new ComputerStateSyncSystem());

    registerLocalWorldTimerSystem();

    if (DEBUG_MODE && !Game.isHeadless()) {
      ECSManagement.add(new Debugger());
      KeyboardConfig.PAUSE.value(Input.Keys.UNKNOWN); // Unbind Debuggers' pause bind

      ECSManagement.add(new DebugDrawSystem());
      ECSManagement.add(new LevelEditorSystem());
    }
  }

  private static void showServerStatusWindow() {
    // Servers hosted via the main menu's "Host Game" option run as a managed child process; their
    // status is shown inside the game (pause menu) instead of a separate window.
    if (Boolean.getBoolean(ServerProcess.MANAGED_PROPERTY)) {
      return;
    }

    String serverInfo = serverInfoText();
    if (GraphicsEnvironment.isHeadless()) {
      System.out.println(serverInfo);
      return;
    }

    SwingUtilities.invokeLater(() -> createServerStatusWindow(serverInfo).setVisible(true));
  }

  private static JFrame createServerStatusWindow(String serverInfo) {
    JFrame frame = new JFrame("The Last Hour Server");
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    JTextArea infoText = new JTextArea(serverInfo);
    infoText.setEditable(false);
    infoText.setFocusable(false);

    JButton stopButton = new JButton("Stop Server");
    stopButton.addActionListener(
        event -> {
          stopButton.setEnabled(false);
          frame.dispose();
          Game.exit(SERVER_STOP_REASON);
        });

    JPanel content = new JPanel(new BorderLayout(12, 12));
    content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    content.add(new JLabel("Server is running"), BorderLayout.NORTH);
    content.add(infoText, BorderLayout.CENTER);
    content.add(stopButton, BorderLayout.SOUTH);

    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent event) {
            frame.dispose();
            Game.exit(SERVER_STOP_REASON);
          }
        });
    frame.setContentPane(content);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  private static String serverInfoText() {
    StringBuilder info = new StringBuilder();
    info.append("Port: ").append(PreRunConfiguration.networkPort()).append(System.lineSeparator());
    info.append("IP addresses:").append(System.lineSeparator());
    NetworkUtils.localIpAddresses()
        .forEach(ip -> info.append("  ").append(ip).append(System.lineSeparator()));
    return info.toString();
  }

  /** Registers the local world timer render/callback system on non-headless clients. */
  public static void registerLocalWorldTimerSystem() {
    if (Game.isHeadless()) {
      return;
    }
    ECSManagement.add(new WorldTimerSystem().onTimerExpired(LastHourLevel::onTimerExpired));
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
