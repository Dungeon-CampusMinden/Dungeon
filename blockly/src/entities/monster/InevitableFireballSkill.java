package entities.monster;

import client.Client;
import coderunner.BlocklyCommands;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;

/**
 * Subclass of {@link FireballSkill}.
 *
 * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the intended
 * target will get frozen in place and the projectile will fly towards the target. The target will
 * not be able to move or dodge the projectile.
 */
public class InevitableFireballSkill extends FireballSkill {

  private static Supplier<Point> TARGET_HERO =
      () ->
          Game.hero()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              // offset for error with fireball path calculation (#2230)
              .map(point -> point.translate(Vector2.of(0.5f, 0.5f)))
              .orElse(null);

  /** Create a Fireball that will freez and kill the player. */
  public InevitableFireballSkill() {
    super(TARGET_HERO, 500);
    this.damageAmount = 9999;
    this.range = 9999;
  }

  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    // Set the velocity to zero to freeze the entity (hero only)
    Game.hero()
        .flatMap(hero -> hero.fetch(VelocityComponent.class))
        .ifPresent(velocityComponent -> velocityComponent.maxSpeed(0));
    // Centers the hero on the tile, so the Blockly step looks completed, and the hero doesn't
    // freeze on the corner of the red zone
    Game.hero()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .ifPresent(
            pc -> {
              pc.position(pc.position().translate(BlocklyCommands.MAGIC_OFFSET));
              pc.toTileCorner();
            });
  }

  protected void additionalEffectAfterDamage(
      Entity caster, Entity projectile, Entity target, Direction direction) {
    // Set the velocity back to the original value (hero only)
    if (!target.isPresent(PlayerComponent.class)) return;
    target
        .fetch(VelocityComponent.class)
        .ifPresent(velocityComponent -> velocityComponent.maxSpeed(Client.MOVEMENT_FORCE.x()));
  }
}
