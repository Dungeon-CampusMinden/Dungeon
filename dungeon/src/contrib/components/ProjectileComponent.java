package contrib.components;

import core.Component;
import core.utils.Point;

/**
 * Marks an entity as a projectile.
 *
 * <p>The component stores a {@link #startPosition start} and a {@link #goalLocation goal} position.
 *
 * <p>A projectile will need a {@link core.components.VelocityComponent VelocityComponent} and
 * {@link core.components.PositionComponent PositionComponent} as well. See <a
 * href="https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/doc/ecs/systems">System-Overview</a>.
 *
 * <p>The {@link contrib.systems.ProjectileSystem ProjectileSystem} will calculate a flight path
 * from the start to the goal and will then set the velocity in the {@link
 * core.components.VelocityComponent} of the entity accordingly. If the goal location is reached,
 * the entity will be removed from the game.
 *
 * <p>Examples of projectiles are {@link contrib.utils.components.skill.FireballSkill fireballs} or
 * arrows.
 *
 * <p>The {@link #goalLocation()} method retrieves the goal position of the projectile. The {@link
 * #startPosition()} method retrieves the start position of the projectile.
 *
 * @param startPosition The point from which to start the calculation to the goal location.
 * @param goalLocation The point where the projectile should fly to.
 * @see contrib.utils.components.skill.DamageProjectile
 */
public record ProjectileComponent(Point startPosition, Point goalLocation) implements Component {

  /**
   * Get the target position of the projectile.
   *
   * @return The point where the projectile should fly to.
   */
  @Override
  public Point goalLocation() {
    return goalLocation;
  }

  /**
   * Get the start position of the projectile.
   *
   * @return The point from which to start the calculation to the goal location
   */
  @Override
  public Point startPosition() {
    return startPosition;
  }
}
