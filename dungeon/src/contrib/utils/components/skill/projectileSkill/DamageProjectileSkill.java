package contrib.utils.components.skill.projectileSkill;

import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.Game;
import core.utils.*;
import core.utils.components.path.IPath;
import java.util.function.Supplier;

/**
 * A projectile-based skill that deals damage on collision with entities.
 *
 * <p>This skill creates a projectile that travels from a start point towards a target point within
 * a given range and speed. When the projectile collides with an entity, it applies damage and can
 * optionally apply a bonus effect. The projectile can either pierce through multiple entities or
 * disappear after the first collision.
 */
public abstract class DamageProjectileSkill extends ProjectileSkill {

  /** The base damage amount dealt by the projectile. */
  protected int damageAmount;

  /** The type of damage this projectile inflicts (e.g., physical, magical). */
  protected DamageType damageType;

  /** Whether the projectile pierces through multiple targets or is removed after hitting one. */
  protected boolean piercing;

  /** A supplier that provides the target endpoint of the projectile. */
  private Supplier<Point> endPointSupplier;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param end A supplier providing the endpoint (target location) of the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param piercing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param hitBoxSize The hitbox size of the projectile used for collision detection.
   * @param hitBoxOffset The hitbox offset of the projectile used for collision detection.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  @SafeVarargs
  public DamageProjectileSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean piercing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      Vector2 hitBoxOffset,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
        cooldown,
        texture,
        speed,
        range,
        hitBoxSize,
        hitBoxOffset,
        ignoreFirstWall,
        resourceCost);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.piercing = piercing;
    this.endPointSupplier = end;
  }

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param end A supplier providing the endpoint (target location) of the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param piercing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  @SafeVarargs
  public DamageProjectileSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean piercing,
      int damageAmount,
      DamageType damageType,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, texture, speed, range, ignoreFirstWall, resourceCost);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.piercing = piercing;
    this.endPointSupplier = end;
  }

  /**
   * Defines the behavior when the projectile collides with another entity.
   *
   * <p>If the target entity has a {@link HealthComponent}, it receives damage. In addition, any
   * configured bonus effect ({@link #additionalEffectAfterDamage} is applied. If {@code piercing}
   * is set to {@code false}, the projectile is removed after the collision; otherwise, it continues
   * traveling and ignores this target in future collisions.
   *
   * @param caster the entity that created or cast the projectile
   * @return a {@link TriConsumer} defining the collision behavior; the parameters are:
   *     <ul>
   *       <li>the projectile entity
   *       <li>the entity the projectile collides with
   *       <li>the collision direction, relative to the projectile
   *     </ul>
   */
  @Override
  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return (projectile, target, direction) -> {
      if (ignoreOtherProjectiles && target.isPresent(ProjectileComponent.class)) return;
      if (!ignoreEntities.contains(target)) {
        target
            .fetch(HealthComponent.class)
            .ifPresent(hc -> hc.receiveHit(calculateDamage(caster, target, direction)));
        additionalEffectAfterDamage(caster, projectile, target, direction);

        if (piercing) {
          ignoreEntities.add(target);
        } else {
          Game.remove(projectile);
        }
      }
    };
  }

  /**
   * Provides the endpoint (target position) for the projectile.
   *
   * @param caster The entity casting the projectile.
   * @return The end point of the projectile.
   */
  @Override
  protected Point end(Entity caster) {
    return endPointSupplier.get();
  }

  /**
   * Calculates the damage dealt by this projectile on impact.
   *
   * <p>Can be overridden to take weaknesses, resistances, or buffs into account.
   *
   * @param caster The entity casting the projectile.
   * @param target The entity being hit by the projectile.
   * @param direction The direction from which the projectile hit.
   * @return A {@link Damage} object describing the damage.
   */
  protected Damage calculateDamage(Entity caster, Entity target, Direction direction) {
    // TODO: Integrate weaknesses/resistances once the system is implemented.
    return new Damage(damageAmount, damageType, caster);
  }

  /**
   * Defines additional behavior on Damage-Collision.
   *
   * <p>Overwrite this function to define what should happen, if damage is applied
   *
   * <p>Default Bonus Effect does nothing.
   *
   * @param caster The entity casting the projectile.
   * @param projectile The projectile Entity
   * @param target The Entity that was hit
   * @param direction The colide Direction
   */
  protected void additionalEffectAfterDamage(
      Entity caster, Entity projectile, Entity target, Direction direction) {}

  /**
   * Returns the base damage amount of this projectile skill.
   *
   * @return The damage amount.
   */
  public int damageAmount() {
    return damageAmount;
  }

  /**
   * Sets a new base damage amount for this projectile skill.
   *
   * @param damageAmount The new damage amount.
   */
  public void damageAmount(int damageAmount) {
    this.damageAmount = damageAmount;
  }

  /**
   * Returns the damage type of this projectile skill.
   *
   * @return The {@link DamageType} of this skill.
   */
  public DamageType damageType() {
    return damageType;
  }

  /**
   * Sets a new damage type for this projectile skill.
   *
   * @param damageType The new {@link DamageType}.
   */
  public void damageType(DamageType damageType) {
    this.damageType = damageType;
  }

  /**
   * Increase the amount of damage of this skill.
   *
   * @param amount amount to add on top of the current damage amount.
   */
  public void increaseDamage(int amount) {
    this.damageAmount += amount;
  }

  /**
   * Increase the speed of this skill.
   *
   * @param amount amount to add on top of the current speed.
   */
  public void increaseSpeed(float amount) {
    this.speed += speed;
  }

  /**
   * Increase the range of this skill.
   *
   * @param amount amount to add on top of the current range.
   */
  public void increaseRange(float amount) {
    this.range += range;
  }

  /**
   * Sets a new endpoint to aim for.
   *
   * @param endPointSupplier a new supplier that provides the point to aim for.
   */
  public void targetSelection(Supplier<Point> endPointSupplier) {
    this.endPointSupplier = endPointSupplier;
  }
}
