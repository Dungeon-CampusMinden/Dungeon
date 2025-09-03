package entities;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import contrib.entities.AIFactory;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.damageSkill.projectile.DamageProjectileSkill;
import contrib.utils.components.skill.damageSkill.projectile.FireballSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import level.utils.LevelUtils;

/**
 * A utility class for building different boss attack skills. The boss will use different attacks
 * based on its current health percentage.
 *
 * @see level.devlevel.BossLevel BossLevel
 */
public class BossAttackSkills {

  /** Damage for the fire shock wave skill that the boss uses. (default: 1) */
  private static final int FIRE_SHOCKWAVE_DAMAGE = 1;

  /** Damage for the fireball skill that the boss uses. (default: 2) */
  private static final int FIREBALL_DAMAGE = 2;

  /** Speed for the fireball skill that the boss uses. (default: 4.50f) */
  private static final float FIREBALL_SPEED = 4.50f;

  /** Maximum range for the fireball skill that the boss uses. (default: 25f) */
  private static final float FIREBALL_MAX_RANGE = 25f;

  /**
   * A skill that does nothing.
   *
   * <p>Use this skill when the boss should not use any skill.
   *
   * @return The skill that does nothing.
   */
  public static Skill SKILL_NONE() {
    return Skill.NONE;
  }

  /**
   * Shoots a fire wall (made of fireballs) towards the hero.
   *
   * @param wallWidth The width of the wall. The wall will be centered on the boss.
   * @return The skill that shoots the fire wall.
   */
  public static OldSkill fireWall(int wallWidth) {
    return new OldSkill(
        (skillUser) -> {
          // Firewall
          Point heroPos = SkillTools.heroPositionAsPoint();
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Vector2 direction = heroPos.vectorTo(bossPos).normalize();
          // Main shoot is directly at the hero
          // every other fireball is offset left and right of the main shoot
          Vector2 right = direction.rotateDeg(90);
          Vector2 left = direction.rotateDeg(-90);
          for (int i = -wallWidth / 2; i < wallWidth / 2; i++) {
            if (i == 0) {
              launchFireBall(bossPos, heroPos, bossPos, skillUser);
            } else {
              launchFireBall(
                  bossPos.translate(right.scale(i)),
                  heroPos.translate(right.scale(i)),
                  bossPos,
                  skillUser);
              launchFireBall(
                  bossPos.translate(left.scale(i)),
                  heroPos.translate(left.scale(i)),
                  bossPos,
                  skillUser);
            }
          }
        },
        AIFactory.FIREBALL_COOL_DOWN * 3);
  }

  /**
   * Starts a shock wave from the boss. The shock wave is a circular explosion of fireballs.
   *
   * @param radius The radius of the shock wave.
   * @return The skill that starts the shock wave.
   * @see LevelUtils#explosionAt(Coordinate, int, long, java.util.function.Consumer) explosionAt
   */
  public static OldSkill fireShockWave(int radius) {
    return new OldSkill(
        (skillUser) -> {
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Tile bossTile = Game.tileAt(bossPos).orElse(null);
          if (bossTile == null) {
            return;
          }
          List<Coordinate> placedPositions = new ArrayList<>();
          LevelUtils.explosionAt(
              bossTile.coordinate(),
              radius,
              250L,
              (tile -> {
                if (tile == null
                    || tile.levelElement() == LevelElement.WALL
                    || tile.coordinate().equals(bossTile.coordinate())
                    || placedPositions.contains(tile.coordinate())) {
                  return;
                }
                placedPositions.add(tile.coordinate());

                Entity entity = new Entity("fire");
                PositionComponent posComp =
                    new PositionComponent(tile.coordinate().toCenteredPoint());
                entity.add(posComp);
                entity.add(new CollideComponent());
                try {
                  DrawComponent drawComp = new DrawComponent(new SimpleIPath("skills/fireball"));
                  drawComp.currentAnimation("run_down");
                  entity.add(drawComp);
                } catch (IOException e) {
                  throw new RuntimeException("Could not load fireball texture" + e);
                }
                entity.add(
                    new SpikyComponent(
                        FIRE_SHOCKWAVE_DAMAGE, DamageType.FIRE, Game.frameRate() / 4));
                Game.add(entity);

                EventScheduler.scheduleAction(() -> Game.remove(entity), 2000);
              }));
        },
        10 * 1000);
  }

