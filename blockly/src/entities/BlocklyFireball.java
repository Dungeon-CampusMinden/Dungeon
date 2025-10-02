package entities;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.utils.Point;
import core.utils.Tuple;
import java.util.function.Supplier;

/**
 * A fireball projectile with a reduced collision box for level elements.
 *
 * <p>This adjustment makes aiming more forgiving by allowing the fireball to pass closer to
 * obstacles before colliding.
 */
public class BlocklyFireball extends FireballSkill {

  private static final String NAME = "Blockly Fireball";

  /**
   * Creates a new {@code BlocklyFireball} skill with detailed configuration.
   *
   * @param target A supplier that provides the point the fireball will be aimed at.
   * @param cooldown Cooldown in milliseconds before the skill can be reused.
   * @param speed The travel speed of the fireball.
   * @param range The maximum travel distance of the fireball.
   * @param damageAmount Amount of damage dealt on impact.
   * @param ignoreFirstWall Whether the fireball ignores the first wall it collides with.
   * @param resourceCost Resource costs (e.g., mana, stamina) required to use the skill.
   */
  @SafeVarargs
  public BlocklyFireball(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(NAME, target, cooldown, speed, range, damageAmount, ignoreFirstWall, resourceCost);
    this.hitBoxSize = this.hitBoxSize.scale(0.1f);
  }

  /**
   * Creates a {@code BlocklyFireball} skill with a simple cooldown and default parameters.
   *
   * @param target A supplier that provides the point the fireball will be aimed at.
   * @param cooldown Cooldown in milliseconds before the skill can be reused.
   */
  public BlocklyFireball(Supplier<Point> target, int cooldown) {
    super(target, cooldown);
    this.hitBoxSize = this.hitBoxSize.scale(0.1f);
  }
}
