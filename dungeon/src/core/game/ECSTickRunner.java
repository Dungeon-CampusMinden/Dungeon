package core.game;

import core.System;
import java.util.function.Predicate;

/**
 * Executes one ECS tick using the same semantics as the in-game loop: - iterate systems in
 * registration order - respect executeEveryXFrames and isRunning - update lastExecuteInFrames
 * counters
 *
 * <p>This allows non-render loops (e.g., dedicated servers) to reuse the original ECS execution
 * behavior without duplicating logic.
 */
public final class ECSTickRunner {
  private ECSTickRunner() {}

  /**
   * Runs one logical ECS frame.
   *
   * @param shouldRun a predicate to include/exclude systems (return true to consider a system).
   */
  public static void runOneFrame(Predicate<System> shouldRun) {
    for (System system : ECSManagment.systems().values()) {
      if (shouldRun != null && !shouldRun.test(system)) continue;
      system.lastExecuteInFrames(system.lastExecuteInFrames() + 1);
      if (system.isRunning() && system.lastExecuteInFrames() >= system.executeEveryXFrames()) {
        system.execute();
        system.lastExecuteInFrames(0);
      }
    }
  }
}
