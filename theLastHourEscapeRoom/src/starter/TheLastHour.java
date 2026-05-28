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
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.FrictionSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.CursorUtil;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
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

  private static final String SERVER_ARGUMENT = "--server";

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
    boolean runMpServer = args != null && Arrays.asList(args).contains(SERVER_ARGUMENT);

    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    if (runMpServer) {
      Game.userOnFrame(TheLastHour::onFrame);
      PreRunConfiguration.multiplayerEnabled(true);
      PreRunConfiguration.isNetworkServer(true);
      PreRunConfiguration.multiplayerCharacterClasses(MULTIPLAYER_CHARACTER_CLASSES);
    }

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevel.class));
    UsbStickItem.ensureRegistration();
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(false);
    Game.userOnSetup(() -> onUserSetup(runMpServer));
    Game.frameRate(60);
    Game.windowTitle("The Last Hour");
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.run();
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
    } else {
      Entity hero =
          HeroBuilder.builder().characterClass(CharacterClass.THE_LAST_HOUR_CHAR03).build();
      hero.fetch(SkillComponent.class).ifPresent(SkillComponent::removeAll);
      Game.add(hero);
      Game.stage().ifPresent(CursorUtil::initListener);
      setupMusic();

      KeyboardConfig.PAUSE.value(Input.Keys.UNKNOWN);

      staticRenderTextures();
      registerSettings();
    }

    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());
    ECSManagement.add(new EventScheduler());
    ECSManagement.add(new ComputerStateSyncSystem());

    if (DEBUG_MODE && !Game.isHeadless()) {
      ECSManagement.add(new Debugger());
      ECSManagement.add(new DebugDrawSystem());
      ECSManagement.add(new LevelEditorSystem());
    }
  }

  private static void registerSettings() {
    ClientSettings.registerSetting("controls_header", new SectionDividerSetting("Controls"));
    ClientSettings.registerSetting(
        "controls_description",
        new DescriptionSetting(
            "Use the mouse to hover over interactables, then press <E> to interact!"));
    ClientSettings.registerSetting("pause", new ButtonBindingSetting("Pause", Input.Keys.P, false));
    ClientSettings.registerSetting(
        "interact", new ButtonBindingSetting("Interact", Input.Keys.E, false));
    ClientSettings.registerSetting(
        "inventory", new ButtonBindingSetting("Inventory", Input.Keys.I, false));
  }

  private static final List<Tuple<String, Color>> USB_TEXTURES =
      List.of(
          Tuple.of("items/usb-side-green.png", Color.GREEN),
          Tuple.of("items/usb-side-blue.png", Color.BLUE),
          Tuple.of("items/usb-side-yellow.png", Color.YELLOW));

  /** Statically renders the needed textures. */
  public static void staticRenderTextures() {
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
  public static void setupMusic() {
    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(
        ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);

    ClientSettings.setOnVolumeChange(
        (key, value) -> {
          if (key.equals(ClientSettings.MUSIC_VOLUME) || key.equals(ClientSettings.MASTER_VOLUME)) {
            backgroundMusic.setVolume(
                ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);
          }
        });
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
