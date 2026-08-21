package rooms.lasthour.starter;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.PreRunConfiguration;
import engine.game.ServerProcess;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.language.Localization;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.utils.NetworkUtils;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.Debugger;
import feature.emote.EmoteSystem;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import feature.timer.WorldTimerSystem;
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
import rooms.lasthour.level.LastHourLevel;
import rooms.lasthour.level.LastHourLevelClient;
import rooms.lasthour.modules.computer.ComputerStateSyncSystem;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.network.LastHourEntitySpawnStrategy;
import rooms.lasthour.network.LastHourSnapshotTranslator;
import rooms.lasthour.util.LastHourAchievements;
import rooms.lasthour.util.translation.LastHourTranslator;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runTheLastHour}.
 */
public class TheLastHour {

  private static final String SERVER_STOP_REASON = "Server stopped from status window";
  private static final String MENU_BACKGROUND_IMAGE = "images/lasthour.png";
  private static final Color MENU_ACCENT_COLOR = new Color(0.56f, 0.87f, 1f, 1f);

  /** Enable or disable debug mode, which adds extra systems for debugging and level editing. */
  public static final boolean DEBUG_MODE = true;

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
            .onConfigure(
                () -> {
                  LastHourAchievements.register();
                  BlackFadeCutscene.register();
                  UsbStickItem.ensureRegistration();
                  initLocalization();
                })
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new LastHourSnapshotTranslator())
            .entitySpawnStrategy(new LastHourEntitySpawnStrategy())
            .onFrame(TheLastHour::onFrame)
            .build();

    ClientStarter client =
        ClientStarter.builder(LastHourClient::clientSetup)
            .levels(Tuple.of("lasthour", LastHourLevelClient.class))
            .onConfigure(LastHourClient::registerClientContent)
            .initLocalization(TheLastHour::initLocalization)
            .registerSettings(LastHourClient::registerSettings)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new LastHourSnapshotTranslator())
            .entitySpawnStrategy(new LastHourEntitySpawnStrategy())
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Registers the translation files for the supported languages. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/theLastHour/de.json");
    localization.registerTranslationFile(Language.EN, "language/theLastHour/en.json");
    localization.setCurrentTranslator(new LastHourTranslator());
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
