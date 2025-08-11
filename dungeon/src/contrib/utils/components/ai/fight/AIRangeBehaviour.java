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

  private enum Proximity {
    TOO_CLOSE,
    IN_RANGE,
    TOO_FAR
  }

  private final float maxAttackRange;
  private final float minAttackRange;
  private Skill fightSkill;

  /**
   * Attacks the player if he is within the given range between minAttackRange and maxAttackRange.
   * Otherwise, it will move into that range.
   *
   * @param maxAttackRange Maximal distance to hero in which the fightSkill should be executed.
   * @param minAttackRange Minimal distance to hero in which the fightSkill should be executed.
   * @param fightSkill Skill to be used when an attack is performed.
   */
  public AIRangeBehaviour(
      final float maxAttackRange, final float minAttackRange, final Skill fightSkill) {
    if (maxAttackRange <= minAttackRange || minAttackRange < 0) {
      throw new IllegalArgumentException(
          "maxAttackRange must be greater than minAttackRange and minAttackRange must be 0 or greater than 0");
    }
    this.maxAttackRange = maxAttackRange;
    this.minAttackRange = minAttackRange;
    this.fightSkill = fightSkill;
  }

  @Override
  public void accept(final Entity entity) {
    switch (proximity(entity)) {
      case IN_RANGE -> useSkill(fightSkill, entity);
      case TOO_CLOSE -> moveAwayFromHero(entity);
      case TOO_FAR -> moveToHero(entity);
    }
  }

  /**
   * Checks if the entity is in range of the hero.
   *
   * @param entity The entity to check.
   * @param radius The radius within which the entity should be considered in range.
   * @return True if the entity is in range, false otherwise.
   */
  private boolean inRange(final Entity entity, final float radius) {
    return LevelUtils.playerInRange(entity, radius);
  }

  /**
   * Determines the proximity of the entity to the hero.
   *
   * @param entity The entity to check.
   * @return The proximity status of the entity relative to the hero.
   */
  private Proximity proximity(final Entity entity) {
    if (inRange(entity, minAttackRange)) return Proximity.TOO_CLOSE;
    if (inRange(entity, maxAttackRange)) return Proximity.IN_RANGE;
    return Proximity.TOO_FAR;
  }

  /**
   * Moves the entity away from the hero if he is too close.
   *
   * @param entity The entity to move.
   */
  private void moveAwayFromHero(Entity entity) {
    Game.hero()
        .flatMap(Game::positionOf) // Hero-Position
        .flatMap(
            positionHero ->
                Game.positionOf(entity)
                    .map(
                        positionEntity ->
                            findPathToSafety(positionEntity, positionHero)
                                .orElseGet(
                                    () ->
                                        LevelUtils.calculatePathToRandomTileInRange(
                                            entity, 2 * maxAttackRange))))
        .ifPresent(path -> AIUtils.move(entity, path));
  }

  /**
   * Finds a path to a reachable tile within the search range that is outside the minimum attack
   * range from the hero.
   *
   * @param positionEntity The position of the entity.
   * @param positionHero The position of the hero.
   * @return An optional containing the path to safety, or empty if no such path exists.
   */
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

  /**
   * Moves the entity towards the hero if he is too far away.
   *
   * @param entity The entity to move.
   */
  private void moveToHero(Entity entity) {
    GraphPath<Tile> path = LevelUtils.calculatePathToHero(entity);
    AIUtils.move(entity, path);
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
    return this.fightSkill;
  }

  @Override
  public void skill(Skill skill) {
    this.fightSkill = skill;
  }
}
