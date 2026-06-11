package portal.portals.abstraction;

import core.Entity;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;

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

  /**
   * Calculates the force applied by a tractor beam.
   *
   * <p>This method defines the directional force that is applied to entities affected by a tractor
   * beam.
   *
   * <p>The returned {@link Vector2} represents both the direction and magnitude of the force. The
   * engine applies this force to movable entities while they are inside the beam.
   *
   * <p>Implementations may vary the force strength depending on the given direction to achieve
   * different gameplay effects.
   *
   * @param direction the direction in which the tractor beam is oriented
   * @return a vector describing the applied force
   */
  public abstract Vector2 beamForce(Direction direction);

  /**
   * Calculates the inverse force of a tractor beam.
   *
   * <p>This method defines the force applied when an entity is moved opposite to the tractor beam's
   * primary direction.
   *
   * <p>In most cases, this force is the negation of {@link #beamForce(Direction)}, but
   * implementations are free to apply different magnitudes or behaviors if required.
   *
   * @param direction the original direction of the tractor beam
   * @return a vector describing the inverse applied force
   */
  public abstract Vector2 reversedBeamForce(Direction direction);
}
