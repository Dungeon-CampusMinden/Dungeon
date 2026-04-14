package core.game.loop;

import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.OverlayInteractionSelectionUi;
import core.camera.ClientCameraAdapter;
import core.game.*;
import core.game.startup.ClientStartup;
import core.game.render.EcsRenderScreen;
import core.input.bridge.ClientInputBridge;
import core.platform.Platform;
import core.platform.client.ClientCursorAdapter;
import core.platform.client.ClientSoundPlayer;
import core.platform.client.ClientRuntimeAdapter;
import core.platform.client.ClientWindowEventsBridge;
import core.platform.client.ClientWindowAdapter;
import core.platform.client.ClientRenderAdapter;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.configuration.DisplayMode;

/**
 * Manages the client-side game loop runtime.
 *
 * <p>This class is responsible for initializing and running the client-side game loop,
 * including setting up the game engine, binding platform adapters, configuring audio,
 * input handling, and starting ECS systems.
 *
 * @see GameLoopCore
 * @see Platform
 * @see ECSManagement
 */
public final class ClientLoopRuntime {
  private ClientLoopRuntime() {}

  private static ISoundPlayer soundPlayer = new NoSoundPlayer();

  /**
   * Returns the current sound player instance.
   *
   * @return the ISoundPlayer instance used for audio playback
   */
  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  /**
   * Runs the client-side game loop with the specified arguments and loop core.
   *
   * <p>Initializes the game engine, sets up platform adapters, configures audio,
   * input handling, and ECS systems. This method blocks until the game exits.
   *
   * @param args command line arguments to pass to the game engine
   * @param loopCore the core game loop handler for tick and render operations
   */
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
            Game.log().severe("Failed to set up render screen in initialized(): " + e.getMessage());
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
            Game.log().severe("Failed to display render screen in started(): " + e.getMessage());
          }
        }
      });

    // Initialize the game engine
    Game.init(args);

    ClientWindowEventsBridge.install();

    // Initialize sound backend after engine init
    soundPlayer = PreRunConfiguration.disableAudio()
      ? new NoSoundPlayer()
      : new ClientSoundPlayer();

    // Bind platform adapters AFTER init so Game.window() etc. are available
    Platform.window(new ClientWindowAdapter());
    Platform.runtime(new ClientRuntimeAdapter());
    Platform.render(new ClientRenderAdapter());
    Platform.cursor(new ClientCursorAdapter());
    Platform.camera(new ClientCameraAdapter());

    // Bridge game engine input events into the InputManager
    ClientInputBridge.install();

    // Install backend-neutral interaction selection UI for complex interactable objects
    InteractionSelection.install(OverlayInteractionSelectionUi.INSTANCE);

    // Ensure a start with a clean input state
    InputManager.reset();

    // Host chooses which default systems exist
    ECSManagement.initializeDefaultSystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.initializeGameplaySystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.system(core.systems.LevelSystem.class, ls -> ls.onLevelLoad(GameLoop.onLevelLoad));

    ClientStartup.setupAndLoadInitialLevelOnce();

    // Drive ECS tick from game engine update loop.
    Game.loop()
      .attach(
        () -> {
          final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

          core.Game.soundPlayer().update(deltaSeconds);
          loopCore.beforeRender();
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
