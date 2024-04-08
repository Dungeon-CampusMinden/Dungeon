package entities;

import com.badlogic.gdx.math.Vector2;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import contrib.entities.AIFactory;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.DamageProjectile;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import level.utils.LevelUtils;
import systems.EventScheduler;
import utils.EntityUtils;

public class BossAttackSkills {

  public static final int FIRE_SHOCKWAVE_DAMAGE = 1;
  public static final int FIREBALL_DAMAGE = 2;
  public static final float FIREBALL_SPEED = 5.00f;
  public static final float FIREBALL_MAX_RANGE = 25f;

  /** A skill that does nothing. */
  public static Skill SKILL_NONE() {
    return new Skill((skillUser) -> {}, 1000);
  }

  /**
   * Shoots a fire wall (made of fireballs) towards the hero.
   *
   * @param wallWidth The width of the wall. The wall will be centered on the boss.
   * @return The skill that shoots the fire wall.
   */
  public static Skill fireWall(int wallWidth) {
    return new Skill(
        (skillUser) -> {
          // Firewall
          Point heroPos = SkillTools.heroPositionAsPoint();
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Vector2 direction = new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y);
          // Main shoot is directly at the hero
          // every other fireball is offset left and right of the main shoot
          Vector2 right = new Vector2(direction).rotateDeg(90).nor();
          Vector2 left = new Vector2(direction).rotateDeg(-90).nor();
          for (int i = -wallWidth / 2; i < wallWidth / 2; i++) {
            if (i == 0) {
              launchFireBall(bossPos, heroPos, bossPos, skillUser);
            } else {
              launchFireBall(
                  new Point(bossPos.x + right.x * i, bossPos.y + right.y * i),
                  new Point(heroPos.x + right.x * i, heroPos.y + right.y * i),
                  bossPos,
                  skillUser);
              launchFireBall(
                  new Point(bossPos.x + left.x * i, bossPos.y + left.y * i),
                  new Point(heroPos.x + left.x * i, heroPos.y + left.y * i),
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
  public static Skill fireShockWave(int radius) {
    return new Skill(
        (skillUser) -> {
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Tile bossTile = Game.currentLevel().tileAt(bossPos);
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

                EventScheduler.getInstance().scheduleAction(() -> Game.remove(entity), 2000);
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
  public static Skill fireCone(
      int degree, int delayMillis, float fireballSpeed, int fireballDamage) {
    return new Skill(
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
          Vector2 direction = new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y).nor();

          // Function to calculate the fireball target position
          Function<Integer, Point> calculateFireballTarget =
              (angle) -> {
                Vector2 offset =
                    new Vector2(direction)
                        .rotateDeg(angle)
                        .scl(new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y).len());
                return new Point(bossPos.x + offset.x, bossPos.y + offset.y);
              };

          Consumer<Integer> launchFireBallWithDegree =
              (degreeValue) -> {
                launchFireBall(
                    bossPos,
                    calculateFireballTarget.apply(degreeValue),
                    bossPos,
                    skillUser,
                    FIREBALL_MAX_RANGE,
                    fireballSpeed,
                    fireballDamage);
              };

          // Launch fireballs
          launchFireBallWithDegree.accept(degree);
          launchFireBallWithDegree.accept(-degree);
          launchFireBallWithDegree.accept(0);

          // Schedule another round of fireballs
          EventScheduler.getInstance()
              .scheduleAction(
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
  public static Skill fireStorm(int totalFireBalls, int delayBetweenFireballs) {
    return new Skill(
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
            EventScheduler.getInstance()
                .scheduleAction(
                    () -> {
                      Point target =
                          new Point(
                              (float) (bossPos.x + Math.cos(Math.toRadians(degree)) * 10),
                              (float) (bossPos.y + Math.sin(Math.toRadians(degree)) * 10));
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
  public static void launchFireBall(Point start, Point target, Point bossPos, Entity skillUser) {
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
  public static void launchFireBall(
      Point start,
      Point target,
      Point bossPos,
      Entity skillUser,
      float maxRange,
      float speed,
      int damage) {
    Entity shooter;
    DamageProjectile skill = new FireballSkill(() -> target, maxRange, speed, damage);
    skill.ignoreEntity(skillUser);
    if (start.equals(bossPos)) {
      shooter = skillUser;
    } else {
      shooter = new Entity("Fireball Shooter");
      shooter.add(new PositionComponent(start));
      shooter.add(new CollideComponent());
    }

    skill.accept(shooter);
    EventScheduler.getInstance().scheduleAction(skill::disposeSounds, 1000);
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
   * @return The skill that the final boss should use.
   * @see level.devlevel.BossLevel BossLevel
   */
  public static Skill getFinalBossSkill() {
    Entity boss =
        Game.entityStream().filter(e -> e.name().contains("Final Boss")).findFirst().orElse(null);
    if (boss == null) {
      return null;
    }
    double healthPercentage = calculateBossHealthPercentage(boss);

    // Example logic for selecting an attack based on the boss's state
    if (healthPercentage > 75) {
      return (getBossAttackChance())
          ? fireCone(40, 125, BossAttackSkills.FIREBALL_SPEED + 1, FIREBALL_DAMAGE + 1)
          : fireWall(3);
    } else if (healthPercentage > 50) {
      return (getBossAttackChance())
          ? fireCone(35, 125, BossAttackSkills.FIREBALL_SPEED + 2, FIREBALL_DAMAGE + 1)
          : getBossAttackChance() ? fireWall(5) : fireStorm(12, 120);
    } else {
      // Low health - more defensive or desperate attacks
      return (getBossAttackChance())
          ? fireStorm(24, 95)
          : getBossAttackChance() ? fireShockWave(6) : fireWall(8);
    }
  }

  /**
   * This method returns random boolean. Based on the current Boss health percentage, and the
   * current Stage of the Boss fight
   *
   * @return random boolean
   */
  public static boolean getBossAttackChance() {
    Entity boss =
        Game.entityStream().filter(e -> e.name().contains("Final Boss")).findFirst().orElse(null);
    if (boss == null) {
      return false;
    }
    double healthPercentage = calculateBossHealthPercentage(boss);
    Random random = Game.currentLevel().RANDOM;

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
      // Low health - more defensive or desperate attacks
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
   * @return The skill that shoots the fireballs.
   */
  public static Skill normalAttack(int coolDown) {
    return new Skill(
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
          EventScheduler.getInstance()
              .scheduleAction(
                  () -> {
                    Point heroPos2 = EntityUtils.getHeroPosition();
                    if (heroPos2 == null) {
                      return;
                    }
                    Vector2 heroDirection =
                        new Vector2(heroPos2.x - heroPos.x, heroPos2.y - heroPos.y).nor();
                    heroDirection.scl((float) (bossPos.distance(heroPos)) * 2);
                    Point predictedHeroPos =
                        new Point(heroPos2.x + heroDirection.x, heroPos2.y + heroDirection.y);
                    launchFireBall(bossPos, predictedHeroPos, bossPos, skillUser);
                  },
                  50L);
        },
        coolDown);
  }
}
