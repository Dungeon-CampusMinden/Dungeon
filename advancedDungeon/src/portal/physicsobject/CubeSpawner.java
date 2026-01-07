package portal.physicsobject;

/**
 * Base abstraction for user-defined cube spawning logic.
 *
 * <p>Implementations of this class are expected to define custom spawn behavior that is executed
 * when a corresponding lever is activated.
 *
 * <p>Instances are loaded dynamically at runtime and may be recompiled and reloaded during
 * gameplay.
 *
 * <p>The engine assumes that implementations are stateless or manage their own internal state
 * safely.
 */
public abstract class CubeSpawner {

  /**
   * Executes the spawn logic.
   *
   * <p>This method is invoked by the engine when the associated spawn lever is triggered.
   *
   * <p>Implementations are responsible for creating and registering any entities they spawn.
   */
  public abstract void spawn();
}
