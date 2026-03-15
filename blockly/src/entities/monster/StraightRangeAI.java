package entities.monster;

import client.Client;
import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Vector2;
import entities.EntityUtils;
import java.util.function.Consumer;

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
    if (!Client.SHOOT_AT_PLAYER) {
      return;
    }

    boolean playerInRange =
        Game.player()
            .flatMap(hero -> hero.fetch(PositionComponent.class))
            .map(pc -> pc.position().translate(Vector2.of(0.5f, 0.5f)))
            .map(
                pos -> {
                  Entity dummy = new Entity("dummy");
                  dummy.add(new PositionComponent(pos));
                  return EntityUtils.canEntitySeeOther(entity, dummy, range);
                })
            .orElse(false);

    if (!playerInRange) {
      return;
    }
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
