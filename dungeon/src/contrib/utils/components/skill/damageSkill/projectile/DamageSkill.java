package contrib.utils.components.skill.damageSkill.projectile;

import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.utils.Direction;

public abstract class DamageSkill extends Skill {
  private int damageAmount;
  private DamageType damageType;

  public DamageSkill(String name, long cooldown, int damageAmount, DamageType damageType) {
    super(name, cooldown);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
  }

  public int damageAmount() {
    return damageAmount;
  }

  public DamageType damageType() {
    return damageType;
  }

  protected Damage calculateDamage(Entity caster, Entity target, Direction direction) {
    // This is the place where we could implement stat checks like susceptibility to fire
    return new Damage(damageAmount, damageType, caster);
  }

  public void damageAmount(int i) {
    this.damageAmount = i;
  }

  public void damageType(DamageType type) {
    this.damageType = type;
  }
}
