package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.components.SkillComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.entities.HeroController;
import contrib.modules.emote.EmoteSystem;
import contrib.systems.AttributeBarSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.CursorUtil;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.settings.ClientSettings;
import java.io.IOException;
import java.util.Arrays;
import level.LastHourLevel;
import network.LastHourEntitySpawnStrategy;
import network.LastHourSnapshotTranslator;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class TheLastHour {

  private static final String SERVER_ARGUMENT = "--server";

  private static final String BACKGROUND_MUSIC = "sounds/forest_bgm.wav";
  private static Music backgroundMusic;

  /** Enable or disable debug mode, which adds extra systems for debugging and level editing. */
  public static final boolean DEBUG_MODE = false;

  private static final CharacterClass[] MULTIPLAYER_CHARACTER_CLASSES = {
    CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03
  };

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    boolean runMpServer = args != null && Arrays.asList(args).contains(SERVER_ARGUMENT);

    if (runMpServer) {
      Game.userOnFrame(TheLastHour::onFrame);
      PreRunConfiguration.multiplayerEnabled(true);
      PreRunConfiguration.isNetworkServer(true);
      PreRunConfiguration.multiplayerCharacterClasses(MULTIPLAYER_CHARACTER_CLASSES);
    }

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(false);
    Game.userOnSetup(() -> onUserSetup(runMpServer));
    Game.frameRate(60);
    Game.windowTitle("The Last Hour");
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.run();
  }

  private static void onUserSetup(boolean runMpServer) {
    if (runMpServer) {
      ECSManagement.add(new PositionSystem());
      ECSManagement.add(new VelocitySystem());
      ECSManagement.add(new FrictionSystem());
      ECSManagement.add(new MoveSystem());
      ECSManagement.remove(AttributeBarSystem.class);

      ECSManagement.system(
          LevelSystem.class,
          levelSystem ->
              levelSystem.onLevelLoad(
                  () -> {
                    GameLoop.onLevelLoad.execute();
                    Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                  }));
    } else {
      Entity hero =
          HeroBuilder.builder().characterClass(CharacterClass.THE_LAST_HOUR_CHAR03).build();
      hero.fetch(SkillComponent.class).ifPresent(SkillComponent::removeAll);
      Game.add(hero);
      Game.stage().ifPresent(CursorUtil::initListener);
      setupMusic();
    }

    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new EmoteSystem());

    if (DEBUG_MODE && !Game.isHeadless()) {
      ECSManagement.add(new Debugger());
      ECSManagement.add(new DebugDrawSystem());
      ECSManagement.add(new LevelEditorSystem());
    }
  }

  /**
   * Initializes and starts the background music for the game, and sets up listeners to adjust the
   * volume based on client settings changes.
   */
  public static void setupMusic() {
    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(
        ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);

    ClientSettings.setOnVolumeChange(
        (key, value) -> {
          if (key.equals(ClientSettings.MUSIC_VOLUME) || key.equals(ClientSettings.MASTER_VOLUME)) {
            backgroundMusic.setVolume(
                ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);
          }
        });
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
