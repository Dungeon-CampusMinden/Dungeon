package feature.components;

import engine.Component;
import engine.Entity;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.skills.projectile.FireballSkill;
import java.util.function.Consumer;

/**
 * Marks an entity as a projectile.
 *
 * <p>The component stores a {@link #startPosition start} and a {@link #goalLocation goal} position.
 *
 * <p>A projectile will need a {@link engine.components.VelocityComponent VelocityComponent} and
 * {@link engine.components.PositionComponent PositionComponent} as well. See <a
 * href="https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/doc/ecs/systems">System-Overview</a>.
 *
 * <p>The {@link feature.systems.ProjectileSystem ProjectileSystem} will calculate a flight path
 * from the start to the goal and will then set the velocity in the {@link
 * engine.components.VelocityComponent} of the entity accordingly. If the goal location is reached,
 * the entity will be removed from the game.
 *
 * <p>Examples of projectiles are {@link FireballSkill fireballs} or arrows.
 *
 * <p>The {@link #goalLocation()} method retrieves the goal position of the projectile. The {@link
 * #startPosition()} method retrieves the start position of the projectile.
 *
 * @param startPosition The point from which to start the calculation to the goal location.
 * @param goalLocation The point where the projectile should fly to.
 * @param forceToApply The force the projectile should move with.
 * @param onEndReached Callback to execute when the projectile reached the goal location.
 * @see feature.skills.projectile.DamageProjectileSkill
 */
public record ProjectileComponent(
    Point startPosition, Point goalLocation, Vector2 forceToApply, Consumer<Entity> onEndReached)
    implements Component {}
