package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.function.Consumer;

/**
 * A fight behavior for a patrolling "sentry" entity that patrols back and forth between two points
 * (A and B) and attacks when the player is within a given range.
 */
public class SentryFightBehaviour implements Consumer<Entity>, ISkillUser {
  private final Point pointA;
  private final Point pointB;
  private final boolean canEnterWalls;
  private boolean toB = true; // true = moving towards B, false = towards A
  private GraphPath<Tile> currentPath;

  private final float attackRange;
  private DamageProjectileSkill fightSkill;
  private final Direction shootDirection;
  private long lastAttackTime = 0;

  /**
   * Creates a new {@code SentryFightBehaviour}.
   *
   * @param pointA the first patrol point.
   * @param pointB the second patrol point.
   * @param attackRange the maximum range within which the sentry can attack the player.
   * @param fightSkill the skill the sentry uses for attacks.
   * @param shootDirection the fixed direction in which the sentry fires its attacks.
   * @param canEnterWalls whether the sentry can move inside walls.
   */
  public SentryFightBehaviour(
      Point pointA,
      Point pointB,
      float attackRange,
      Skill fightSkill,
      Direction shootDirection,
      boolean canEnterWalls) {
    this.pointA = pointA;
    this.pointB = pointB;
    this.attackRange = attackRange;
    this.shootDirection = shootDirection;
    this.canEnterWalls = canEnterWalls;
    if (fightSkill instanceof DamageProjectileSkill dps) {
      this.fightSkill = dps;
    } else {
      throw new IllegalArgumentException(
          "Skill for SentryFightBehaviour must be a DamageProjectileSkill!");
    }
  }

  @Override
  public void accept(Entity entity) {
    PositionComponent entityPosComp =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    // patrol
    if (currentPath != null && !AIUtils.pathFinished(entity, currentPath)) {
      if (AIUtils.pathLeft(entity, currentPath)) {
        pathCalculator(entityPosComp.position(), getTargetPoint());
      }
      AIUtils.followPath(entity, currentPath);
    } else {
      if (currentPath != null && AIUtils.pathFinished(entity, currentPath)) {
        toB = !toB; // change destination
        currentPath = null;
      }
      if (currentPath == null) {
        pathCalculator(entityPosComp.position(), getTargetPoint());
      }
    }

    // set a new targetEndPoint based on the current Position of the entity and the given direction
    Point targetEndPoint = entityPosComp.position().translate(shootDirection.scale(attackRange));
    fightSkill.targetSelection(() -> targetEndPoint);

    // attack if hero is in range
    tryAttack(entity);
  }

  private Point getTargetPoint() {
    return toB ? pointB : pointA;
  }

  private void tryAttack(Entity entity) {
    if (fightSkill == null) return;

    if (LevelUtils.playerInRange(entity, attackRange)) {
      long now = System.currentTimeMillis();
      if (now - lastAttackTime >= fightSkill.cooldown()) {
        useSkill(fightSkill, entity);
        lastAttackTime = now;
      }
    }
  }

  private void pathCalculator(Point from, Point to) {
    if (canEnterWalls) {
      currentPath = LevelUtils.calculatePathInsideWall(from, to);
    } else {
      currentPath = LevelUtils.calculatePath(from, to);
    }
  }

  @Override
  public void useSkill(Skill fightSkill, Entity skillUser) {
    if (fightSkill != null) {
      fightSkill.execute(skillUser);
    }
  }

  @Override
  public Skill skill() {
    return this.fightSkill;
  }

  @Override
  public void skill(Skill skill) {
    if (skill instanceof DamageProjectileSkill dps) {
      this.fightSkill = dps;
    } else {
      throw new IllegalArgumentException("Skill must be a DamageProjectileSkill!");
    }
  }
}
