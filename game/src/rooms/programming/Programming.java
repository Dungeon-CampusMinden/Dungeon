package rooms.programming;

import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerLifecycle;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.language.Localization;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.tracking.TrackingConsentFeature;
import engine.utils.CursorUtil;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.questlog.QuestLogUI;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import java.util.logging.Level;
import rooms.programming.level.ProgrammingClientLevel;
import rooms.programming.level.ProgrammingDecisions;
import rooms.programming.level.ProgrammingLevel;
import rooms.programming.level.ProgrammingMethods;
import rooms.programming.level.ProgrammingTerminal;
import rooms.programming.network.ProgrammingSnapshotTranslator;

/** Entry point for the Programming 1 escape room. */
public final class Programming {

  private static final String LEVEL_KEY = "programming";
  private static final CharacterClass[] CHARACTER_CLASSES = {CharacterClass.THE_LAST_HOUR_CHAR03};
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  // The room has no savegame, so a decision lasts for one launch and is asked again on the next.
  private static final TrackingConsentFeature TRACKING_CONSENT =
      new TrackingConsentFeature(
          "programming-1", TRACKING_OPERATOR_EMAIL, () -> null, consent -> {});

  private Programming() {}

  /**
   * Starts the Programming 1 escape room or its level editor.
   *
   * @param args command-line arguments passed to the game starter
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    TRACKING_CONSENT.initialize(args, () -> null);

    ServerStarter server =
        ServerStarter.builder(Programming::serverSetup)
            .onConfigure(ProgrammingAchievements::register)
            .characterClasses(CHARACTER_CLASSES)
            .maximumPlayers(2)
            .levels(Tuple.of(LEVEL_KEY, ProgrammingLevel.class))
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new ProgrammingSnapshotTranslator())
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, Programming::clientSetup)
            .onConfigure(ProgrammingAchievements::register)
            .levels(Tuple.of(LEVEL_KEY, ProgrammingClientLevel.class))
            .initLocalization(Programming::initLocalization)
            .build();

    GameStarter game =
        GameStarter.builder("Das Erbe der Seelenweber", Programming.class)
            .language(Language.DE)
            .levelEditor("levels/programming")
            .startupConsent(TRACKING_CONSENT::startupPrompt)
            .trackingSettings(TRACKING_CONSENT::settings)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Registers the shared escape-room translations used by the menu and its consent dialogs. */
  private static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
  }

  private static void serverSetup() {
    ServerLifecycle.install("Programming server stopped");
    BlackFadeCutscene.register();
    QuestLogUI.register();
    ProgrammingTerminal.register();
    ProgrammingMethods.register();
    ProgrammingDecisions.register();
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.remove(AttributeBarSystem.class);
    ECSManagement.add(new CollisionSystem());
  }

  private static void clientSetup() {
    Game.stage().ifPresent(CursorUtil::initListener);
    BlackFadeCutscene.register();
    QuestLogUI.register();
    ProgrammingTerminal.register();
    ProgrammingMethods.register();
    ProgrammingDecisions.register();
  }
}
