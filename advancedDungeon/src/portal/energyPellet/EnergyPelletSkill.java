package portal.energyPellet;

import contrib.components.HealthComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;
import portal.components.AntiMaterialBarrierComponent;

/**
 * An energy pellet projectile skill that deals damage on impact.
 *
 * <p>This class extends {@link DamageProjectileSkill} to implement an energy pellet-specific skill.
 * The projectile will travel toward a target point, deal damage on collision with an entity, and be
 * removed if it hits a wall or reaches its maximum range.
 *
 * <p>This projectile is intended to be used in combination with an energyPellet- Catcher and
 * Launcher.
 */
public class EnergyPelletSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "ENERGY_PELLET";

  private static final IPath TEXTURE = new SimpleIPath("skills/energy_pellet/energy_pellet.png");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 7f;

  /** Cooldown of the Skill. */
  public static final long COOLDOWN = 500;

  private static final boolean IS_PIERCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;

  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;

  private long PROJECTILE_LIFETIME = 2000;
  private EventScheduler.ScheduledAction scheduledRemoveAction;

  /**
   * Creates a fully customized energy pellet skill with a custom name.
   *
   * <p>This constructor allows for subclassing and customization of the energy pellet skill,
   * including its name, target selection, cooldown, speed, range, damage amount, and resource
   * costs.
   *
   * @param name Name of the skill.
   * @param target Function providing the target point.
   * @param cooldown Cooldown in ms.
   * @param speed Travel speed of the projectile.
   * @param range Maximum travel range.
   * @param damageAmount Base damage dealt.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  protected EnergyPelletSkill(
      String name,
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
        cooldown,
        TEXTURE,
        target,
        speed,
        range,
        IS_PIERCING,
        damageAmount,
        DAMAGE_TYPE,
        ignoreFirstWall,
        resourceCost);
  }

  /**
   * Creates a fully customized energy pellet skill.
   *
   * @param target Function providing the target point.
   * @param cooldown Cooldown in ms.
   * @param speed Travel speed of the projectile.
   * @param range Maximum travel range.
   * @param damageAmount Base damage dealt.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    this(SKILL_NAME, target, cooldown, speed, range, damageAmount, ignoreFirstWall, resourceCost);
  }

  /**
   * Creates an energy pellet skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown time (in ms) before the skill can be used again.
   * @param range Maximum travel range.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      Supplier<Point> targetSelection,
      long cooldown,
      float range,
      Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, cooldown, SPEED, range, DAMAGE, IGNORE_FIRST_WALL, resourceCost);
  }

  /**
   * Creates an energy pellet skill with default values and custom cooldown.
   *
   * @param name Name of the skill.
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown time (in ms) before the skill can be used again.
   * @param range Maximum travel range.
   * @param projectileLifetime Time in ms before the projectile is removed.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      String name,
      Supplier<Point> targetSelection,
      long cooldown,
      float range,
      long projectileLifetime,
      Tuple<Resource, Integer>... resourceCost) {
    this(name, targetSelection, cooldown, SPEED, range, DAMAGE, IGNORE_FIRST_WALL, resourceCost);
    this.PROJECTILE_LIFETIME = projectileLifetime;
  }

  /**
   * Creates an energy pellet skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      Supplier<Point> targetSelection, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, COOLDOWN, RANGE, resourceCost);
  }

  @Override
  protected void onWallHit(Entity caster, Entity projectile) {
    VelocityComponent vc = projectile.fetch(VelocityComponent.class).orElse(null);
    if (vc == null) return;

    PositionComponent pc = projectile.fetch(PositionComponent.class).orElse(null);
    if (pc == null) return;

    Vector2 velocity = Vector2.ZERO;

    if (pc.viewDirection().equals(Direction.DOWN) || pc.viewDirection().equals(Direction.UP)) {
      velocity = Vector2.of(vc.currentVelocity().x(), -vc.currentVelocity().y());
    } else if (pc.viewDirection().equals(Direction.RIGHT)
        || pc.viewDirection().equals(Direction.LEFT)) {
      velocity = Vector2.of(-vc.currentVelocity().x(), vc.currentVelocity().y());
    }

    vc.currentVelocity(velocity);
  }

  /**
   * Defines the behavior when the Energy Pellet projectile collides with another entity.
   *
   * <p>If the target entity has a {@link HealthComponent}, it receives damage. In addition, any
   * configured bonus effect ({@link #additionalEffectAfterDamage} is applied. If {@code piercing}
   * is set to {@code false}, the projectile is removed after the collision as long as it does not
   * collide with an AntiMaterialBarrier entity; otherwise, it continues traveling and ignores this
   * target in future collisions.
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
      target
          .fetch(HealthComponent.class)
          .ifPresent(hc -> hc.receiveHit(calculateDamage(caster, target, direction)));
      additionalEffectAfterDamage(caster, projectile, target, direction);

      if (piercing || target.isPresent(AntiMaterialBarrierComponent.class)) {
        ignoreEntity(target);
      } else {
        Game.remove(projectile);
      }
    };
  }

  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    if (scheduledRemoveAction != null && EventScheduler.isScheduled(scheduledRemoveAction)) {
      EventScheduler.cancelAction(scheduledRemoveAction);
    }

    scheduledRemoveAction =
        EventScheduler.scheduleAction(
            () -> {
              Game.remove(projectile);
            },
            PROJECTILE_LIFETIME);
  }
}
