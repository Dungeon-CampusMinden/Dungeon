package contrib.components;

import core.Component;
import core.Entity;
import core.utils.position.Position;

/**
 * Marks an entity as a projectile.
 *
 * <p>The component stores a {@link #startPosition start} and a {@link #goalLocation goal} position.
 *
 * <p>A projectile will need a {@link core.components.VelocityComponent VelocityComponent} and
 * {@link core.components.PositionComponent PositionComponent} as well. See <a
 * href="https://github.com/Programmiermethoden/Dungeon/tree/master/doc/ecs/systems">System-Overview</a>.
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
 * @see contrib.utils.components.skill.DamageProjectile
 */
public final class ProjectileComponent extends Component {

    private final Position goalLocation;
    private final Position startPosition;

    /**
     * Create a new ProjectileComponent and add it to the associated entity.
     *
     * @param entity The associated entity.
     * @param startPosition The point from which to start the calculation to the goal location.
     * @param goalLocation The point where the projectile should fly to.
     */
    public ProjectileComponent(
            final Entity entity, final Position startPosition, final Position goalLocation) {
        super(entity);
        this.goalLocation = goalLocation;
        this.startPosition = startPosition;
    }

    /**
     * Get the target position of the projectile.
     *
     * @return The point where the projectile should fly to.
     */
    public Position goalLocation() {
        return goalLocation;
    }

    /**
     * Get the start position of the projectile.
     *
     * @return The point from which to start the calculation to the goal location
     */
    public Position startPosition() {
        return startPosition;
    }
}
