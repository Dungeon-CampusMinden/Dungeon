package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.level.loader.DungeonLoader;
import core.network.input.InputCommandRouter;
import core.network.messages.c2s.InputMessage;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import demoDungeon.level.Level01;
import hint.HintLogComponent;
import hint.HintLogDialog;
import java.io.IOException;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class DemoRoom {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DemoRoom.class);

  private static final boolean DEBUG_MODE = false;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.windowTitle("Demo-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnLevelLoad(firstLoad -> bindLocalHintLogInput());
    Game.userOnSetup(
        () -> {
          setupMusic();
          registerServerHintLogCommand();
          DungeonLoader.addLevel(Tuple.of("demo", Level01.class));
          createSystems();
          createHero();
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  /**
   * Binds the local `T` key to send the hint-log command.
   *
   * <p>The key binding is only installed when the local player currently has a {@link
   * HintLogComponent}. Existing `T` bindings are removed first to avoid duplicates when levels
   * change.
   */
  private static void bindLocalHintLogInput() {
    Game.player()
        .ifPresent(
            player ->
                player
                    .fetch(InputComponent.class)
                    .ifPresent(
                        inputComponent -> {
                          inputComponent.removeCallback(Input.Keys.T);
                          if (player.fetch(HintLogComponent.class).isEmpty()) {
                            return;
                          }
                          inputComponent.registerCallback(
                              Input.Keys.T,
                              ignored ->
                                  Game.network()
                                      .sendInput(InputMessage.custom("escapeRoom:hint_log.open")),
                              false,
                              true);
                          LOGGER.debug(
                              "Registered hint-log key binding for player {}", player.id());
                        }));
  }

  /**
   * Registers the server-side command handler for opening the hint log.
   *
   * <p>Re-registering replaces the previous handler from this source.
   */
  private static void registerServerHintLogCommand() {
    InputCommandRouter.register(
        "escapeRoom:hint_log.open",
        true,
        context ->
            context
                .playerEntity()
                .fetch(HintLogComponent.class)
                .ifPresent(
                    hintLog -> HintLogDialog.showHintLog(hintLog, context.playerEntity().id())));
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.HUNTER);
    hero.add(new HintLogComponent());
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(30);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
