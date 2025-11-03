package entities.monster;

import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;
import systems.BlocklyCommandExecuteSystem;

/**
 * Subclass of {@link FireballSkill}.
 *
 * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the intended
 * target will get frozen in place and the projectile will fly towards the target. The target will
 * not be able to move or dodge the projectile.
 */
public class InevitableFireballSkill extends FireballSkill {

  private static final Supplier<Point> TARGET_HERO =
      () ->
          Game.player()
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
    Game.system(BlocklyCommandExecuteSystem.class, System::stop);
  }
}
