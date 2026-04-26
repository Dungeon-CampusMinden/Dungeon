package core.platform.client.loop;

import core.game.ECSManagement;
import core.game.GameRuntime;
import core.game.PreRunConfiguration;
import core.game.SystemProfile;
import core.game.loop.GameLoop;
import core.game.loop.GameLoopHost;
import core.platform.client.adapters.*;
import core.platform.client.render.EcsRenderScreen;
import core.game.startup.ClientStartup;
import core.platform.Platform;
import core.platform.client.audio.ClientSoundPlayer;
import core.platform.client.input.ClientInputBridge;
import core.platform.client.window.ClientShutdownExceptionFilter;
import core.platform.client.window.ClientWindowEventsBridge;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.platform.client.ui.ClientStageHandle;
import core.ui.StageHandle;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.configuration.DisplayMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A platform-specific implementation of {@link GameLoopHost} for client-side runtime environments.
 *
 * <p>The {@code ClientLoopHost} manages initialization, configuration, and runtime systems
 * required for running a game client. This includes setting up rendering, input,
 * sound playback, and other platform-specific resources.
 *
 * <p>It integrates the game loop into the client runtime environment. Optional gameplay,
 * debugging, or user interface systems can be supplied explicitly through
 * {@link ClientLoopHostInstaller} implementations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Initializes the game engine and core runtime systems.</li>
 *   <li>Configures platform-specific services, such as rendering, input, and audio playback.</li>
 *   <li>Manages runtime systems for HUD, gameplay extensions, and debugging.</li>
 *   <li>Provides access to a client-side {@link StageHandle} for managing UI components.</li>
 *   <li>Offers a configurable sound player through the {@code soundPlayer()} method.</li>
 * </ul>
 *
 * <p>This class is designed to be a final implementation and should not be extended.
 */
public final class ClientLoopHost implements GameLoopHost {
  private final List<ClientLoopHostInstaller> installers;
  private ISoundPlayer soundPlayer = new NoSoundPlayer();

  /**
   * Creates a client loop host without optional installers.
   */
  public ClientLoopHost() {
    this(Collections.emptyList());
  }

  /**
   * Creates a client loop host with explicit installers.
   *
   * @param installers installers that extend the client startup
   */
  public ClientLoopHost(ClientLoopHostInstaller... installers) {
    this(Arrays.asList(installers));
  }

  /**
   * Creates a client loop host with explicit installers.
   *
   * @param installers installers that extend the client startup
   */
  public ClientLoopHost(Collection<? extends ClientLoopHostInstaller> installers) {
    this.installers = List.copyOf(Objects.requireNonNull(installers, "installers must not be null"));
  }

  @Override
  public ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  @Override
  public void run(String[] args, GameLoop loop) {
    initializeEngine(args);
    wirePlatformServices();
    initializeClientRuntime(loop);
    Game.start();
  }

  @Override
  public Optional<StageHandle> stage() {
    return Optional.of(new ClientStageHandle());
  }

  private void initializeEngine(String[] args) {
    syncDisplaySettings();
    registerRenderScreenListener();
    Game.init(args);
  }

  private void registerRenderScreenListener() {
    Game.addGameListener(
      new GameListener() {
        @Override
        public void initialized(String... initArgs) {
          Game.screens().add(new EcsRenderScreen());
          Game.screens().display(EcsRenderScreen.NAME);
        }
      });
  }

  private void wirePlatformServices() {
    ClientShutdownExceptionFilter.install();

    Platform.camera(new ClientCameraAdapter());
    Platform.cursor(new ClientCursorAdapter());
    Platform.render(new ClientRenderAdapter());
    Platform.runtime(new ClientRuntimeAdapter());
    Platform.window(new ClientWindowAdapter());
    Platform.clipboard(new ClientClipboardAdapter());
    ClientWindowEventsBridge.install();

    soundPlayer =
      PreRunConfiguration.disableAudio() ? new NoSoundPlayer() : new ClientSoundPlayer();

    ClientInputBridge.install();
    installers.forEach(ClientLoopHostInstaller::installPlatformServices);
    InputManager.reset();
  }

  private void initializeClientRuntime(GameLoop loopCore) {
    ECSManagement.initializeDefaultSystems(SystemProfile.CLIENT);
    ECSManagement.initializeGameplaySystems(SystemProfile.CLIENT);
    installers.forEach(ClientLoopHostInstaller::installRuntimeSystems);
    ECSManagement.system(
      core.systems.LevelSystem.class,
      levelSystem -> levelSystem.onLevelLoad(GameRuntime.onLevelLoad));

    ClientStartup.setupAndLoadInitialLevelOnce();

    Game.loop()
      .attach(
        () -> {
          final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

          core.Game.soundPlayer().update(deltaSeconds);
          loopCore.beforeRender();
          loopCore.tick(deltaSeconds, false);
          InputManager.update();
        });
  }

  private void syncDisplaySettings() {
    Game.config().load();
    Game.config().client().setMaxFps(PreRunConfiguration.frameRate());
    Game.config().graphics().setDisplayMode(
      PreRunConfiguration.fullScreen() ? DisplayMode.FULLSCREEN : DisplayMode.WINDOWED);
    Game.config().graphics().setResolutionWidth(PreRunConfiguration.windowWidth());
    Game.config().graphics().setResolutionHeight(PreRunConfiguration.windowHeight());
    Game.config().save();
  }
}
