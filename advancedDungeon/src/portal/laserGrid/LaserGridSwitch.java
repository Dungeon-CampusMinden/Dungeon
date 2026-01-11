package portal.laserGrid;

import core.Entity;

/**
 * Base abstraction for controlling a laser grid.
 *
 * <p>This class defines how a laser grid reacts to external triggers such as switches, pressure
 * plates, or levers.
 *
 * <p>Concrete implementations decide what it means to activate or deactivate the grid, while the
 * engine manages rendering, collision, and damage logic.
 */
public abstract class LaserGridSwitch {

  /**
   * Activates the given laser grid.
   *
   * @param grid the laser grid entities to activate
   */
  public abstract void activate(Entity[] grid);

  /**
   * Deactivates the given laser grid.
   *
   * @param grid the laser grid entities to deactivate
   */
  public abstract void deactivate(Entity[] grid);
}
