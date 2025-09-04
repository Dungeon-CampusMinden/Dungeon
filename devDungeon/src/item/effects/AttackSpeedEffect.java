package item.effects;

import contrib.components.SkillComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;

/**
 * This class represents an effect that modifies the attack speed of an entity. The effect is
 * temporary and its duration is specified in seconds. The speed multiplier determines how much
 * faster the entity will attack.
 */
public class AttackSpeedEffect {

  // The multiplier that determines how much faster the entity will attack. (1.5 = 50% faster)
  private final float speedMultiplier;

  // The duration of the effect in seconds.
  private final int duration;

  private long originalFireballCoolDown;

  /**
   * Creates a new AttackSpeedEffect with the specified speed multiplier and duration.
   *
   * @param speedMultiplier The multiplier that determines how much faster the entity will attack.
   *     (1.5 = 50% faster)
   * @param duration The duration of the effect in seconds.
   */
  public AttackSpeedEffect(float speedMultiplier, int duration) {
    this.speedMultiplier = speedMultiplier;
    this.duration = duration;
  }

  /**
   * Applies the attack speed effect to the specified target entity. The effect increases the attack
   * speed of the entity by reducing the cooldown of its fireball skill. The original cooldown is
   * stored and reset after the effect duration ends. If the target entity is not a player, an
   * UnsupportedOperationException is thrown.
   *
   * @param target The entity to apply the effect to.
   * @throws UnsupportedOperationException if the target entity is not a player.
   */
  public void applyAttackSpeed(Entity target) {
    if (target.fetch(PlayerComponent.class).isEmpty()) {
      throw new UnsupportedOperationException(
          "Attack speed can only be applied to player entities.");
    }

    Skill fireball = heroFireballSkill();
    if (fireball != null) {
      this.originalFireballCoolDown = fireball.cooldown();
      fireball.cooldown((long) (originalFireballCoolDown / speedMultiplier));

      EventScheduler.scheduleAction(
          () -> fireball.cooldown(originalFireballCoolDown), duration * 1000L);
    }
  }

  private Skill heroFireballSkill() {
    return Game.hero()
        .flatMap(hero -> hero.fetch(SkillComponent.class))
        .flatMap(sc -> sc.getSkill(FireballSkill.class))
        .orElse(null);
  }
}
