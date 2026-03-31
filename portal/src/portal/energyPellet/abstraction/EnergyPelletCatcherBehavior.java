package portal.energyPellet.abstraction;

import core.Entity;

/**
 * Base abstraction for defining behavior when an energy pellet is caught.
 *
 * <p>This class defines the contract for handling interactions between an energy pellet and a
 * pellet catcher entity.
 *
 * <p>Concrete implementations are expected to define what happens when a pellet collides with a
 * catcher, such as toggling a state, triggering events, or removing the pellet.
 *
 * <p>Implementations may be dynamically loaded and hot-reloaded during runtime.
 */
public abstract class EnergyPelletCatcherBehavior {

  /**
   * Handles the collision between an energy pellet and a catcher.
   *
   * <p>This method is invoked by the engine when an energy pellet collides with a catcher entity.
   *
   * <p>Implementations are responsible for defining all resulting behavior, including state changes
   * on the catcher and lifecycle handling of the pellet entity.
   *
   * @param catcher the catcher entity that was hit
   * @param pellet the energy pellet entity that collided with the catcher
   */
  public abstract void catchPellet(Entity catcher, Entity pellet);
}
