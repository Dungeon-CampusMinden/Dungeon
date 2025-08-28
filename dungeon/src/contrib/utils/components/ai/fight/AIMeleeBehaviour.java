package contrib.utils.components.ai.fight;

import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.Consumer;

/**
 * Implements a fight AI. The entity attacks the player if he is in a given range. When the entity
 * is not in range but in fight mode, the entity will be moving towards the player.
 *
 * @see ISkillUser
 */
public class AIMeleeBehaviour extends AIChaseBehaviour implements Consumer<Entity>, ISkillUser {
  private final float attackRange;
  private Skill fightSkill;

  /**
   * Attacks the player if he is within the given range. Otherwise, it will move towards the player.
   *
   * @param chaseRange Range in which the entity will chase the player.
   * @param attackRange Range in which the attack skill should be executed.
   * @param fightSkill Skill to be used when an attack is performed.
   */
  public AIMeleeBehaviour(float chaseRange, float attackRange, Skill fightSkill) {
    super(chaseRange);
    this.attackRange = attackRange;
    this.fightSkill = fightSkill;
  }

  @Override
  public void accept(Entity entity) {
    if (LevelUtils.playerInRange(entity, attackRange)) {
      useSkill(fightSkill, entity);
    } else {
      super.accept(entity);
    }
  }

  @Override
  public void useSkill(Skill fightSkill, Entity skillUser) {
    if (fightSkill == null) {
      return;
    }
    fightSkill.execute(skillUser);
  }

  @Override
  public Skill skill() {
    return fightSkill;
  }

  @Override
  public void skill(Skill skill) {
    this.fightSkill = skill;
  }
}
