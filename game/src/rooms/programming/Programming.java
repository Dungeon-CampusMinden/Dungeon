package rooms.programming;

import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerLifecycle;
import engine.game.ServerProcess;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import rooms.programming.level.ProgrammingClientLevel;
import rooms.programming.level.ProgrammingDecisions;
import rooms.programming.level.ProgrammingLevel;
import rooms.programming.level.ProgrammingMethods;
import rooms.programming.level.ProgrammingTerminal;
import rooms.programming.network.ProgrammingSnapshotTranslator;
import rooms.programming.save.ProgrammingSave;
import rooms.programming.state.ProgrammingPhase;

/** Entry point for the Programming 1 escape room. */
public final class Programming {

  private static final String LEVEL_KEY = "programming";
  private static final CharacterClass[] CHARACTER_CLASSES = {CharacterClass.THE_LAST_HOUR_CHAR03};
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  private static final String NEW_GAME_ARGUMENT = "--new-programming";
  private static final String LOAD_SAVE_ARGUMENT = "--load-programming";
  private static Optional<ProgrammingSave.SaveData> continuation = Optional.empty();
  private static UUID runId = UUID.randomUUID();
  // Checkpoints retain the run identity and its consent decision.
  private static final TrackingConsentFeature TRACKING_CONSENT =
      new TrackingConsentFeature(
          "programming-1",
          TRACKING_OPERATOR_EMAIL,
          () -> runId,
          ProgrammingSave::updateTrackingConsent);

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

    var arguments = Arrays.asList(args);
    if (arguments.contains(NEW_GAME_ARGUMENT)) ProgrammingSave.delete();
    continuation =
        arguments.contains(LOAD_SAVE_ARGUMENT) ? ProgrammingSave.read() : Optional.empty();
    runId = continuation.map(ProgrammingSave.SaveData::runId).orElseGet(UUID::randomUUID);
    TRACKING_CONSENT.initialize(
        args, () -> continuation.map(ProgrammingSave.SaveData::trackingConsent).orElse(null));

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
            .serverArguments(ServerProcess.SERVER_ARGUMENT, NEW_GAME_ARGUMENT)
            .continueGame(
                ProgrammingSave::exists, ServerProcess.SERVER_ARGUMENT, LOAD_SAVE_ARGUMENT)
            .startupConsent(TRACKING_CONSENT::startupPrompt)
            .trackingSettings(TRACKING_CONSENT::settings)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /**
   * @return the checkpoint requested by this server launch
   */
  public static Optional<ProgrammingPhase> checkpoint() {
    return continuation.map(ProgrammingSave.SaveData::phase);
  }

  /**
   * Saves the beginning of an act on the authoritative server.
   *
   * @param phase act to resume
   */
  public static void saveCheckpoint(ProgrammingPhase phase) {
    if (Game.isMultiplayerClient()) return;
    ProgrammingSave.write(new ProgrammingSave.SaveData(phase, runId, TRACKING_CONSENT.decision()));
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
