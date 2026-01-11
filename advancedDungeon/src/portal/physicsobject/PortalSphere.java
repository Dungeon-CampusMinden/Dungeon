package portal.physicsobject;

import core.Entity;
import core.utils.Point;

/**
 * Base abstraction for spawning configurable portal spheres.
 *
 * <p>This abstraction defines how a sphere entity is created and configured before being added to
 * the game world.
 *
 * <p>Concrete implementations are responsible for defining sphere-specific properties such as mass,
 * appearance, and interaction behavior, while the engine controls when and where the sphere is
 * spawned.
 */
public abstract class PortalSphere {

  /**
   * Spawns a new sphere entity at the given position.
   *
   * <p>The returned {@link Entity} is expected to be fully configured and ready to be added to the
   * game world.
   *
   * @param spawn the world position where the sphere should be spawned
   * @return the newly created sphere entity
   */
  public abstract Entity spawn(Point spawn);
}
