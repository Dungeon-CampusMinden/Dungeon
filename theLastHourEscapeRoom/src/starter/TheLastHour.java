package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import contrib.components.SkillComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.entities.HeroController;
import contrib.modules.emote.EmoteSystem;
import contrib.modules.worldTimer.WorldTimerSystem;
import contrib.systems.AttributeBarSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.MainMenu;
import core.game.PreRunConfiguration;
import core.game.ServerProcess;
import core.language.Language;
import core.language.Localization;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.FrictionSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.CursorUtil;
import core.utils.NetworkUtils;
import core.utils.Tuple;
import core.utils.components.draw.TextureGenerator;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.shader.ColorGradeShader;
import core.utils.components.draw.shader.HueRemapShader;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLoggerConfig;
import core.utils.settings.ButtonBindingSetting;
import core.utils.settings.ClientSettings;
import core.utils.settings.DescriptionSetting;
import core.utils.settings.SectionDividerSetting;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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

  private static final String APPLICATION_PROPERTIES = "/application.properties";
  private static final String SERVER_ARGUMENT = "--server";
  private static final String SERVER_PROPERTY = "server";
  private static final String SERVER_STOP_REASON = "Server stopped from status window";

  private static final String BACKGROUND_MUSIC = "sounds/forest_bgm.wav";
  private static Music backgroundMusic;

  /** Enable or disable debug mode, which adds extra systems for debugging and level editing. */
  public static final boolean DEBUG_MODE = false;

  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03
  };

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    if (shouldRunMpServer(args)) {
      startServer();
    } else {
      // The engine is multiplayer-only: the main menu lets the player host or join a server.
      MainMenu.run(new LastHourStarter());
    }
  }

  /**
   * Configures and starts the dedicated multiplayer server, then runs the headless game loop.
   *
   * <p>This is the entry point used by the main menu's "Host Game" option, which launches this
   * class in a child process with the {@code --server} argument (see {@link ServerProcess}).
   */
  private static void startServer() {
    Game.userOnFrame(TheLastHour::onFrame);
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    PreRunConfiguration.multiplayerCharacterClasses(MULTIPLAYER_CHARACTER_CLASSES);
    PreRunConfiguration.networkPort(
        Integer.getInteger(ServerProcess.PORT_PROPERTY, PreRunConfiguration.networkPort()));

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevel.class));
    UsbStickItem.ensureRegistration();
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(false);
    Game.userOnSetup(() -> onUserSetup(true));
    Game.frameRate(60);
    Game.windowTitle("The Last Hour");
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.run();
  }

  private static boolean shouldRunMpServer(String[] args) {
    if (args != null && Arrays.asList(args).contains(SERVER_ARGUMENT)) {
      return true;
    }

    try (InputStream propertiesStream =
        TheLastHour.class.getResourceAsStream(APPLICATION_PROPERTIES)) {
      if (propertiesStream == null) {
        return false;
      }

      Properties properties = new Properties();
      properties.load(propertiesStream);
      return properties.containsKey(SERVER_PROPERTY);
    } catch (IOException e) {
      return false;
    }
  }

  private static void onUserSetup(boolean runMpServer) {
    if (runMpServer) {
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
    } else {
      Entity hero =
          HeroBuilder.builder().characterClass(CharacterClass.THE_LAST_HOUR_CHAR03).build();
      hero.fetch(SkillComponent.class).ifPresent(SkillComponent::removeAll);
      Game.add(hero);
      Game.stage().ifPresent(CursorUtil::initListener);

      setupClient();
    }

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

  /** Proxy to init all client resources which are not available on the server. */
  public static void setupClient() {
    initLocalization();
    setupMusic();
    staticRenderTextures();
    registerSettings();
  }

  private static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/de.json");
    localization.registerTranslationFile(Language.EN, "language/en.json");
    localization.currentLanguage(Language.EN);
  }

  private static final String T_SETTINGS_CONTROLS_HEADER = "settings.controls_header";
  private static final String T_SETTINGS_CONTROLS_DESCRIPTION = "settings.controls_description";
  private static final String T_SETTINGS_PAUSE = "settings.pause";
  private static final String T_SETTINGS_INTERACT = "settings.interact";
  private static final String T_SETTINGS_INVENTORY = "settings.inventory";
  private static final String T_SETTINGS_INVENTORY_DESCRIPTION = "settings.inventory_description";

  /** Registers additional client settings. */
  private static void registerSettings() {
    ClientSettings.registerSetting(new SectionDividerSetting(T_SETTINGS_CONTROLS_HEADER));
    ClientSettings.registerSetting(
        new DescriptionSetting(T_SETTINGS_CONTROLS_DESCRIPTION, Input.Keys.E));
    ClientSettings.registerSetting(new ButtonBindingSetting(T_SETTINGS_PAUSE, Input.Keys.P, false));
    ClientSettings.registerSetting(
        new ButtonBindingSetting(T_SETTINGS_INTERACT, Input.Keys.E, false));
    ClientSettings.registerSetting(
        new ButtonBindingSetting(T_SETTINGS_INVENTORY, Input.Keys.I, false));
    ClientSettings.registerSetting(
        new DescriptionSetting(T_SETTINGS_INVENTORY_DESCRIPTION, Input.Buttons.RIGHT));
  }

  private static final List<Tuple<String, Color>> USB_TEXTURES =
      List.of(
          Tuple.of("items/usb-side-green.png", Color.GREEN),
          Tuple.of("items/usb-side-blue.png", Color.BLUE),
          Tuple.of("items/usb-side-yellow.png", Color.YELLOW));

  /** Statically renders the needed textures. */
  private static void staticRenderTextures() {
    String basePath = "items/usb-side-red.png";
    float baseHue = 0.0f;

    for (Tuple<String, Color> usbTexture : USB_TEXTURES) {
      ShaderList shaderList = new ShaderList();

      String outTexturePath = usbTexture.a();
      Color color = usbTexture.b();
      float[] hsv = new float[3];
      shaderList.add("hueRemap", new HueRemapShader(baseHue, color.toHsv(hsv)[0] / 360f));

      TextureGenerator.registerRenderShaderTexture(basePath, outTexturePath, shaderList);
    }

    // Invert the keyboard / mouse input-prompt spritesheet so the white-on-transparent icons
    // read clearly against the dark HUD text used in this game.
    String keyboardPromptPath = "hud/input/keyboard_mouse.png";
    ShaderList invertShaders = new ShaderList();
    invertShaders.add("invert", new ColorGradeShader().invert(true));
    TextureGenerator.registerRenderShaderTexture(
        keyboardPromptPath, keyboardPromptPath, invertShaders);

    // Put the first frame of the idle animation of the rogue and the char03 characters into a
    // special texture in "@gen/char03.png" and "@gen/rogue.png", so they can be used for Dialogs
    // without needing to parse the spritesheet again.
    registerCharacterPortrait(CharacterClass.THE_LAST_HOUR_ROGUE, ROGUE_PORTRAIT_PATH);
    registerCharacterPortrait(CharacterClass.THE_LAST_HOUR_CHAR03, CHAR03_PORTRAIT_PATH);
  }

  /** Path of the generated portrait texture for the Rogue character. */
  public static final String ROGUE_PORTRAIT_PATH = "@gen/rogue.png";

  /** Path of the generated portrait texture for the Char03 character. */
  public static final String CHAR03_PORTRAIT_PATH = "@gen/char03.png";

  /** Width / height in pixels of a single frame in the character spritesheets. */
  private static final int CHARACTER_FRAME_SIZE = 32;

  private static final int CHARACTER_FRAME_PADDING = 8;

  /**
   * Extracts the first frame from the given character's spritesheet and registers it as a
   * standalone texture in the {@link TextureMap} under {@code outPath}.
   *
   * @param characterClass character whose sprite sheet should be sampled
   * @param outPath virtual texture-map output path for the generated portrait
   */
  private static void registerCharacterPortrait(CharacterClass characterClass, String outPath) {
    String sheetPath = characterClass.textures().pathString();
    if (!sheetPath.endsWith(".png")) {
      sheetPath = sheetPath + "/" + sheetPath.substring(sheetPath.lastIndexOf('/') + 1) + ".png";
    }
    TextureGenerator.registerSpritesheetRegionTexture(
        sheetPath,
        CHARACTER_FRAME_PADDING,
        CHARACTER_FRAME_PADDING,
        CHARACTER_FRAME_SIZE - CHARACTER_FRAME_PADDING * 2,
        CHARACTER_FRAME_SIZE - CHARACTER_FRAME_PADDING * 2,
        outPath);
  }

  /**
   * Initializes and starts the background music for the game, and sets up listeners to adjust the
   * volume based on client settings changes.
   */
  private static void setupMusic() {
    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(
        ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);

    ClientSettings.setOnVolumeChange(
        (key, value) -> {
          if (key.equals(ClientSettings.KEY_MUSIC_VOLUME)
              || key.equals(ClientSettings.KEY_MASTER_VOLUME)) {
            backgroundMusic.setVolume(
                ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);
          }
        });
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
