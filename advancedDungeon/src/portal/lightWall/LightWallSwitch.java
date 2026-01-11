package portal.lightWall;

import core.Entity;

/**
 * Base abstraction for controlling a light wall.
 *
 * <p>This class defines how a light wall (or its emitter) reacts to external triggers.
 *
 * <p>Concrete implementations decide when a light wall is enabled or disabled. The engine is
 * responsible for creating and removing the wall segments.
 */
public abstract class LightWallSwitch {

  /**
   * Activates the light wall.
   *
   * @param wall the light wall or emitter entity to activate
   */
  public abstract void activate(Entity wall);

  /**
   * Deactivates the light wall.
   *
   * @param wall the light wall or emitter entity to deactivate
   */
  public abstract void deactivate(Entity wall);
}
