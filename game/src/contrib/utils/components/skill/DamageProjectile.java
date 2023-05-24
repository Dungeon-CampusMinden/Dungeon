package contrib.utils.components.skill;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.TriConsumer;
import contrib.utils.components.health.Damage;

import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;

import dslToGame.AnimationBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * DamageProjectile is an abstract class that represents a projectile capable of dealing damage to
 * entities. The DamageProjectile class implements the Consumer interface, allowing it to accept an
 * entity as a parameter.
 */
public abstract class DamageProjectile implements Consumer<Entity> {

    private String pathToTexturesOfProjectile;
    private float projectileSpeed;

    private float projectileRange;
    private Damage projectileDamage;
    private Point projectileHitboxSize;

    private Supplier<Point> selectionFunction;

    /**
     * The DamageProjectile constructor sets the path to the textures of the projectile, the speed
     * of the projectile, the damage to be dealt, the size of the projectile's hitbox, the target
     * selection function, and the range of the projectile.
     *
     * <p>for specific implementation, see {@link contrib.utils.components.skill.FireballSkill}
     *
     * @param pathToTexturesOfProjectile path to the textures of the projectile
     * @param projectileSpeed speed of the projectile
     * @param projectileDamage damage of the projectile
     * @param projectileHitboxSize size of the Hitbox
     * @param selectionFunction specific functionality of the projectile
     * @param projectileRange range in which the projectile is effective
     */
    public DamageProjectile(
            String pathToTexturesOfProjectile,
            float projectileSpeed,
            Damage projectileDamage,
            Point projectileHitboxSize,
            Supplier<Point> selectionFunction,
            float projectileRange) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.projectileHitboxSize = projectileHitboxSize;
        this.selectionFunction = selectionFunction;
    }

    /**
     * Performs the necessary actions to create and handle the damage projectile based on the
     * provided entity.
     *
     * @param entity the entity on which the damage projectile will be applied
     * @throws MissingComponentException if the entity does not have a PositionComponent
     */
    @Override
    public void accept(Entity entity) {
        Entity projectile = new Entity("Projectile");
        // Get the PositionComponent of the entity
        PositionComponent epc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        new PositionComponent(projectile, epc.getPosition());

        Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
        new DrawComponent(projectile, animation);

        // Get the target point based on the selection function and projectile range
        Point aimedOn = selectionFunction.get();
        Point targetPoint =
                SkillTools.calculateLastPositionInRange(
                        epc.getPosition(), aimedOn, projectileRange);

        // Calculate the velocity of the projectile
        Point velocity =
                SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);

        // Add the VelocityComponent to the projectile
        VelocityComponent vc =
                new VelocityComponent(projectile, velocity.x, velocity.y, animation, animation);

        // Add the ProjectileComponent with the initial and target positions to the projectile
        new ProjectileComponent(projectile, epc.getPosition(), targetPoint);

        // Create a collision handler for the projectile
        TriConsumer<Entity, Entity, Tile.Direction> collide =
                (a, b, from) -> {
                    if (b != entity) {
                        b.getComponent(HealthComponent.class)
                                .ifPresent(
                                        hc -> {
                                            // Apply the projectile damage to the collided entity
                                            ((HealthComponent) hc).receiveHit(projectileDamage);

                                            // Remove the projectile entity from the game
                                            Game.removeEntity(projectile);
                                        });
                    }
                };

        // Add the CollideComponent with the appropriate hitbox size and collision handler to the
        // projectile
        new CollideComponent(
                projectile, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
    }
}
