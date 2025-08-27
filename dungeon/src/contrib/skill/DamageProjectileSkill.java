package contrib.skill;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DamageProjectileSkill extends ProjectileSkill {

  public static final BiConsumer<Entity, Entity> DEFAULT_BONUS_EFFECT = (entity, entity2) -> {};

  protected int damageAmount;
  protected DamageType damageType;
  protected Entity owner;
  protected BiConsumer<Entity, Entity> bonusEffect;
  protected TriConsumer<Entity, Entity, Direction> onCollide =
      new TriConsumer<>() {
        @Override
        public void accept(Entity projectile, Entity target, Direction direction) {

          if (owner == target) return;
          target
              .fetch(HealthComponent.class)
              .ifPresent(hc -> hc.receiveHit(calculateDamage(owner, target, direction)));
          bonusEffect.accept(owner, target);
        }
      };

  public DamageProjectileSkill(
      String name,
      long cooldown,
      Supplier<Point> start,
      Supplier<Point> target,
      IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      float projectileRange,
      Vector2 projectileHitBoxSize,
      Consumer<Entity> onWallHit,
      Consumer<Entity> onSpawn,
      Consumer<Entity> onTargetReached,
      TriConsumer<Entity, Entity, Direction> onCollideLeave,
      Entity owner,
      int damageAmount,
      DamageType damageType,
      BiConsumer<Entity, Entity> bonusEffect) {
    super(
        name,
        cooldown,
        start,
        target,
        pathToTexturesOfProjectile,
        projectileSpeed,
        projectileRange,
        projectileHitBoxSize,
        onWallHit,
        onSpawn,
        onTargetReached,
        null,
        onCollideLeave);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.owner = owner;
    this.bonusEffect = bonusEffect;
    this.onCollide(onCollide);
  }

  protected Damage calculateDamage(Entity caster, Entity target, Direction direction) {
    // toodo add weaknesses to the game
    return new Damage(damageAmount, damageType, owner);
  }
}
