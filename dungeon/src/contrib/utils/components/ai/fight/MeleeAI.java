package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.skill.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;
import java.util.function.Consumer;

/**
 * Implements a fight AI. The entity attacks the player if he is in a given range. When the entity
 * is not in range but in fight mode, the entity will be moving to ward the player.
 *
 * @see ISkillUser
 */
public class MeleeAI implements Consumer<Entity>, ISkillUser {
  private final float attackRange;
  private final int delay = Game.frameRate();
  private Skill fightSkill;
  private int timeSinceLastUpdate = 0;
  private GraphPath<Tile> path;

  /**
   * Attacks the player if he is within the given range. Otherwise, it will move towards the player.
   *
   * @param attackRange Range in which the attack skill should be executed.
   * @param fightSkill Skill to be used when an attack is performed.
   */
  public MeleeAI(final float attackRange, final Skill fightSkill) {
    this.attackRange = attackRange;
    this.fightSkill = fightSkill;
  }

  @Override
  public void accept(final Entity entity) {
    if (LevelUtils.playerInRange(entity, attackRange)) {
      useSkill(fightSkill, entity);
    } else {
      if (path == null || timeSinceLastUpdate >= delay) {
        path = LevelUtils.calculatePathToHero(entity);
        timeSinceLastUpdate = -1;
      }
      timeSinceLastUpdate++;
      AIUtils.move(entity, path);
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
