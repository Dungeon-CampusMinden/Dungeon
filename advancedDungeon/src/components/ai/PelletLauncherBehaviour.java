package components.ai;

import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Point;
import java.util.function.Consumer;

/**
 * An idle behaviour for the energyPelletLauncher entity created by the AdvancedFactory.
 *
 * <p>uses a damageProjectileSkill to shoot a projectile.
 *
 * <p>the EnergyPelletSkill is the intended damageProjectileSkill to be used for this idle
 * behaviour.
 */
public class PelletLauncherBehaviour implements Consumer<Entity>, ISkillUser {
  private final Point spawnPoint;
  private final float attackRange;
  private final Direction shootDirection;
  private DamageProjectileSkill projectileSkill;
  private long lastAttackTime = 0;
  private String uniqueSkillName;

  /**
   * Creates a new {@code PelletLauncherBehaviour}.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param attackRange maximum shooting (projectile travel) range.
   * @param shootDirection the fixed direction in which the energyPelletLauncher will launch the
   *     energyPellet.
   * @param skill the {@link DamageProjectileSkill} used to shoot the projectile (energyPellet).
   */
  public PelletLauncherBehaviour(
      String uniqueSkillName,
      Point spawnPoint,
      float attackRange,
      Direction shootDirection,
      Skill skill) {
    this.uniqueSkillName = uniqueSkillName;
    this.spawnPoint = spawnPoint;
    this.attackRange = attackRange;
    this.shootDirection = shootDirection;

    if (skill instanceof DamageProjectileSkill dps) {
      this.projectileSkill = dps;
    } else {
      throw new IllegalArgumentException(
          "Skill for PelletLauncher must be a DamageProjectileSkill");
    }

    Point targetEndPoint = this.spawnPoint.translate(this.shootDirection.scale(this.attackRange));
    this.projectileSkill.targetSelection(() -> targetEndPoint.toCenteredPoint());
  }

  @Override
  public void useSkill(Skill skill, Entity skillUser) {
    if (projectileSkill != null) {
      projectileSkill.execute(skillUser);
    }
  }

  @Override
  public Skill skill() {
    return this.projectileSkill;
  }

  @Override
  public void skill(Skill skill) {
    if (skill instanceof DamageProjectileSkill dps) {
      this.projectileSkill = dps;
    } else {
      throw new IllegalArgumentException("Skill must be a DamageProjectileSkill!");
    }
  }

  @Override
  public void accept(Entity entity) {
    launchEnergyPellet(entity);
  }

  private void launchEnergyPellet(Entity entity) {
    if (projectileSkill == null) return;

    String projectileEntityName = uniqueSkillName + "_projectile";
    Entity projectileEntity =
        Game.allEntities()
            .filter(e -> e.name().equals(projectileEntityName))
            .findFirst()
            .orElse(null);
    // only one projectile at the same time
    if (projectileEntity != null) return;

    long now = System.currentTimeMillis();
    if (now - lastAttackTime >= projectileSkill.cooldown()) {
      useSkill(projectileSkill, entity);
      lastAttackTime = now;
    }
  }
}
