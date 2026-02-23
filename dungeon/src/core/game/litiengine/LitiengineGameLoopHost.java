package core.game.litiengine;

import core.game.ECSManagement;
import core.game.GameLoopCore;
import core.platform.Platform;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;

/**
 * LITIENGINE host that drives the engine-agnostic GameLoopCore.
 *
 * <p>Note: This does not render the Dungeon yet (current rendering pipeline is libGDX-based).
 * This host is a proof that LITIENGINE can drive the ECS tick.
 */
public final class LitiengineGameLoopHost {
  private LitiengineGameLoopHost() {}

  public static void run(String[] args, GameLoopCore core) {
    Game.init(args); // spawns an empty window after Game.start()

    Platform.window(new core.platform.litiengine.LitiengineWindowAdapter());
    Platform.runtime(new core.platform.litiengine.LitiengineRuntimeAdapter());
    Platform.resources(new core.platform.classpath.ClasspathResourcesAdapter());

    ECSManagement.bootstrapDefaultSystems(false);

    Game.loop().attach(new IUpdateable() { // attach to update loop
      private float logAccumulator = 0f;

      @Override
      public void update() {
        // LITIENGINE delta time is ms since last tick
        final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

        core.beforeRender(deltaSeconds);
        core.tick(deltaSeconds, false);

        // visible proof in console (once per ~1s)
        logAccumulator += deltaSeconds;
        if (logAccumulator >= 1.0f) {
          logAccumulator = 0f;
          java.lang.System.out.println("[LITIENGINE] GameLoopCore ticked. dt=" + deltaSeconds + "s");
        }
      }
    });

    Game.start();
  }
}