  /**
   * Shoots a fire cone towards the hero. The fire cone consists of six fireballs.
   *
   * <ul>
   *   <li>One fireball directly at the hero.
   *   <li>Two fireballs to the left and right of the hero. (X degrees)
   *   <li>One delayed fireball directly at the hero. With updated hero position.
   *   <li>Two delayed fireballs left and right offset to that previous fireball. (X-5 degrees)
   * </ul>
   *
   * @param degree The degree of the fire cone.
   * @param delayMillis The delay between the first and second round of fireballs.
   * @param fireballSpeed The speed of the fireballs.
   * @param fireballDamage The damage of the fireballs.
   * @return The skill that shoots the fire cone.
   */
  public static OldSkill fireCone(
      int degree, int delayMillis, float fireballSpeed, int fireballDamage) {
    return new OldSkill(
        (skillUser) -> {
          Point heroPos = EntityUtils.getHeroPosition();
          if (heroPos == null) {
            return;
          }
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Vector2 direction = bossPos.vectorTo(heroPos).normalize();

          // Function to calculate the fireball target position
          Function<Integer, Point> calculateFireballTarget =
              (angle) -> {
                Vector2 offset =
                    direction.rotateDeg(angle).scale(bossPos.vectorTo(heroPos).length());
                return bossPos.translate(offset);
              };

          Consumer<Integer> launchFireBallWithDegree =
              (degreeValue) ->
                  launchFireBall(
                      bossPos,
                      calculateFireballTarget.apply(degreeValue),
                      bossPos,
                      skillUser,
                      FIREBALL_MAX_RANGE,
                      fireballSpeed,
                      fireballDamage);

          // Launch fireballs
          launchFireBallWithDegree.accept(degree);
          launchFireBallWithDegree.accept(-degree);
          launchFireBallWithDegree.accept(0);

          // Schedule another round of fireballs
          EventScheduler.scheduleAction(
              () -> {
                launchFireBallWithDegree.accept(degree - 5);
                launchFireBallWithDegree.accept(-(degree - 5));
                launchFireBallWithDegree.accept(0);
              },
              delayMillis);
        },
        AIFactory.FIREBALL_COOL_DOWN * 2);
  }

  /**
   * Launches a fireball in every direction around the boss. Sort of like a fire spin attack.
   *
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   * @return The skill that shoots the fireballs.
   */
  public static OldSkill fireStorm(int totalFireBalls, int delayBetweenFireballs) {
    return new OldSkill(
        (skillUser) -> {
          // Fire Storm
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();

          for (int i = 0; i < totalFireBalls; i++) {
            final int degree = i * 360 / totalFireBalls;
            EventScheduler.scheduleAction(
                () -> {
                  Vector2 direction = Direction.UP.rotateDeg(degree);
                  Point target = bossPos.translate(direction.scale(FIREBALL_MAX_RANGE * 0.5f));
                  launchFireBall(bossPos, target, bossPos, skillUser);
                },
                (long) i * delayBetweenFireballs);
          }
        },
        AIFactory.FIREBALL_COOL_DOWN * 4);
  }

  /**
   * Launches a fireball from the start position to the target position. If the start position is
   * the same as the boss position, the fireball will be launched from the boss. Otherwise, a
   * temporary entity will be created to launch the fireball.
   *
   * @param start The start position of the fireball.
   * @param target The target position of the fireball.
   * @param bossPos The position of the boss.
   * @param skillUser The entity that uses the skill.
   */
  private static void launchFireBall(Point start, Point target, Point bossPos, Entity skillUser) {
    BossAttackSkills.launchFireBall(
        start, target, bossPos, skillUser, FIREBALL_MAX_RANGE, FIREBALL_SPEED, FIREBALL_DAMAGE);
  }

  /**
   * Launches a fireball from the start position to the target position. If the start position is
   * the same as the boss position, the fireball will be launched from the boss. Otherwise, a
   * temporary entity will be created to launch the fireball.
   *
   * @param start The start position of the fireball.
   * @param target The target position of the fireball.
   * @param bossPos The position of the boss.
   * @param skillUser The entity that uses the skill.
   * @param maxRange The maximum range of the fireball.
   * @param speed The speed of the fireball.
   * @param damage The damage of the fireball.
   */
  private static void launchFireBall(
      Point start,
      Point target,
      Point bossPos,
      Entity skillUser,
      float maxRange,
      float speed,
      int damage) {
    Entity shooter;
    DamageProjectileSkill skill = new FireballSkill(() -> target, maxRange, speed, damage);
    skill.ignoreEntity(skillUser);
    if (start.equals(bossPos)) {
      shooter = skillUser;
    } else {
      shooter = new Entity("Fireball Shooter");
      shooter.add(new PositionComponent(start));
      shooter.add(new CollideComponent());
    }

    skill.execute(shooter);
  }

