package contrib.utils.components.skill.selfSkill;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.HealthComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class MeleeAttackSkill extends Skill {

  /** Name of the skill. */
  public static final String NAME = "Meele";
  private static final IPath TEXTURE = new SimpleIPath("skills/melee");
  private int damage;
  private DamageType damageType;
  private Vector2 hitbox;
  private Vector2 offset;

  /**
   * Creates a new skill with the given parameters.
   *
   * @param resources the resources and their required amounts, provided as {@link Tuple}s
   */
  public MeleeAttackSkill(int damage, DamageType  damageType, long cooldown, Vector2 offset, Vector2 hitbox, Tuple<Resource, Integer>... resources) {
    super(NAME, cooldown, resources);
    this.damage = damage;
    this.damageType = damageType;
    this.offset = offset;
    this.hitbox = hitbox;
  }

  @Override
  protected void executeSkill(Entity caster) {
    caster.fetch(PositionComponent.class)
      .ifPresent(casterPositionComponent -> {
        Entity attack = new Entity();

        DrawComponent drawComponent = new DrawComponent(TEXTURE);
        attack.add(drawComponent);

        Point attackPosition = casterPositionComponent.position().translate(casterPositionComponent.viewDirection());
        PositionComponent attackPositionComponent = new PositionComponent(attackPosition);

        switch(casterPositionComponent.viewDirection()) {
          case UP -> {
            attackPositionComponent.rotation(90);
          }
          case RIGHT -> {
            attackPositionComponent.rotation(0);
          }
          case DOWN -> {
            attackPositionComponent.rotation(270);
          }
          case LEFT -> {
            attackPositionComponent.rotation(180);
          }
          default -> {}
        }

        attack.add(attackPositionComponent);

        CollideComponent collideComponent = new CollideComponent(offset, hitbox, this::onCollideEnter, CollideComponent.DEFAULT_COLLIDER);
        collideComponent.isSolid(false);
        attack.add(collideComponent);

        VelocityComponent velocityComponent = caster.fetch(VelocityComponent.class).get();
        velocityComponent.canEnterWalls(true);
        attack.add(velocityComponent);

        Game.add(attack);
        EventScheduler.scheduleAction(() ->{Game.remove(attack);},250);
      });
  }

  public void onCollideEnter(Entity attack, Entity other, Direction direction) {
    other.fetch(HealthComponent.class)
      .ifPresent(healthComponent -> {
        healthComponent.receiveHit(new Damage(damage, damageType, attack));
      });
  }
}
