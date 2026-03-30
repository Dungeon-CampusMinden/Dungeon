package core.game.litiengine;

import core.configuration.KeyboardConfig;
import core.game.*;
import core.game.bootstrap.ClientStartup;
import core.platform.Platform;
import core.platform.litiengine.*;
import core.platform.litiengine.input.LitiengineCursorAdapter;
import core.platform.litiengine.sound.LitiengineSoundPlayer;
import core.platform.litiengine.window.LitiengineWindowEventsBridge;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;

public final class LitiengineGameLoopHost {
  private LitiengineGameLoopHost() {}

  private static ISoundPlayer soundPlayer = new NoSoundPlayer();

  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  public static void run(String[] args, GameLoopCore loopCore) {
    // Register lifecycle listener BEFORE init so we can safely set up screens at the right time.
    Game.addGameListener(
      new GameListener() {
        @Override
        public void initialized(String... initArgs) {
          try {
            if (Game.screens() != null) {
              Game.screens().add(new EcsRenderScreen());
              Game.screens().display(EcsRenderScreen.NAME);
            }
          } catch (Exception e) {
            Game.log().severe("Failed to set up debug screen in initialized(): " + e.getMessage());
          }
        }

        @Override
        public void started() {
          // Safety net: ensure a current screen exists once the game has started.
          try {
            if (Game.screens() != null && Game.screens().current() == null) {
              Game.screens().display(EcsRenderScreen.NAME);
            }
          } catch (Exception e) {
            Game.log().severe("Failed to display debug screen in started(): " + e.getMessage());
          }
        }
      });

    // Initialize LITIENGINE
    Game.init(args);

    LitiengineWindowEventsBridge.install();

    // init sound backend after engine init
    soundPlayer = PreRunConfiguration.disableAudio()
      ? new NoSoundPlayer()
      : new LitiengineSoundPlayer();

    // Bind platform adapters AFTER init so Game.window() etc. are available.
    Platform.window(new LitiengineWindowAdapter());
    Platform.runtime(new LitiengineRuntimeAdapter());
    Platform.render(new LitiengineRenderAdapter());
    Platform.cursor(new LitiengineCursorAdapter());
    Platform.camera(new LitiengineCameraAdapter());

    // Bridge LITIENGINE input events into our engine-agnostic InputManager.
    LitiengineInputBridge.install();

    // Ensure we start with a clean input state.
    InputManager.reset();

    // Host chooses which default systems exist
    ECSManagement.bootstrapDefaultSystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.bootstrapGameplaySystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.system(core.systems.LevelSystem.class, ls -> ls.onLevelLoad(GameLoop.onLevelLoad));

    ClientStartup.setupAndLoadInitialLevelOnce();

    // Drive ECS tick from LITIENGINE update loop.
    Game.loop()
      .attach(
        () -> {
          final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

          core.Game.soundPlayer().update(deltaSeconds);
          loopCore.beforeRender(deltaSeconds);
          loopCore.tick(deltaSeconds, false);

          fullscreenKey();

          // Must be called once per frame to clear justPressed/justReleased.
          InputManager.update();
        });

    Game.start();
  }

  private static void fullscreenKey() {
    if (InputManager.isKeyJustPressed(KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
      Platform.window().toggleFullscreen();
    }
  }
}
