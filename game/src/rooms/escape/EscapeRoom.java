package rooms.escape;

import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.game.ClientStarter;
import engine.game.ECSManagement;
import engine.game.GameStarter;
import engine.game.MainMenu;
import engine.game.ServerStarter;
import engine.language.Language;
import engine.level.DungeonLevel;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.time.PlayClock;
import engine.tracking.Tracking;
import engine.tracking.TrackingRuntime;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLoggerConfig;
import feature.audience.AudienceConfig;
import feature.audience.AudienceHud;
import feature.audience.AudienceSystem;
import feature.audience.RoomCamera;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.questlog.QuestLogUI;
import feature.survey.SurveyFeature;
import feature.systems.CollisionSystem;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Starts an escape room in audience mode. The game runs on a shared screen, and the audience steers
 * the hero and answers the questions by voting on their devices. The rooms differ only in the
 * values of this record; their levels extend {@link EscapeRoomLevel}.
 *
 * <p>The menu offers "Publikumsrunde starten". F4 in the main menu opens the level editor, which
 * runs without an audience and saves to {@code game/assets/levels/<roomId>}.
 *
 * @param roomId tracking room ID, level key and folder of the level file; the surveys are {@code
 *     surveys/pre-<roomId>.json} and {@code post-<roomId>.json}
 * @param title name of the room in the main menu
 * @param mainClass class whose {@code main} starts the room; the menu starts it again as the
 *     dedicated server
 * @param accentColor accent color of the main menu
 * @param level server level of the room
 * @param intro shows the intro once play begins
 * @param moveWindow how long a vote on where the hero goes stays open
 */
public record EscapeRoom(
    String roomId,
    String title,
    Class<?> mainClass,
    Color accentColor,
    Class<? extends DungeonLevel> level,
    Runnable intro,
    Duration moveWindow) {
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  // Time to discuss a question; once everybody voted, the window closes a few seconds later.
  private static final Duration PUZZLE_WINDOW = Duration.ofMinutes(2);

  private static AudienceSystem audience;
  // The level editor builds the level without an audience. It opens no relay session and no
  // overlay, and the keyboard moves the hero.
  private static boolean levelEditorMode;

  /**
   * Starts the room.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server
   */
  public void start(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();
    // Every device gives or declines consent in the pre survey; the game screen itself records
    // nothing.
    Tracking.configureRoom(roomId, TRACKING_OPERATOR_EMAIL, Optional.of(UUID.randomUUID()), true);
    TrackingRuntime.localTrackingConsent(true);
    Game.playClock().configure(1);

    ServerStarter server =
        ServerStarter.builder(this::serverSetup)
            .characterClasses(CharacterClass.THE_LAST_HOUR_ROGUE)
            .maximumPlayers(1)
            .levels(Tuple.of(roomId, level))
            .onConfigure(SurveyFeature::register)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, EscapeRoom::clientSetup)
            .levels(Tuple.of(roomId, DungeonLevel.class))
            .onConfigure(
                () -> {
                  SurveyFeature.register();
                  AudienceHud.register();
                })
            .build();

    GameStarter game =
        GameStarter.builder(title, mainClass)
            .accentColor(accentColor)
            .language(Language.DE)
            .showJoinButton(false)
            .askPlayerName(false)
            .hostActionLabel(() -> "Publikumsrunde starten")
            .levelEditor("game/assets/levels/" + roomId)
            .beforeLevelEditorStart(() -> levelEditorMode = true)
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Ends the game once the hero reached the exit. The devices switch to the post survey. */
  static void finished() {
    if (audience != null) audience.end(PlayClock.Outcome.SUCCESS);
  }

  private static void clientSetup() {
    if (!levelEditorMode) AudienceHud.install();
    QuestLogUI.disable();
    // The audience watches the whole room; the editor keeps its own camera.
    if (!levelEditorMode) Game.add(new RoomCamera());
  }

  private void serverSetup() {
    if (!levelEditorMode) {
      AudienceConfig defaults = AudienceConfig.defaults();
      AudienceConfig config =
          new AudienceConfig(
              defaults.relay(),
              defaults.relayKey(),
              moveWindow,
              PUZZLE_WINDOW,
              defaults.continueWindow(),
              defaults.countdown(),
              defaults.resultDisplay(),
              defaults.closeAfterEnd());
      audience = new AudienceSystem(config, intro, Game::complete);
      ECSManagement.add(audience);
    }
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new CollisionSystem());
  }
}
