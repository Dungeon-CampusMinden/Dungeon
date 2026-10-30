package rooms.ki;

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
 * An escape room about machine learning and decision trees, played in audience mode. The game runs
 * on a shared screen, and the audience steers the hero and answers the questions by voting on their
 * devices. Instead of single steps, the audience picks where the hero goes from a list of targets
 * that grows as the story goes on.
 *
 * <p>The hero is trapped inside a decision tree. Five terminals ask about machine learning, CAL2,
 * entropy, ID3 and random forests; each reveals one digit of the door code. Start it with the
 * Gradle task {@code runKiRoom}, then choose "Publikumsrunde starten" in the menu. F4 in the main
 * menu opens the level editor.
 */
public final class KiRoom {
  /** Tracking room ID; the surveys are {@code surveys/pre-ki.json} and {@code post-ki.json}. */
  public static final String ROOM_ID = "ki";

  private static final String LEVEL_KEY = "ki";
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  private static final Color MENU_ACCENT_COLOR = Color.valueOf("4cc38aff");
  // The audience picks a target from a list, which takes longer to read than an arrow; once
  // everybody voted, the window closes three seconds later.
  private static final Duration MOVE_WINDOW = Duration.ofSeconds(15);
  // Time to discuss a question; once everybody voted, the window closes a few seconds later.
  private static final Duration PUZZLE_WINDOW = Duration.ofMinutes(2);

  private static AudienceSystem audience;
  // The level editor (F4 in the main menu) builds the level without an audience. It opens no relay
  // session and no overlay, and the keyboard moves the hero.
  private static boolean levelEditorMode;

  private KiRoom() {}

  /**
   * Starts the room.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.builder()
        .consoleLevel(Level.WARNING)
        .enableConsole(true)
        .enableFile(false)
        .build();
    // Every device gives or declines consent in the pre survey; the game screen itself records
    // nothing.
    Tracking.configureRoom(ROOM_ID, TRACKING_OPERATOR_EMAIL, Optional.of(UUID.randomUUID()), true);
    TrackingRuntime.localTrackingConsent(true);
    Game.playClock().configure(1);

    ServerStarter server =
        ServerStarter.builder(KiRoom::serverSetup)
            .characterClasses(CharacterClass.THE_LAST_HOUR_ROGUE)
            .maximumPlayers(1)
            .levels(Tuple.of(LEVEL_KEY, KiRoomLevel.class))
            .onConfigure(SurveyFeature::register)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, KiRoom::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, DungeonLevel.class))
            .onConfigure(
                () -> {
                  SurveyFeature.register();
                  AudienceHud.register();
                })
            .build();

    GameStarter game =
        GameStarter.builder("Gefangen im Entscheidungsbaum", KiRoom.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.DE)
            .showJoinButton(false)
            .askPlayerName(false)
            .hostActionLabel(() -> "Publikumsrunde starten")
            .levelEditor("levels/ki")
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

  private static void serverSetup() {
    if (!levelEditorMode) {
      AudienceConfig defaults = AudienceConfig.defaults();
      AudienceConfig config =
          new AudienceConfig(
              defaults.relay(),
              defaults.relayKey(),
              MOVE_WINDOW,
              PUZZLE_WINDOW,
              defaults.continueWindow(),
              defaults.countdown(),
              defaults.resultDisplay(),
              defaults.closeAfterEnd());
      audience = new AudienceSystem(config, KiRoomLevel::intro, Game::complete);
      ECSManagement.add(audience);
    }
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new CollisionSystem());
  }
}
