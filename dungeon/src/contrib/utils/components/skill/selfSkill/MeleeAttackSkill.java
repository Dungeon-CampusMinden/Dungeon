package contrib.utils.components.skill.selfSkill;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.systems.AttachmentSystem;
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
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * A skill that does a melee attack in the direction the entity is looking, generates a short living
 * entity that deals damage to all entities colliding with it.
 */
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
   * @param damage The damage the melee attack deals.
   * @param damageType Typ of damage the attack deals.
   * @param cooldown How frequent this attack can be used.
   * @param offset Offset for the collision hitbox.
   * @param hitbox Hitbox for the collision.
   * @param resources The resources and their required amounts, provided as {@link Tuple}s
   */
  public MeleeAttackSkill(
      int damage,
      DamageType damageType,
      long cooldown,
      Vector2 offset,
      Vector2 hitbox,
      Tuple<Resource, Integer>... resources) {
    super(NAME, cooldown, resources);
    this.damage = damage;
    this.damageType = damageType;
    this.offset = offset;
    this.hitbox = hitbox;
  }

  public MeleeAttackSkill(int damage) {
      this(damage,DamageType.PHYSICAL,500, Vector2.ZERO, Vector2.of(1.5,1));
  }

  /**
   * Creates a short livid entity in the direction which the caster is looking at. The damage is
   * dealt via the collide component which uses the onCollideEnter to deal the damage. The damage
   * takes the damagetyp into account. Removes itself after {@link #cooldown} milliseconds from the
   * game.
   *
   * @param caster The entity using the skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    caster
        .fetch(PositionComponent.class)
        .ifPresent(
            casterPositionComponent -> {
              Entity attack = new Entity();


              DrawComponent drawComponent = new DrawComponent(TEXTURE);
              attack.add(drawComponent);

              Point attackPosition =
                  casterPositionComponent
                      .position()
                      .translate(casterPositionComponent.viewDirection());
              PositionComponent attackPositionComponent = new PositionComponent(attackPosition);
              attackPositionComponent.rotation(casterPositionComponent.rotation());

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
              AttachmentComponent ac = new AttachmentComponent(attackPositionComponent, casterPositionComponent);
              ac.setRotatingWithOrigin(true);
              attack.add(ac);

              CollideComponent collideComponent =
                  new CollideComponent(
                      offset,
                      hitbox,
                      (entity, other, direction) -> {
                        other
                            .fetch(HealthComponent.class)
                            .ifPresent(
                                healthComponent -> {
                                  healthComponent.receiveHit(
                                      new Damage(damage, damageType, attack));
                                });
                      },
                      CollideComponent.DEFAULT_COLLIDER);
              collideComponent.isSolid(false);
              attack.add(collideComponent);

              Game.add(attack);
              EventScheduler.scheduleAction(
                  () -> {
                    Game.remove(attack);
                  },
                  2500);
            });
  }
}
