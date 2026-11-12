package rooms.morphco;

import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.PreRunConfiguration;
import engine.game.ServerLifecycle;
import engine.game.ServerProcess;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.language.Localization;
import engine.network.server.CharacterSelection;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.tracking.TrackingConsentFeature;
import engine.utils.Tuple;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.Debugger;
import feature.credits.CreditsFeature;
import feature.emote.EmoteSystem;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.input.systems.ControlsDialogSystem;
import feature.petrinet.PetriNetSystem;
import feature.questlog.QuestLogUI;
import feature.survey.SurveyFeature;
import feature.systems.AttributeBarSystem;
import feature.systems.CollisionSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import feature.timer.WorldTimerSystem;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.level.MorphCoClientLevel;
import rooms.morphco.level.MorphCoLevel;
import rooms.morphco.level.MorphCoObjectPrefab;
import rooms.morphco.network.MorphCoSnapshotTranslator;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.save.MorphCoSave;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTranslator;

/**
 * Entry point of the escape room "Morph & Co. KG - Die Wörterfabrik".
 *
 * <p>A morphology room for one or two players: the word factory of Morph & Co. KG has stopped its
 * production. Players switch off the alarm, read the production control system, repair the affix
 * sorter A-12 and the inflection line F-04 in any order and restart the production.
 */
public final class MorphCo {

  /** Room ID used by tracking, surveys and the credits file. */
  public static final String ROOM_ID = "morph-co";

  /** Room ID of the optional credits file {@code credits/morph-co.json}. */
  public static final String CREDITS_ROOM_ID = ROOM_ID;

  private static final String LEVEL_KEY = "morphco";
  private static final String MENU_BACKGROUND_IMAGE = "rooms/morphCo/art/menu_background.png";
  private static final Color MENU_ACCENT_COLOR = new Color(0.93f, 0.62f, 0.2f, 1f);
  private static final int MINIMUM_PLAYERS = 1;
  private static final int MAXIMUM_PLAYERS = 2;
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  private static final String NEW_GAME_ARGUMENT = "--new-morph-co";
  private static final String LOAD_SAVE_ARGUMENT = "--load-morph-co";
  private static final CharacterClass[] CHARACTER_CLASSES = {
    CharacterClass.TECHNICIAN,
    CharacterClass.THE_LAST_HOUR_ROGUE,
    CharacterClass.THE_LAST_HOUR_CHAR03
  };

  private static boolean debugMode;
  private static boolean autoplay;
  private static Optional<MorphCoSave.SaveData> continuation = Optional.empty();
  private static UUID runId = UUID.randomUUID();
  private static final TrackingConsentFeature TRACKING_CONSENT =
      new TrackingConsentFeature(
          ROOM_ID, TRACKING_OPERATOR_EMAIL, () -> runId, MorphCoSave::updateTrackingConsent);

  private MorphCo() {}

