package rooms.systemRecovery;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.language.Localization;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import feature.components.Debugger;
import feature.emote.EmoteSystem;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.logging.Level;
import rooms.systemRecovery.level.SystemRecoveryClientLevel;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.network.SystemRecoveryEntitySpawnStrategy;
import rooms.systemRecovery.network.SystemRecoverySnapshotTranslator;

/** Entry point for the System Recovery escape room. */
public final class SystemRecovery {

  private static final String LEVEL_KEY = "systemrecovery";
  private static final Color MENU_ACCENT_COLOR = new Color(0.43f, 0.78f, 0.72f, 1f);
  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03
  };

  /** Enables debug systems while developing the room. */
  public static final boolean DEBUG_MODE = false;

  private SystemRecovery() {}

  /**
   * Starts the System Recovery escape room.
   *
   * @param args command-line arguments; {@code --server} starts a dedicated server and {@code
   *     --leveleditor} starts the room directly in editor mode
   */
  public static void main(String[] args) {
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
            .onConfigure(SystemRecoveryComputerFactory::ensureRegistration)
            .build();

    GameStarter game =
        GameStarter.builder("System Recovery", SystemRecovery.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.EN)
            .levelEditor("levels/systemRecovery")
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Registers the shared escape-room translations used by the menu and default dialogs. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
  }

  /** Registers shared translations and custom dialog builders. */
  static void registerContent() {
    initLocalization();
    SystemRecoveryComputerFactory.ensureRegistration();
  }

  private static void serverSetup() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.remove(AttributeBarSystem.class);
    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());

    if (DEBUG_MODE && !Game.isHeadless()) {
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
