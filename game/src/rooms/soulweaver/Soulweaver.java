package rooms.soulweaver;

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
import rooms.soulweaver.level.SoulweaverClientLevel;
import rooms.soulweaver.level.SoulweaverDecisions;
import rooms.soulweaver.level.SoulweaverLevel;
import rooms.soulweaver.level.SoulweaverMethods;
import rooms.soulweaver.level.SoulweaverTablePrefab;
import rooms.soulweaver.level.SoulweaverTerminal;
import rooms.soulweaver.network.SoulweaverSnapshotTranslator;
import rooms.soulweaver.save.SoulweaverSave;
import rooms.soulweaver.state.SoulweaverPhase;

/** Entry point for the Soulweaver escape room. */
public final class Soulweaver {

  private static final String LEVEL_KEY = "soulweaver";
  private static final CharacterClass[] CHARACTER_CLASSES = {CharacterClass.THE_LAST_HOUR_CHAR03};
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  private static final String NEW_GAME_ARGUMENT = "--new-soulweaver";
  private static final String LOAD_SAVE_ARGUMENT = "--load-soulweaver";
  private static Optional<SoulweaverSave.SaveData> continuation = Optional.empty();
  private static UUID runId = UUID.randomUUID();
  // Checkpoints retain the run identity and its consent decision.
  private static final TrackingConsentFeature TRACKING_CONSENT =
      new TrackingConsentFeature(
          "soulweaver",
          TRACKING_OPERATOR_EMAIL,
          () -> runId,
          SoulweaverSave::updateTrackingConsent);

  private Soulweaver() {}

  /**
   * Starts the Soulweaver escape room or its level editor.
   *
   * @param args command-line arguments passed to the game starter
   */
  public static void main(String[] args) {
    SoulweaverTablePrefab.register();
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    var arguments = Arrays.asList(args);
    if (arguments.contains(NEW_GAME_ARGUMENT)) SoulweaverSave.delete();
    continuation =
        arguments.contains(LOAD_SAVE_ARGUMENT) ? SoulweaverSave.read() : Optional.empty();
    runId = continuation.map(SoulweaverSave.SaveData::runId).orElseGet(UUID::randomUUID);
    TRACKING_CONSENT.initialize(
        args, () -> continuation.map(SoulweaverSave.SaveData::trackingConsent).orElse(null));

    ServerStarter server =
        ServerStarter.builder(Soulweaver::serverSetup)
            .onConfigure(SoulweaverAchievements::register)
            .characterClasses(CHARACTER_CLASSES)
            .maximumPlayers(2)
            .levels(Tuple.of(LEVEL_KEY, SoulweaverLevel.class))
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .snapshotTranslator(new SoulweaverSnapshotTranslator())
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, Soulweaver::clientSetup)
            .onConfigure(SoulweaverAchievements::register)
            .levels(Tuple.of(LEVEL_KEY, SoulweaverClientLevel.class))
            .initLocalization(Soulweaver::initLocalization)
            .build();

    GameStarter game =
        GameStarter.builder("Das Erbe der Seelenweber", Soulweaver.class)
            .language(Language.DE)
            .levelEditor("levels/soulweaver")
            .serverArguments(ServerProcess.SERVER_ARGUMENT, NEW_GAME_ARGUMENT)
            .continueGame(SoulweaverSave::exists, ServerProcess.SERVER_ARGUMENT, LOAD_SAVE_ARGUMENT)
            .startupConsent(TRACKING_CONSENT::startupPrompt)
            .trackingSettings(TRACKING_CONSENT::settings)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /**
   * @return the checkpoint requested by this server launch
   */
  public static Optional<SoulweaverPhase> checkpoint() {
    return continuation.map(SoulweaverSave.SaveData::phase);
  }

  /**
   * Saves the beginning of an act on the authoritative server.
   *
   * @param phase act to resume
   */
  public static void saveCheckpoint(SoulweaverPhase phase) {
    if (Game.isMultiplayerClient()) return;
    SoulweaverSave.write(new SoulweaverSave.SaveData(phase, runId, TRACKING_CONSENT.decision()));
  }

  /** Registers the shared escape-room translations used by the menu and its consent dialogs. */
  private static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
  }

  private static void serverSetup() {
    ServerLifecycle.install("Soulweaver server stopped");
    BlackFadeCutscene.register();
    QuestLogUI.register();
    SoulweaverTerminal.register();
    SoulweaverMethods.register();
    SoulweaverDecisions.register();
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
    SoulweaverTerminal.register();
    SoulweaverMethods.register();
    SoulweaverDecisions.register();
  }
}
