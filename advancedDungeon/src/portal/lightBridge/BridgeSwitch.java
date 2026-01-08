package portal.lightBridge;

import core.Entity;

/**
 * Base abstraction for controlling light bridge activation.
 *
 * <p>This abstraction defines how a light bridge (or its emitter)
 * reacts to external triggers such as switches, pressure plates,
 * or other game logic.
 *
 * <p>Concrete implementations decide what it means to activate
 * or deactivate a bridge, while the engine handles the actual
 * bridge construction and teardown.
 */
public abstract class BridgeSwitch {

  /**
   * Activates the light bridge associated with the given entity.
   *
   * <p>This method is invoked when a connected trigger (e.g. a lever
   * or pressure plate) switches to the active state.
   *
   * @param bridge the bridge or bridge emitter entity to activate
   */
  public abstract void activate(Entity bridge);

  /**
   * Deactivates the light bridge associated with the given entity.
   *
   * <p>This method is invoked when a connected trigger switches
   * to the inactive state.
   *
   * @param bridge the bridge or bridge emitter entity to deactivate
   */
  public abstract void deactivate(Entity bridge);
}