  /**
   * This method returns the skill that the final boss should use based on the current state of the
   * boss fight. The boss will use different attacks based on its current health percentage.
   *
   * <p>The boss will use different attacks based on its current health percentage.
   *
   * <ul>
   *   <li>75% - 100% health: 90% chance to use normal attack, 10% chance to use fire cone
   *   <li>50% - 75% health: 80% chance to use fire wall, 10% chance to use fire storm, 10% chance
   *       to use fire cone
   *   <li>0% - 50% health: 70% chance to use fire wall, 10% chance to use fire storm, 20% chance to
   *       use fire shock wave
   * </ul>
   *
   * @param boss The boss entity. That should use the skill.
   * @return The skill that the final boss should use.
   * @see level.devlevel.BossLevel BossLevel
   */
  public static OldSkill getFinalBossSkill(Entity boss) {
    double healthPercentage = calculateBossHealthPercentage(boss);

    if (healthPercentage > 75) { // High health - more simple attacks
      return (getBossAttackChance(boss))
          ? fireCone(40, 125, BossAttackSkills.FIREBALL_SPEED + 1, FIREBALL_DAMAGE + 1)
          : fireWall(3);
    } else if (healthPercentage > 50) { // Medium health - similar but more difficult attacks
      return (getBossAttackChance(boss))
          ? fireCone(35, 125, BossAttackSkills.FIREBALL_SPEED + 2, FIREBALL_DAMAGE + 1)
          : getBossAttackChance(boss) ? fireWall(5) : fireStorm(12, 120);
    } else {
      // Low health - more defensive and desperate attacks
      return (getBossAttackChance(boss))
          ? fireStorm(24, 95)
          : getBossAttackChance(boss) ? fireShockWave(6) : fireWall(8);
    }
  }

  /**
   * This method returns random boolean. Based on the current Boss health percentage, and the
   * current Stage of the Boss fight
   *
   * @param boss The boss entity.
   * @return random boolean
   */
  private static boolean getBossAttackChance(Entity boss) {
    double healthPercentage = calculateBossHealthPercentage(boss);
    Random random = ILevel.RANDOM;

    if (healthPercentage > 75) {
      return random.nextDouble() < 0.4;
    } else if (healthPercentage > 60) {
      return random.nextDouble() < 0.8;
    } else if (healthPercentage > 50) {
      return random.nextDouble() < 0.5;
    } else if (healthPercentage > 40) {
      return random.nextDouble() < 0.6;
    } else if (healthPercentage > 25) {
      return random.nextDouble() < 0.4;
    } else {
      return random.nextDouble() < 0.2;
    }
  }

  /**
   * Calculates the current health percentage of the boss.
   *
   * @param bossEntity The boss entity.
   * @return The current health percentage of the boss.
   */
  public static double calculateBossHealthPercentage(Entity bossEntity) {
    HealthComponent healthComponent =
        bossEntity
            .fetch(HealthComponent.class)
            .orElseThrow(() -> MissingComponentException.build(bossEntity, HealthComponent.class));
    return (double) healthComponent.currentHealthpoints()
        / healthComponent.maximalHealthpoints()
        * 100;
  }

  /**
   * An enchantment version of a normal attack. Shoots two fireballs at the hero. One directly at
   * the hero and one is trying to predict the hero's movement.
   *
   * @param coolDown The cool down of the skill.
   * @return The skill that shoots the fireballs.
   */
  public static OldSkill normalAttack(int coolDown) {
    return new OldSkill(
        (skillUser) -> {
          Point heroPos = EntityUtils.getHeroPosition();
          if (heroPos == null) {
            return;
          }
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          launchFireBall(bossPos, heroPos, bossPos, skillUser);
          EventScheduler.scheduleAction(
              () -> {
                Point heroPos2 = EntityUtils.getHeroPosition();
                if (heroPos2 == null) {
                  return;
                }
                Vector2 heroDirection = heroPos.vectorTo(heroPos2).normalize();
                heroDirection = heroDirection.scale((float) (bossPos.distance(heroPos)) * 2);
                Point predictedHeroPos = heroPos2.translate(heroDirection);
                launchFireBall(bossPos, predictedHeroPos, bossPos, skillUser);
              },
              50L);
        },
        coolDown);
  }
}
