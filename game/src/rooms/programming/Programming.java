package rooms.programming;

import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import java.util.logging.Level;
import rooms.programming.level.ProgrammingClientLevel;
import rooms.programming.level.ProgrammingLevel;

/** Entry point for the Programming 1 escape room. */
public final class Programming {

  private static final String LEVEL_KEY = "programming";
  private static final CharacterClass[] CHARACTER_CLASSES = {
    CharacterClass.WIZARD, CharacterClass.HUNTER
  };

  private Programming() {}

  /** Starts the Programming 1 escape room or its level editor. */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    ServerStarter server =
        ServerStarter.builder(Programming::serverSetup)
            .characterClasses(CHARACTER_CLASSES)
            .maximumPlayers(2)
            .levels(Tuple.of(LEVEL_KEY, ProgrammingLevel.class))
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, () -> {})
            .levels(Tuple.of(LEVEL_KEY, ProgrammingClientLevel.class))
            .build();

    GameStarter game =
        GameStarter.builder("Das Erbe der Seelenweber", Programming.class)
            .language(Language.DE)
            .levelEditor("levels/programming")
            .build();

    MainMenu.run(args, game, client, server);
  }

  private static void serverSetup() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.remove(AttributeBarSystem.class);
    ECSManagement.add(new CollisionSystem());
  }
}
