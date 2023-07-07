package contrib.components;

import core.Component;
import core.Entity;
import core.utils.Point;

/**
 * Marks an entity as a projectile.
 *
 * <p>The Component stores a {@link #startPosition start} and a {@link #goalLocation goal}
 *
 * <p>The {@link contrib.systems.ProjectileSystem ProjectileSystem} will calculate a flight path
 * from the start to the goal and will then set the velocity of the entity accordingly. If the goal
 * location is reached, the entity will be removed from the game. A {@link
 * core.components.VelocityComponent VelocityComponent} and {@link core.components.PositionComponent
 * PositionComponent} is needed as well.
 *
 * <p>Examples of projectiles are Fireballs or Arrows.
 *
 * <p>The {@link #goalLocation()} method retrieves the goal position of the projectile. The {@link
 * #startPosition()} method retrieves the start position of the projectile.
 *
 * @see contrib.utils.components.skill.DamageProjectile
 */
public final class ProjectileComponent extends Component {

    private final Point goalLocation;
    private final Point startPosition;

    /**
     * Create a new ProjectileComponent and add it to the associated entity.
     *
     * @param entity The associated entity.
     * @param startPosition The point from which to start the calculation to the goal location.
     * @param goalLocation The point where the projectile should fly to.
     */
    public ProjectileComponent(
            final Entity entity, final Point startPosition, final Point goalLocation) {
        super(entity);
        this.goalLocation = goalLocation;
        this.startPosition = startPosition;
    }

    /**
     * Get goal location of the projectile.
     *
     * @return The point where the projectile should fly to.
     */
    public Point goalLocation() {
        return goalLocation;
    }

    /**
     * Get start position of the projectile.
     *
     * @return The point from which to start the calculation to the goal location
     */
    public Point startPosition() {
        return startPosition;
    }
}
