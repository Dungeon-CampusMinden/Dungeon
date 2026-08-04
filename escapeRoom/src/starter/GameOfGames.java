package starter;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.entities.CharacterClass;
import contrib.entities.HeroController;
import contrib.modules.emote.EmoteSystem;
import contrib.systems.AttributeBarSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ClientStarter;
import core.game.ECSManagement;
import core.game.GameStarter;
import core.game.MainMenu;
import core.game.ServerStarter;
import core.language.Language;
import core.language.Localization;
import core.systems.FrictionSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLoggerConfig;
import gameOfGames.level.GameOfGamesClientLevel;
import gameOfGames.level.GameOfGamesLevel;
import java.util.logging.Level;

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

    GameStarter game =
        GameStarter.builder("Game of Games", GameOfGames.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.EN)
            .build();

    ServerStarter server =
        ServerStarter.builder(GameOfGames::serverSetup)
            .characterClasses(MULTIPLAYER_CHARACTER_CLASSES)
            .levels(Tuple.of(LEVEL_KEY, GameOfGamesLevel.class))
            .onConfigure(GameOfGames::initLocalization)
            .config(
                new SimpleIPath("dungeon_config.json"),
                contrib.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .onFrame(GameOfGames::onFrame)
            .build();

    ClientStarter client =
        ClientStarter.builder(GameOfGames::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, GameOfGamesClientLevel.class))
            .initLocalization(GameOfGames::initLocalization)
            .config(
                new SimpleIPath("dungeon_config.json"),
                contrib.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Registers escape-room translation files. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/de.json");
    localization.registerTranslationFile(Language.EN, "language/en.json");
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

  private static void clientSetup() {
    ECSManagement.remove(AttributeBarSystem.class);

    if (DEBUG_MODE) {
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
