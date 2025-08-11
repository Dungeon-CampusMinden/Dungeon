package contrib.utils.components.ai.fight;

import static core.level.utils.LevelUtils.accessibleTilesInRange;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.skill.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implements a fight AI. The entity attacks the player if he is in a given maximum and minimum
 * range. When the entity is not in range but in fight mode, the entity will be moving to within
 * this range.
 *
 * @see ISkillUser
 */
public class AIRangeBehaviour implements Consumer<Entity>, ISkillUser {

  private enum Proximity { TOO_CLOSE, IN_RANGE, TOO_FAR }
  private final float maxAttackRange;
  private final float minAttackRange;
  private Skill skill;

  /**
   * Attacks the player if he is within the given range between minAttackRange and maxAttackRange.
   * Otherwise, it will move into that range.
   *
   * @param maxAttackRange Maximal distance to hero in which the attack skill should be executed.
   * @param minAttackRange Minimal distance to hero in which the attack skill should be executed.
   * @param skill Skill to be used when an attack is performed.
   */
  public AIRangeBehaviour(
      final float maxAttackRange, final float minAttackRange, final Skill skill) {
    if (maxAttackRange <= minAttackRange || minAttackRange < 0) {
      throw new IllegalArgumentException(
          "maxAttackRange must be greater than minAttackRange and minAttackRange must be 0 or greater than 0");
    }
    this.maxAttackRange = maxAttackRange;
    this.minAttackRange = minAttackRange;
    this.skill = skill;
  }

  @Override
  public void accept(final Entity entity) {
    switch (proximity(entity)) {
      case IN_RANGE -> useSkill(skill, entity);
      case TOO_CLOSE -> moveAwayFromHero(entity);
      case TOO_FAR -> moveToHero(entity);
    }
  }

  private boolean inRange(final Entity entity, final float radius) {
    return LevelUtils.playerInRange(entity, radius);
  }

  private Proximity proximity(final Entity entity) {
    if (inRange(entity, minAttackRange)) return Proximity.TOO_CLOSE;
    if (inRange(entity, maxAttackRange)) return Proximity.IN_RANGE;
    return Proximity.TOO_FAR;
  }

  private void moveAwayFromHero(Entity entity) {
    Game.hero()
      .flatMap(Game::positionOf)
      .ifPresent(positionHero ->
        Game.positionOf(entity).ifPresent(positionEntity -> {
          GraphPath<Tile> path =
            findPathToSafety(positionEntity, positionHero)
              .orElseGet(() -> LevelUtils.calculatePathToRandomTileInRange(entity, 2 * maxAttackRange));
          AIUtils.move(entity, path);
        })
      );
  }

  private Optional<GraphPath<Tile>> findPathToSafety(Point positionEntity, Point positionHero) {
    List<Tile> tiles = accessibleTilesInRange(positionEntity, maxAttackRange - minAttackRange);
    for (Tile t : tiles) {
      Point p = t.position();
      if (!Point.inRange(p, positionHero, minAttackRange)) {
        return Optional.of(LevelUtils.calculatePath(positionEntity, p));
      }
    }
    return Optional.empty();
  }

  private void moveToHero(Entity entity) {
    GraphPath<Tile> path = LevelUtils.calculatePathToHero(entity);
    AIUtils.move(entity, path);
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
