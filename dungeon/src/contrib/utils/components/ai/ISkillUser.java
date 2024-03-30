package contrib.utils.components.ai;

import contrib.utils.components.skill.Skill;
import core.Entity;

/** Interface for entity that can use skills. */
public interface ISkillUser {

  /**
   * Uses the given skill on the skill user.
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
  Skill getSkill();

  /**
   * Sets the skill of the skill user.
   *
   * @param skill The skill to be set.
   */
  void setSkill(Skill skill);
}
