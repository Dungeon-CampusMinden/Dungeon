package portal.portals.abstraction;

import core.Entity;
import core.utils.Point;

/**
 * Base class for portal-related calculations.
 *
 * <p>This abstraction defines calculation logic that is used by the portal system to determine
 * derived values during gameplay.
 *
 * <p>Concrete implementations are responsible for providing game-specific behavior, while the
 * engine controls when and how these calculations are executed.
 */
public abstract class Calculations {

  /**
   * Calculates the exit position for a portal traversal.
   *
   * <p>This method is invoked by the engine when an entity exits a portal. The provided {@link
   * Entity} represents the portal that was used.
   *
   * <p>The returned {@link Point} defines the world position at which the traveling entity will
   * reappear.
   *
   * @param portal the portal entity that was used for traversal
   * @return the calculated exit position
   */
  public abstract Point calculatePortalExit(Entity portal);
}
