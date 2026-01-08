package portal.portals.abstraction;

import core.Entity;
import core.level.utils.LevelElement;
import core.utils.Direction;
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

  /**
   * Calculates the end point of a light wall beam.
   *
   * <p>This method determines how far a light wall may extend starting from a given point and
   * moving in a fixed direction.
   *
   * <p>The calculation proceeds step by step along the given direction until a tile is encountered
   * whose {@link LevelElement} is contained in the provided {@code stoppingTiles} set.
   *
   * <p>The returned {@link Point} represents the last valid position the light wall may occupy
   * before being blocked.
   *
   * @param from the starting point of the light wall
   * @param beamDirection the direction in which the light wall propagates
   * @param stoppingTiles level elements that block the light wall
   * @return the final reachable point of the light wall
   */
  public abstract Point calculateLightWallAndBridgeEnd(
      Point from, Direction beamDirection, LevelElement[] stoppingTiles);
}
