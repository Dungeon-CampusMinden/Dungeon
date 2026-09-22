package rooms.systemRecovery;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerProcess;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.language.Localization;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.tracking.Tracking;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.Debugger;
import feature.emote.EmoteSystem;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import feature.systems.AISystem;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import feature.systems.LeverSystem;
import java.util.logging.Level;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.level.SystemRecoveryClientLevel;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.network.SystemRecoveryEntitySpawnStrategy;
import rooms.systemRecovery.network.SystemRecoverySnapshotTranslator;
import rooms.systemRecovery.save.SystemRecoverySave;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryTranslator;

/** Entry point for the System Recovery escape room. */
public final class SystemRecovery {

  private static final String LEVEL_KEY = "systemrecovery";
  private static final Color MENU_ACCENT_COLOR = new Color(0.43f, 0.78f, 0.72f, 1f);
  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03
  };

  private static boolean debugMode;
  private static boolean loadFromSave;
  private static final String LOAD_SAVE_ARGUMENT = "--load-system-recovery";

  private SystemRecovery() {}

  /**
   * Starts the System Recovery escape room.
   *
   * @param args command-line arguments; {@code --server} starts a dedicated server and {@code
   *     --leveleditor} starts the room directly in editor mode
   */
  public static void main(String[] args) {
    configureDebugMode(args);
    configureLoadFromSave(args);
    Tracking.configureRoom("system-recovery");
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    ServerStarter server =
        ServerStarter.builder(SystemRecovery::serverSetup)
            .characterClasses(MULTIPLAYER_CHARACTER_CLASSES)
            .levels(Tuple.of(LEVEL_KEY, SystemRecoveryLevel.class))
            .onConfigure(SystemRecovery::registerContent)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new SystemRecoverySnapshotTranslator())
            .entitySpawnStrategy(new SystemRecoveryEntitySpawnStrategy())
            .onFrame(SystemRecovery::onFrame)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, SystemRecoveryClient::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, SystemRecoveryClientLevel.class))
            .initLocalization(SystemRecovery::initLocalization)
            .onConfigure(SystemRecovery::registerContent)
            .build();

    GameStarter game =
        GameStarter.builder("System Recovery", SystemRecovery.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.DE)
            .levelEditor("levels/systemRecovery")
            .serverArguments(hostedServerArguments())
            .continueGame(SystemRecoverySave::exists, hostedServerArguments(true))
            .build();

    MainMenu.run(args, game, client, server);
  }

  /**
   * Returns whether debug-only room controls are enabled for this process.
   *
   * @return {@code true} for an explicit debug launch or the level editor
   */
  public static boolean debugMode() {
    return debugMode;
  }

  /**
   * Configures the process-wide debug flag from launcher arguments.
   *
   * @param args launcher arguments inspected for debug or level-editor mode
   */
  public static void configureDebugMode(String... args) {
    debugMode = containsArgument(args, "--debug") || containsArgument(args, "--leveleditor");
  }

  /**
   * Configures whether the authoritative server should restore the menu checkpoint.
   *
   * @param args launcher arguments inspected for the continue flag
   */
  public static void configureLoadFromSave(String... args) {
    loadFromSave = containsArgument(args, LOAD_SAVE_ARGUMENT);
  }

  /**
   * @return whether this process was launched by the main-menu continue action
   */
  public static boolean loadFromSave() {
    return loadFromSave;
  }

  /**
   * Returns launch arguments for the dedicated host child process.
   *
   * @return server mode and, when enabled, the debug mode flag
   */
  static String[] hostedServerArguments() {
    return hostedServerArguments(false);
  }

  static String[] hostedServerArguments(boolean continueGame) {
    java.util.List<String> arguments = new java.util.ArrayList<>();
    arguments.add(ServerProcess.SERVER_ARGUMENT);
    if (continueGame) arguments.add(LOAD_SAVE_ARGUMENT);
    if (debugMode) arguments.add("--debug");
    return arguments.toArray(String[]::new);
  }

  private static boolean containsArgument(String[] args, String expected) {
    if (args == null) return false;
    for (String arg : args) {
      if (expected.equals(arg)) return true;
    }
    return false;
  }

  /** Registers the shared escape-room translations used by the menu and default dialogs. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
    localization.registerTranslationFile(Language.DE, "language/systemRecovery/de.json");
    localization.registerTranslationFile(Language.EN, "language/systemRecovery/en.json");
    localization.setCurrentTranslator(new SystemRecoveryTranslator());
  }

  /** Registers shared translations and custom dialog builders. */
  static void registerContent() {
    initLocalization();
    SystemRecoveryAchievements.register();
    BlackFadeCutscene.register();
    BatteryItem.ensureRegistration();
    SearchProgramChipItem.ensureRegistration();
    SortProgramStickItem.ensureRegistration();
    SystemCoreAccessChipItem.ensureRegistration();
    SystemRecoveryComputerFactory.ensureRegistration();
  }

  private static void serverSetup() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new AISystem());
    ECSManagement.remove(AttributeBarSystem.class);
    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());
    ECSManagement.add(new LeverSystem());
    ECSManagement.add(new PetriNetSystem());
    ECSManagement.add(new HintSystem());

    if (debugMode() && !Game.isHeadless()) {
      ECSManagement.add(new Debugger());
      KeyboardConfig.PAUSE.value(Input.Keys.UNKNOWN);
      ECSManagement.add(new DebugDrawSystem());
      ECSManagement.add(new LevelEditorSystem());
    }
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