  /**
   * Starts the room, its dedicated server or its level editor.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server, {@code
   *     --leveleditor} the level editor and {@code --debug} enables debug systems
   */
  public static void main(String[] args) {
    MorphCoObjectPrefab.register();
    List<String> arguments = Arrays.asList(args);
    autoplay = arguments.contains("--autoplay");
    debugMode = autoplay || arguments.contains("--debug") || arguments.contains("--leveleditor");
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();

    if (arguments.contains(NEW_GAME_ARGUMENT)) MorphCoSave.delete();
    continuation = arguments.contains(LOAD_SAVE_ARGUMENT) ? MorphCoSave.read() : Optional.empty();
    if (arguments.contains(LOAD_SAVE_ARGUMENT)) {
      CharacterSelection.restoreAssignments(
          continuation.map(MorphCoSave.SaveData::characterAssignments).orElse(Map.of()));
    } else if (arguments.contains(NEW_GAME_ARGUMENT)
        || arguments.contains(ServerProcess.SERVER_ARGUMENT)) {
      CharacterSelection.clear();
    }
    runId = continuation.map(MorphCoSave.SaveData::runId).orElseGet(UUID::randomUUID);
    Game.playClock().configure(MINIMUM_PLAYERS);
    continuation.ifPresent(save -> Game.playClock().restore(save.activeMs()));
    restoreSavedPlayerNameForMenu();
    TRACKING_CONSENT.initialize(
        args, () -> continuation.map(MorphCoSave.SaveData::trackingConsent).orElse(null));

    ServerStarter server =
        ServerStarter.builder(MorphCo::serverSetup)
            .onConfigure(MorphCo::registerContent)
            .characterClasses(CHARACTER_CLASSES)
            .maximumPlayers(MAXIMUM_PLAYERS)
            .levels(Tuple.of(LEVEL_KEY, MorphCoLevel.class))
            .snapshotTranslator(new MorphCoSnapshotTranslator())
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, MorphCo::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, MorphCoClientLevel.class))
            .initLocalization(MorphCo::initLocalization)
            .onConfigure(MorphCo::registerContent)
            .build();

    GameStarter game =
        GameStarter.builder("Morph & Co. KG", MorphCo.class)
            .backgroundImage(MENU_BACKGROUND_IMAGE)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.DE)
            .levelEditor("levels/morphCo")
            .hostActionLabel(() -> MorphCoText.text("menu.start"))
            .serverArguments(ServerProcess.SERVER_ARGUMENT, NEW_GAME_ARGUMENT)
            .continueGame(MorphCoSave::exists, ServerProcess.SERVER_ARGUMENT, LOAD_SAVE_ARGUMENT)
            .creditsRoomId(CREDITS_ROOM_ID)
            .startupConsent(TRACKING_CONSENT::startupPrompt)
            .trackingSettings(TRACKING_CONSENT::settings)
            .build();

    MainMenu.run(args, game, client, server);
  }

  // The menu offers the name of the saved run, so Continue keeps the hero's name.
  private static void restoreSavedPlayerNameForMenu() {
    MorphCoSave.read()
        .map(MorphCoSave.SaveData::playerName)
        .filter(name -> name != null && !name.isBlank() && !name.contains("_"))
        .ifPresent(
            name -> {
              try {
                PreRunConfiguration.username(name);
              } catch (IllegalArgumentException ignored) {
                // A malformed name must not prevent the room from starting.
              }
            });
  }

  /**
   * Returns the checkpoint requested by this server launch.
   *
   * @return saved checkpoint, or empty for a new game
   */
  public static Optional<MorphCoSave.SaveData> checkpoint() {
    return continuation;
  }

  /**
   * Writes a checkpoint on the authoritative server.
   *
   * @param milestones completed milestones
   * @param flags observation flags
   * @param assembled assembled circumfixes
   * @param leverPulls pulls on the stuck lever
   * @param affixAttempts checked answers of the affix step
   */
  public static void saveCheckpoint(
      Set<MorphCoMilestone> milestones,
      Set<String> flags,
      Set<CircumfixPuzzle.Circumfix> assembled,
      int leverPulls,
      int affixAttempts) {
    if (Game.isMultiplayerClient()) return;
    MorphCoSave.write(
        new MorphCoSave.SaveData(
            milestones,
            flags,
            assembled,
            leverPulls,
            affixAttempts,
            runId,
            Game.playClock().activeMs(),
            TRACKING_CONSENT.decision(),
            MorphCoSave.currentPlayerName(),
            MorphCoSave.currentCharacterAssignments()));
  }

  /**
   * Returns whether debug systems are enabled for this process.
   *
   * @return {@code true} for {@code --debug} and the level editor
   */
  public static boolean debugMode() {
    return debugMode;
  }

  /**
   * Returns whether the debug playthrough runs, see {@code runMorphCoAutoplay}.
   *
   * @return {@code true} for {@code --autoplay}
   */
  public static boolean autoplay() {
    return autoplay;
  }

  /** Registers the shared escape-room texts, the room texts and the room translator. */
  public static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
    localization.registerTranslationFile(Language.DE, "language/morphCo/de.json");
    localization.registerTranslationFile(Language.EN, "language/morphCo/en.json");
    localization.setCurrentTranslator(new MorphCoTranslator());
  }

  /** Registers texts, achievements, dialogs and items on the server and on every client. */
  static void registerContent() {
    initLocalization();
    MorphCoAchievements.register();
    BlackFadeCutscene.register();
    CreditsFeature.register();
    SurveyFeature.register();
    QuestLogUI.register();
    MorphCoDialogs.register();
    CircumfixFragmentItem.ensureRegistration();
  }

  private static void serverSetup() {
    ServerLifecycle.install("Morph & Co. server stopped");
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.remove(AttributeBarSystem.class);
    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());
    ECSManagement.add(new PetriNetSystem());
    if (debugMode && !Game.isHeadless()) addDebugSystems();
  }

  private static void clientSetup() {
    Game.remove(AttributeBarSystem.class);
    Game.add(new ControlsDialogSystem(MorphCoText::controls));
    if (!Game.isHeadless()) Game.add(new WorldTimerSystem());
    if (debugMode) addDebugSystems();
  }

  private static void addDebugSystems() {
    Game.add(new Debugger());
    Game.add(new DebugDrawSystem());
    Game.add(new LevelEditorSystem());
  }
}
