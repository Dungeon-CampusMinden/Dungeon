package core.game.litiengine;

import core.game.*;
import core.platform.Platform;
import core.platform.litiengine.LitiengineInputBridge;
import core.platform.litiengine.LitiengineLoopHost;
import core.platform.litiengine.LitiengineRenderAdapter;
import core.platform.litiengine.sound.LitiengineSoundPlayer;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.IUpdateable;

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
              Game.screens().add(new DungeonDebugScreen());
              Game.screens().display(DungeonDebugScreen.NAME);
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
              Game.screens().display(DungeonDebugScreen.NAME);
            }
          } catch (Exception e) {
            Game.log().severe("Failed to display debug screen in started(): " + e.getMessage());
          }
        }
      });

    // Initialize LITIENGINE
    Game.init(args);

    // init sound backend after engine init
    soundPlayer = PreRunConfiguration.disableAudio()
      ? new NoSoundPlayer()
      : new LitiengineSoundPlayer();

    Platform.loopHost(new LitiengineLoopHost());

    // Bind platform adapters AFTER init so Game.window() etc. are available.
    Platform.window(new core.platform.litiengine.LitiengineWindowAdapter());
    Platform.runtime(new core.platform.litiengine.LitiengineRuntimeAdapter());
    Platform.resources(new core.platform.classpath.ClasspathResourcesAdapter());
    Platform.render(new core.platform.NullRenderAdapter());
    Platform.render(new LitiengineRenderAdapter());
    Platform.pathfinding(new core.platform.grid.GridPathfindingAdapter());

    // Bridge LITIENGINE input events into our engine-agnostic InputManager.
    LitiengineInputBridge.install();

    // Ensure we start with a clean input state.
    InputManager.reset();

    // Host chooses which default systems exist (simulation only).
    ECSManagement.bootstrapDefaultSystems(SystemProfile.LITIENGINE_SIMULATION);
    ECSManagement.bootstrapGameplaySystems(SystemProfile.LITIENGINE_SIMULATION);
    ECSManagement.bootstrapDefaultSystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.bootstrapGameplaySystems(SystemProfile.LITIENGINE_CLIENT);
    ECSManagement.system(core.systems.LevelSystem.class, ls -> ls.onLevelLoad(GameLoop.onLevelLoad));

    // Drive ECS tick from LITIENGINE update loop.
    Game.loop()
      .attach(
        new IUpdateable() {
          @Override
          public void update() {
            final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

            core.Game.soundPlayer().update(deltaSeconds);
            loopCore.beforeRender(deltaSeconds);
            loopCore.tick(deltaSeconds, false);

            // Must be called once per frame to clear justPressed/justReleased.
            InputManager.update();
          }
        });

    Game.start();
  }
}
