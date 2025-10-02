package entities;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
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
  }

  /**
   * Creates a {@code BlocklyFireball} skill with a simple cooldown and default parameters.
   *
   * @param target A supplier that provides the point the fireball will be aimed at.
   * @param cooldown Cooldown in milliseconds before the skill can be reused.
   */
  public BlocklyFireball(Supplier<Point> target, int cooldown) {
    super(target, cooldown);
  }

  @Override
  protected void shootProjectile(Entity caster, Point start, Point aimedOn) {
    Entity projectile = new Entity(name() + "_projectile");
    ignoreEntities.add(caster);
    ignoreEntities.add(projectile);

    projectile.add(new FlyComponent());
    DrawComponent dc = new DrawComponent(texture);

    dc.tintColor(tintColor);
    projectile.add(dc);

    // Target point calculation
    Point targetPoint = SkillTools.calculateLastPositionInRange(start, aimedOn, range);

    Point position = start.translate(hitBoxSize.scale(-0.5)); // +offset
    PositionComponent pc = new PositionComponent(position);
    projectile.add(pc);
    // calculate rotation
    double angleDeg = Vector2.of(position).angleToDeg(Vector2.of(targetPoint));
    pc.rotation((float) angleDeg);
    // Calculate velocity
    Vector2 forceToApply = SkillTools.calculateDirection(start, targetPoint).scale(speed);

    // Add components
    VelocityComponent vc = new VelocityComponent(speed, handleProjectileWallHit(caster), true);

    // this is the only difference to a normal fireball
    vc.moveboxSize(hitBoxSize.scale(0.1));
    vc.moveboxOffset(hitBoxOffset);
    projectile.add(vc);
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
        new CollideComponent(
            hitBoxOffset, hitBoxSize, onCollideEnter(caster), onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    cc.isSolid(false);
    projectile.add(cc);

    Game.add(projectile);
    onSpawn(caster, projectile);
  }
}
