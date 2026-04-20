package core.game.loop;

import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityRenderSystem;
import contrib.debug.systems.DebugGameplaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import contrib.editor.level.LevelEditorSystem;
import contrib.hud.dialogs.DialogBackendInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionSelectionOverlayUi;
import contrib.modules.levelhide.LevelHideSystem;
import core.System;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.GameRuntime;
import core.game.PreRunConfiguration;
import core.game.SystemProfile;
import core.game.render.EcsRenderScreen;
import core.game.startup.ClientStartup;
import core.platform.Platform;
import core.platform.client.*;
import core.platform.client.audio.ClientSoundPlayer;
import core.platform.client.input.ClientInputBridge;
import core.platform.client.window.ClientShutdownExceptionFilter;
import core.platform.client.window.ClientWindowEventsBridge;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.ui.ClientStageHandle;
import core.ui.StageHandle;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.configuration.DisplayMode;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A platform-specific implementation of {@link GameLoopHost} for client-side runtime environments.
 *
 * <p>The {@code ClientLoopHost} manages initialization, configuration, and runtime systems
 * required for running a game client. This includes setting up rendering, input,
 * sound playback, and other platform-specific resources.
 *
 * <p>It integrates the game loop into the client runtime environment and provides support for gameplay extensions,
 * debugging features, and user interface systems.
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
  private ISoundPlayer soundPlayer = new NoSoundPlayer();

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
          Game.screens().display("EcsRenderScreen");
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
    InteractionSelection.install(InteractionSelectionOverlayUi.INSTANCE);
    InputManager.reset();
  }

  private void initializeClientRuntime(GameLoop loopCore) {
    ECSManagement.initializeDefaultSystems(SystemProfile.CLIENT);
    ECSManagement.initializeGameplaySystems(SystemProfile.CLIENT);
    installClientRuntimeSystems();
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

  private void installClientRuntimeSystems() {
    installHudSystems();
    installGameplayExtensions();
    installDebugSystems();
  }

  private void installHudSystems() {
    DialogBackendInstaller.install();
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  private void installGameplayExtensions() {
    addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }

  private void installDebugSystems() {
    addIfAbsent(DebugGameplaySystem.class, DebugGameplaySystem::new);
    addIfAbsent(DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    addIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
    addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    addIfAbsent(DebugEntityRenderSystem.class, DebugEntityRenderSystem::new);
  }

  private <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
