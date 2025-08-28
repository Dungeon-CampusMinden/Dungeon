package contrib.utils.components.skill.projectileSkill;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.Game;
import core.utils.*;
import core.utils.components.path.IPath;
import java.util.function.Supplier;

public abstract class DamageProjectileSkill extends ProjectileSkill {

  protected static final TriConsumer<Entity, Entity, Direction> NOOP_EFFECT =
      (entity, entity2, direction) -> {};
  protected int damageAmount;
  protected DamageType damageType;
  protected boolean pircing;

  protected Supplier<Point> end;
  private TriConsumer<Entity, Entity, Direction> bonusEffect;

  public DamageProjectileSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      TriConsumer<Entity, Entity, Direction> bonusEffect,
      Vector2 hitBoxSize,
      Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, texture, speed, range, hitBoxSize, resourceCost);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.pircing = pircing;
    this.bonusEffect = bonusEffect;
    this.end = end;
  }

  @Override
  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return (projectile, target, direction) -> {
      if (!ignoreEntities.contains(target)) {
        target
            .fetch(HealthComponent.class)
            .ifPresent(hc -> hc.receiveHit(calculateDamage(caster, target, direction)));
        bonusEffect.accept(caster, target, direction);
        if (pircing) ignoreEntities.add(target);
        else Game.remove(projectile);
      }
    };
  }

  @Override
  protected Point end(Entity caster) {
    return end.get();
  }

  protected Damage calculateDamage(Entity caster, Entity target, Direction direction) {
    // toodo add weaknesses to the game
    return new Damage(damageAmount, damageType, caster);
  }
}
