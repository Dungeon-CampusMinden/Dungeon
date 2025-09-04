package entities;

import contrib.components.HealthComponent;
import contrib.entities.AIFactory;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.*;
import contrib.utils.components.skill.selfSkill.FireShockWaveSkill;
import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A utility class for building different boss attack skills. The boss will use different attacks
 * based on its current health percentage.
 *
 * @see level.devlevel.BossLevel BossLevel
 */
public class BossAttackSkills {

  private static final Supplier<Point> HERO_POSITION = EntityUtils::getHeroPosition;

  /** Damage for the fire shock wave skill that the boss uses. (default: 1) */
  private static final int SHOCKWAVE_DAMAGE = 1;

  /** Damage for the fireball skill that the boss uses. (default: 2) */
  private static final int FIREBALL_DAMAGE = 2;

  /** Speed for the fireball skill that the boss uses. (default: 4.50f) */
  private static final float FIREBALL_SPEED = 4.50f;

  /** Maximum range for the fireball skill that the boss uses. (default: 25f) */
  private static final float FIREBALL_RANGE = 25f;

  /**
   * Cool down for the fireball skill that the boss uses (in milliseconds). (default: {@link
   * AIFactory#FIREBALL_COOL_DOWN})
   */
  private static final long FIREBALL_COOLDOWN = AIFactory.FIREBALL_COOL_DOWN;

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
  public static Skill fireWall(int wallWidth) {
    return new FireballWallSkill(
        HERO_POSITION,
        FIREBALL_COOLDOWN,
        FIREBALL_SPEED,
        FIREBALL_RANGE,
        FIREBALL_DAMAGE,
        wallWidth);
  }

  /**
   * Starts a shock wave from the boss. The shock wave is a circular explosion of fireballs.
   *
   * @param radius The radius of the shock wave.
   * @return The skill that starts the shock wave.
   */
  public static Skill fireShockWave(int radius) {
    return new FireShockWaveSkill(radius, SHOCKWAVE_DAMAGE, radius);
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
    return new FireballConeSkill(
        HERO_POSITION,
        FIREBALL_COOLDOWN,
        fireballSpeed,
        FIREBALL_RANGE,
        fireballDamage,
        degree,
        delayMillis);
  }

  /**
   * Launches a fireball in every direction around the boss. Sort of like a fire spin attack.
   *
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   * @return The skill that shoots the fireballs.
   */
  public static Skill fireStorm(int totalFireBalls, int delayBetweenFireballs) {
    return new FireballStormSkill(
        FIREBALL_COOLDOWN,
        FIREBALL_SPEED,
        FIREBALL_RANGE,
        FIREBALL_DAMAGE,
        totalFireBalls,
        delayBetweenFireballs);
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
  public static Skill getFinalBossSkill(Entity boss) {
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
  public static Skill normalAttack(int coolDown) {
    return new DoubleFireballSkill(
        HERO_POSITION, coolDown, FIREBALL_SPEED, FIREBALL_RANGE, FIREBALL_DAMAGE);
  }
}
