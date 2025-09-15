package contrib.utils.components.ai.fight;

import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Point;
import java.util.function.Consumer;

/**
 * A simple attack behaviour for a sentry entity.
 *
 * <p>The entity stands still on a fixed Point and shoots in a fixed direction.
 */
public final class StationarySentryAttack implements Consumer<Entity>, ISkillUser {
  private final Point spawnPoint;
  private final boolean canEnterWalls;

  private final float attackRange;
  private DamageProjectileSkill fightSkill;
  private final Direction shootDirection;
  private long lastAttackTime = 0;

  /**
   * Creates a new {@code StationarySentryAttack}.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param attackRange Maximum shooting (projectile travel) range.
   * @param fightSkill the {@link DamageProjectileSkill} used to attack.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param canEnterWalls whether the sentry can move inside walls.
   */
  public StationarySentryAttack(
      Point spawnPoint,
      float attackRange,
      Skill fightSkill,
      Direction shootDirection,
      boolean canEnterWalls) {
    this.spawnPoint = spawnPoint;
    this.attackRange = attackRange;
    this.shootDirection = shootDirection;
    this.canEnterWalls = canEnterWalls;
    if (fightSkill instanceof DamageProjectileSkill dps) {
      this.fightSkill = dps;
    } else {
      throw new IllegalArgumentException(
          "Skill for SentryFightBehaviour must be a DamageProjectileSkill!");
    }

    Point targetEndPoint = this.spawnPoint.translate(this.shootDirection.scale(this.attackRange));
    this.fightSkill.targetSelection(() -> targetEndPoint.toCenteredPoint());
  }

  @Override
  public void accept(Entity entity) {
    tryAttack(entity);
  }

  private void tryAttack(Entity entity) {
    if (fightSkill == null) return;

    if (LevelUtils.playerInRange(entity, attackRange)) {
      long now = System.currentTimeMillis();
      if (now - lastAttackTime >= fightSkill.cooldown()) {
        useSkill(fightSkill, entity);
        lastAttackTime = now;
      }
    }
  }

  @Override
  public void useSkill(Skill fightSkill, Entity skillUser) {
    if (fightSkill != null) {
      fightSkill.execute(skillUser);
    }
  }

  @Override
  public Skill skill() {
    return this.fightSkill;
  }

  @Override
  public void skill(Skill skill) {
    if (skill instanceof DamageProjectileSkill dps) {
      this.fightSkill = dps;
    } else {
      throw new IllegalArgumentException("Skill must be a DamageProjectileSkill!");
    }
  }
}
