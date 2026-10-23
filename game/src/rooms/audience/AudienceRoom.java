package rooms.audience;

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
import engine.systems.CameraSystem;
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
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.questlog.QuestLogUI;
import feature.survey.SurveyFeature;
import feature.systems.CollisionSystem;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A small escape room for audience mode: the game runs on a shared screen and the audience steers
 * the hero and answers the puzzles by voting on their devices.
 *
 * <p>The room holds placeholder puzzles of every kind the devices support: single choice, multiple
 * choice, free text, and a code. Start it with the Gradle task {@code runAudienceRoom}, then choose
 * "Publikumsrunde starten" in the menu.
 */
public final class AudienceRoom {
  /**
   * Tracking room ID; the surveys are {@code surveys/pre-audience.json} and {@code
   * post-audience.json}.
   */
  public static final String ROOM_ID = "audience";

  private static final String LEVEL_KEY = "audience";
  private static final String TRACKING_OPERATOR_EMAIL = "amatutat@hsbi.de";
  private static final Color MENU_ACCENT_COLOR = Color.valueOf("f0b43cff");
  private static final float SCREEN_ZOOM = 0.22f;

  private static AudienceSystem audience;

  private AudienceRoom() {}

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
        ServerStarter.builder(AudienceRoom::serverSetup)
            .characterClasses(CharacterClass.WIZARD)
            .maximumPlayers(1)
            .levels(Tuple.of(LEVEL_KEY, AudienceRoomLevel.class))
            .onConfigure(SurveyFeature::register)
            .config(
                new SimpleIPath("dungeon_config.json"),
                feature.input.configuration.KeyboardConfig.class,
                KeyboardConfig.class)
            .onFrame(HeroController::drainAndApplyInputs)
            .build();

    ClientStarter client =
        ClientStarter.builder(server, AudienceRoom::clientSetup)
            .levels(Tuple.of(LEVEL_KEY, DungeonLevel.class))
            .onConfigure(
                () -> {
                  SurveyFeature.register();
                  AudienceHud.register();
                })
            .build();

    GameStarter game =
        GameStarter.builder("Publikums-Escape-Room", AudienceRoom.class)
            .accentColor(MENU_ACCENT_COLOR)
            .language(Language.DE)
            .showJoinButton(false)
            .askPlayerName(false)
            .hostActionLabel(() -> "Publikumsrunde starten")
            .build();

    MainMenu.run(args, game, client, server);
  }

  /** Ends the game: the hero reached the exit. The devices switch to the post survey. */
  static void finished() {
    if (audience != null) audience.end(PlayClock.Outcome.SUCCESS);
  }

  private static void clientSetup() {
    AudienceHud.install();
    QuestLogUI.disable();
    // The whole room fits on the game screen; the default zoom shows it at half the size.
    CameraSystem.camera().zoom = SCREEN_ZOOM;
  }

  private static void serverSetup() {
    audience =
        new AudienceSystem(AudienceConfig.defaults(), AudienceRoomLevel::intro, Game::complete);
    ECSManagement.add(audience);
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new CollisionSystem());
  }
}
