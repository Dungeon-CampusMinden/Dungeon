package portal.physicsobject;

import core.Entity;
import core.utils.Point;

/**
 * Base abstraction for spawning configurable portal cubes.
 *
 * <p>This abstraction defines how a cube entity is created and configured before being added to the
 * game world.
 *
 * <p>Concrete implementations are responsible for defining cube-specific properties such as mass,
 * appearance, and interaction behavior, while the engine controls when and where the cube is
 * spawned.
 */
public abstract class PortalCube {

  /**
   * Spawns a new cube entity at the given position.
   *
   * <p>The returned {@link Entity} is expected to be fully configured and ready to be added to the
   * game world.
   *
   * @param spawn the world position where the cube should be spawned
   * @return the newly created cube entity
   */
  public abstract Entity spawn(Point spawn);
}
