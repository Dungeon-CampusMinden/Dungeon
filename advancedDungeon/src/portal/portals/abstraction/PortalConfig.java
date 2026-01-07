package portal.portals.abstraction;

import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;
import portal.abstraction.Hero;

/**
 * Base configuration for a portal projectile ("portal shot").
 *
 * <p>This class defines all configurable parameters that control how a portal shot behaves, such as
 * cooldown, movement speed, range, collision size and target selection.
 *
 * <p>Concrete implementations are expected to provide gameplay-specific values while the engine
 * handles execution and physics.
 */
public abstract class PortalConfig {

  /** Size of the portal projectile hit box. */
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.5, 0.5);

  /** Offset applied to the hit box relative to the projectile position. */
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.25, 0.25);

  /**
   * Creates a new portal configuration for the given hero.
   *
   * @param hero the {@link Hero} this portal configuration belongs to
   */
  public PortalConfig(Hero hero) {}

  /**
   * Returns the cooldown of the portal shot.
   *
   * <p>The cooldown specifies how long (in milliseconds) must pass before the portal shot can be
   * fired again.
   *
   * @return the cooldown duration in milliseconds
   */
  public abstract long cooldown();

  /**
   * Returns the movement speed of the portal shot.
   *
   * <p>This value controls how fast the portal projectile travels towards its target.
   *
   * @return the speed of the portal shot
   */
  public abstract float speed();

  /**
   * Returns the maximum range of the portal shot.
   *
   * <p>The range limits how far away the target point may be. Shots exceeding this distance may be
   * clamped or rejected by the engine.
   *
   * @return the maximum allowed range
   */
  public abstract float range();

  /**
   * Returns the size of the portal shot hit box.
   *
   * <p>This value is fixed and cannot be overridden by subclasses.
   *
   * @return the hit box size
   */
  public final Vector2 hitBoxSize() {
    return HIT_BOX_SIZE;
  }

  /**
   * Returns the hit box offset of the portal shot.
   *
   * <p>This value is fixed and cannot be overridden by subclasses.
   *
   * @return the hit box offset
   */
  public final Vector2 hitBoxOffset() {
    return HIT_BOX_OFFSET;
  }

  /**
   * Provides the target point supplier for the portal shot.
   *
   * <p>The returned {@link Supplier} is evaluated when the portal shot is fired and determines the
   * point the projectile will travel to.
   *
   * <p>The resulting {@link Point} is used by the engine to compute the projectile's trajectory.
   *
   * @return a supplier providing the target point of the portal shot
   */
  public abstract Supplier<Point> target();
}
