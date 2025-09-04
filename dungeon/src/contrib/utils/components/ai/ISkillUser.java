package contrib.utils.components.ai;

import contrib.utils.components.ai.fight.AIMeleeBehaviour;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import contrib.utils.components.skill.Skill;
import core.Entity;

/**
 * Interface for skill users. It defines methods to use a skill and to get and set a skill.
 *
 * <p>It is used by the {@link AIMeleeBehaviour AIMeleeBehaviour} and {@link AIRangeBehaviour
 * AIRangeBehaviour} classes to use a skill when an attack is performed.
 */
public interface ISkillUser {

  /**
   * Uses the skill of the skill user.
   *
   * @param skill The skill to be used.
   * @param skillUser The entity that uses the skill.
   */
  void useSkill(Skill skill, Entity skillUser);

  /**
   * Gets the skill of the skill user.
   *
   * @return The skill of the skill user.
   */
  Skill skill();

  /**
   * Sets the skill of the skill user.
   *
   * @param skill The skill to be set.
   */
  void skill(Skill skill);
}
