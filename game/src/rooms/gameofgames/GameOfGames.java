package rooms.gameofgames;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerStarter;
import engine.game.SingleplayerStarter;
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
import rooms.gameofgames.level.GameOfGamesClientLevel;
import rooms.gameofgames.level.GameOfGamesLevel;
import rooms.gameofgames.network.GameOfGamesEntitySpawnStrategy;
import rooms.gameofgames.network.GameOfGamesSnapshotTranslator;

/** Entry point for the Game of Games escape room. */
public final class GameOfGames {

  private static final String LEVEL_KEY = "gameofgames";
  private static final Color MENU_ACCENT_COLOR = new Color(0.92f, 0.74f, 0.22f, 1f);
  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.WIZARD, CharacterClass.HUNTER
  };

  /** Enables debug systems and the level editor while developing the room. */
  public static final boolean DEBUG_MODE = false;

  private GameOfGames() {}

  /**
   * Starts Game of Games.
   *
   * @param args command-line arguments; {@code --server} starts a dedicated server
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    ServerStarter server =
        ServerStarter.builder(GameOfGames::serverSetup)
            .characterClasses(MULTIPLAYER_CHARACTER_CLASSES)
            .levels(Tuple.of(LEVEL_KEY, GameOfGamesLevel.class))
            .onConfigure(GameOfGames::initLocalization)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new GameOfGamesSnapshotTranslator())
            .entitySpawnStrategy(new GameOfGamesEntitySpawnStrategy())
            .onFrame(GameOfGames::onFrame)
            .build();

    ClientStarter client =
        ClientStarter.builder(GameOfGamesClient::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, GameOfGamesClientLevel.class))
            .initLocalization(GameOfGames::initLocalization)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new GameOfGamesSnapshotTranslator())
            .entitySpawnStrategy(new GameOfGamesEntitySpawnStrategy())
            .build();

    SingleplayerStarter singleplayer =
        SingleplayerStarter.builder(GameOfGames::serverSetup, GameOfGamesClient::clientSetup)
            .characterClass(MULTIPLAYER_CHARACTER_CLASSES[0])
            .levels(Tuple.of(LEVEL_KEY, GameOfGamesLevel.class))
            .onConfigure(GameOfGames::initLocalization)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new GameOfGamesSnapshotTranslator())
            .entitySpawnStrategy(new GameOfGamesEntitySpawnStrategy())
            .onFrame(GameOfGames::onFrame)
            .levelEditor("levels/gameOfGames")
            .build();

    GameStarter game =
        GameStarter.builder("Game of Games", GameOfGames.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.EN)
            .singleplayer(singleplayer)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Registers escape-room translation files. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
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
