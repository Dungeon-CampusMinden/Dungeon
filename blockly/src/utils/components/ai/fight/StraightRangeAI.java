package utils.components.ai.fight;

import contrib.utils.components.skill.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.utils.MissingHeroException;
import java.util.function.Consumer;
import utils.EntityUtils;

/**
 * Attacks the player if he is the view range of the entity. The entity will only shoot in its view
 * direction.
 *
 * <p>This AI is used for ranged entities that can only attack in a straight line.
 */
public class StraightRangeAI implements Consumer<Entity>, ISkillUser {
  private final int range;
  private Skill skill;

  /**
   * Creates a new StraightRangeAI.
   *
   * @param range The view range of the AI.
   * @param skill The skill to be used when an attack is performed.
   */
  public StraightRangeAI(int range, Skill skill) {
    this.range = range;
    this.skill = skill;
  }

  /**
   * Return the current range of the AI.
   *
   * @return The current range of the AI.
   */
  public int range() {
    return range;
  }

  @Override
  public void accept(final Entity entity) {
    boolean playerInRange =
        EntityUtils.canEntitySeeOther(
            entity, Game.hero().orElseThrow(MissingHeroException::new), range);
    if (!playerInRange) return;

    useSkill(skill, entity);
  }

  @Override
  public void useSkill(Skill skill, Entity skillUser) {
    if (skill == null) {
      return;
    }
    skill.execute(skillUser);
  }

  @Override
  public Skill skill() {
    return this.skill;
  }

  @Override
  public void skill(Skill skill) {
    this.skill = skill;
  }
}
