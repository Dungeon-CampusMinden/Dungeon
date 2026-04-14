package core.game.loop;

import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.OverlayInteractionSelectionUi;
import core.camera.ClientCameraAdapter;
import core.game.*;
import core.game.startup.ClientStartup;
import core.game.render.EcsRenderScreen;
import core.input.bridge.ClientInputBridge;
import core.platform.Platform;
import core.platform.cursor.ClientCursorAdapter;
import core.platform.sound.ClientSoundPlayer;
import core.platform.runtime.ClientRuntimeAdapter;
import core.platform.window.ClientWindowEventsBridge;
import core.platform.window.ClientWindowAdapter;
import core.platform.render.ClientRenderAdapter;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.configuration.DisplayMode;

public final class ClientLoopRuntime {
  private ClientLoopRuntime() {}

  private static ISoundPlayer soundPlayer = new NoSoundPlayer();

  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  public static void run(String[] args, GameLoopCore loopCore) {
    syncDisplaySettings();

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

    ClientWindowEventsBridge.install();

    // init sound backend after engine init
    soundPlayer = PreRunConfiguration.disableAudio()
      ? new NoSoundPlayer()
      : new ClientSoundPlayer();

    // Bind platform adapters AFTER init so Game.window() etc. are available.
    Platform.window(new ClientWindowAdapter());
    Platform.runtime(new ClientRuntimeAdapter());
    Platform.render(new ClientRenderAdapter());
    Platform.cursor(new ClientCursorAdapter());
    Platform.camera(new ClientCameraAdapter());

    // Bridge LITIENGINE input events into our engine-agnostic InputManager.
    ClientInputBridge.install();

    // Install backend-neutral interaction selection UI for complex interactables.
    InteractionSelection.install(OverlayInteractionSelectionUi.INSTANCE);

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

          // Must be called once per frame to clear justPressed/justReleased.
          InputManager.update();
        });

    Game.start();
  }

  private static void syncDisplaySettings() {
    Game.config().load();
    Game.config().client().setMaxFps(PreRunConfiguration.frameRate());
    Game.config().graphics().setDisplayMode(
      PreRunConfiguration.fullScreen() ? DisplayMode.FULLSCREEN : DisplayMode.WINDOWED);
    Game.config().graphics().setResolutionWidth(PreRunConfiguration.windowWidth());
    Game.config().graphics().setResolutionHeight(PreRunConfiguration.windowHeight());
    Game.config().save();
  }
}
