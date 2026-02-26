package core.game.litiengine;

import core.game.ECSManagement;
import core.game.GameLoopCore;
import core.game.SystemProfile;
import core.platform.Platform;
import core.platform.litiengine.LitiengineInputBridge;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.IUpdateable;

public final class LitiengineGameLoopHost {
  private LitiengineGameLoopHost() {}

  public static void run(String[] args, GameLoopCore core) {
    // Register lifecycle listener BEFORE init so we can safely set up screens at the right time.
    Game.addGameListener(
      new GameListener() {
        @Override
        public void initialized(String... initArgs) {
          // After Game.init: infrastructure should exist -> add and display our debug screen.
          // This avoids timing issues where RenderComponent renders before a screen is ready.
          try {
            if (Game.screens() != null) {
              Game.screens().add(new DungeonDebugScreen());
              Game.screens().display(DungeonDebugScreen.NAME);
            }
          } catch (Exception e) {
            // Don't crash the engine during initialization; log and continue.
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

    // Initialize LITIENGINE (sets up core singletons like window/loop/graphics/screens).
    Game.init(args); // docs: init + start are required to launch the engine

    // Bind platform adapters AFTER init so Game.window() etc. are available.
    Platform.window(new core.platform.litiengine.LitiengineWindowAdapter());
    Platform.runtime(new core.platform.litiengine.LitiengineRuntimeAdapter());
    Platform.resources(new core.platform.classpath.ClasspathResourcesAdapter());
    Platform.render(new core.platform.NullRenderAdapter());
    Platform.pathfinding(new core.platform.grid.GridPathfindingAdapter());

    // Bridge LITIENGINE input events into our engine-agnostic InputManager.
    LitiengineInputBridge.install();

    // Ensure we start with a clean input state.
    InputManager.reset();

    // Host chooses which default systems exist (simulation only).
    ECSManagement.bootstrapDefaultSystems(SystemProfile.LITIENGINE_SIMULATION);

    // Drive ECS tick from LITIENGINE update loop.
    Game.loop()
      .attach(
        new IUpdateable() {
          @Override
          public void update() {
            final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

            core.beforeRender(deltaSeconds);

            // LITIENGINE_SIMULATION => no rendering systems (libGDX pipeline)
            core.tick(deltaSeconds, false);

            // Must be called once per frame to clear justPressed/justReleased.
            InputManager.update();
          }
        });

    Game.start();
  }
}
