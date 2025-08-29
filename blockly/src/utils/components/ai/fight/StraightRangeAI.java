package utils.components.ai.fight;

import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.utils.MissingHeroException;
import java.util.function.Consumer;
import utils.BlocklyCommands;
import utils.EntityUtils;

/**
 * Attacks the player if he is the view range of the entity. The entity will only shoot in its view
 * direction.
 *
 * <p>This AI is used for ranged entities that can only attack in a straight line.
 */
public class StraightRangeAI implements Consumer<Entity>, ISkillUser {
  private int range;
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

  /**
   * Set the range of the AI.
   *
   * @param range The new range of the AI.
   */
  public void range(int range) {
    this.range = range;
  }

  @Override
  public void accept(final Entity entity) {
    if (BlocklyCommands.DISABLE_SHOOT_ON_HERO) return;
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
