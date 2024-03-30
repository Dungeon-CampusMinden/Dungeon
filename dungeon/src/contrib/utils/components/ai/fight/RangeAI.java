package contrib.utils.components.ai.fight;

import static core.level.utils.LevelUtils.accessibleTilesInRange;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implements a fight AI. The entity attacks the player if he is in a given maximum and minimum
 * range. When the entity is not in range but in fight mode, the entity will be moving to within
 * this range.
 */
public final class RangeAI implements Consumer<Entity>, ISkillUser {

  private final float attackRange;
  private final float distance;
  private Skill skill;
  private GraphPath<Tile> path;

  /**
   * Attacks the player if he is within the given range between attackRange and distance. Otherwise,
   * it will move into that range.
   *
   * @param attackRange Maximal distance to hero in which the attack skill should be executed.
   * @param distance Minimal distance to hero in which the attack skill should be executed.
   * @param skill Skill to be used when an attack is performed.
   */
  public RangeAI(final float attackRange, final float distance, final Skill skill) {
    if (attackRange <= distance || distance < 0) {
      throw new IllegalArgumentException(
          "attackRange must be greater than distance and distance must be 0 or greater than 0");
    }
    this.attackRange = attackRange;
    this.distance = distance;
    this.skill = skill;
  }

  @Override
  public void accept(final Entity entity) {
    boolean playerInDistanceRange = LevelUtils.playerInRange(entity, distance);
    boolean playerInAttackRange = LevelUtils.playerInRange(entity, attackRange);

    if (playerInAttackRange) {
      if (playerInDistanceRange) {
        Point positionHero = Game.positionOf(Game.hero().orElseThrow());
        Point positionEntity = Game.positionOf(entity);
        List<Tile> tiles = accessibleTilesInRange(positionEntity, attackRange - distance);
        boolean newPositionFound = false;
        for (Tile tile : tiles) {
          Point newPosition = tile.position();
          if (!Point.inRange(newPosition, positionHero, distance)) {
            path = LevelUtils.calculatePath(positionEntity, newPosition);
            newPositionFound = true;
            break;
          }
        }
        if (!newPositionFound) {
          path = LevelUtils.calculatePathToRandomTileInRange(entity, 2 * attackRange);
        }
        AIUtils.move(entity, path);
      } else {
        this.useSkill(this.skill, entity);
      }
    } else {
      path = LevelUtils.calculatePathToHero(entity);
      AIUtils.move(entity, path);
    }
  }

  @Override
  public void useSkill(Skill skill, Entity skillUser) {
    if (skill == null) {
      return;
    }
    skill.execute(skillUser);
  }

  @Override
  public Skill getSkill() {
    return this.skill;
  }

  @Override
  public void setSkill(Skill skill) {
    this.skill = skill;
  }
}
