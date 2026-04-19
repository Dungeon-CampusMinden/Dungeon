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
import contrib.modules.levelHide.LevelHideSystem;
import core.System;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.GameRuntime;
import core.game.PreRunConfiguration;
import core.game.SystemProfile;
import core.game.render.EcsRenderScreen;
import core.game.startup.ClientStartup;
import core.platform.Platform;
import core.platform.client.ClientCameraAdapter;
import core.platform.client.ClientCursorAdapter;
import core.platform.client.ClientRenderAdapter;
import core.platform.client.ClientRuntimeAdapter;
import core.platform.client.ClientWindowAdapter;
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

public final class ClientLoopHost implements GameLoopHost {
  private ISoundPlayer soundPlayer = new NoSoundPlayer();

  @Override
  public ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  @Override
  public void run(String[] args, GameLoop loopCore) {
    syncDisplaySettings();

    Game.addGameListener(new GameListener() {
      public void initialized() {
        Game.screens().add(new EcsRenderScreen());
        Game.screens().display("EcsRenderScreen");
      }
    });

    Game.init(args);

    Thread.setDefaultUncaughtExceptionHandler(new ClientShutdownExceptionFilter());

    Platform.camera(new ClientCameraAdapter());
    Platform.cursor(new ClientCursorAdapter());
    Platform.render(new ClientRenderAdapter());
    Platform.runtime(new ClientRuntimeAdapter());
    Platform.window(new ClientWindowAdapter());
    ClientWindowEventsBridge.install();

    soundPlayer =
      PreRunConfiguration.disableAudio() ? new NoSoundPlayer() : new ClientSoundPlayer();

    ClientInputBridge.install();
    InteractionSelection.install(InteractionSelectionOverlayUi.INSTANCE);
    InputManager.reset();

    ECSManagement.initializeDefaultSystems(SystemProfile.CLIENT);
    ECSManagement.initializeGameplaySystems(SystemProfile.CLIENT);
    installClientRuntimeSystems();
    ECSManagement.system(
      core.systems.LevelSystem.class,
      ls -> ls.onLevelLoad(GameRuntime.onLevelLoad));

    ClientStartup.setupAndLoadInitialLevelOnce();

    Game.loop().attach(() -> {
      final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

      core.Game.soundPlayer().update(deltaSeconds);
      loopCore.beforeRender();
      loopCore.tick(deltaSeconds, false);
      InputManager.update();
    });

    Game.start();
  }

  @Override
  public Optional<StageHandle> stage() {
    return Optional.of(new ClientStageHandle());
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
