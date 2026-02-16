package entities.monster;

import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;
import level.LevelManagementUtils;
import systems.BlocklyCommandExecuteSystem;

/**
 * Subclass of {@link FireballSkill}.
 *
 * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the intended
 * target will get frozen in place and the projectile will fly towards the target. The target will
 * not be able to move or dodge the projectile.
 */
public class InevitableFireballSkill extends FireballSkill {

  // Vector to center the fireball
  private static Vector2 centerFireballOnMonster = Vector2.of(0.5f, 0.5f);

  private static final Supplier<Point> TARGET_PLAYER =
      () -> {
        LevelManagementUtils.centerHero();
        return Game.player()
            .flatMap(hero -> hero.fetch(PositionComponent.class))
            .map(PositionComponent::position)
            // translate the fireball to the center of the monster
            // by default it is throwing the fireball to the lower left corner
            .map(point -> point.translate(centerFireballOnMonster))
            .orElse(null);
      };

  /** Create a Fireball that will stop blockly-code execution on spawn. */
  public InevitableFireballSkill() {
    super(TARGET_PLAYER, 500);
    this.damageAmount = 9999;
    this.range = 9999;
  }

  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    Game.system(BlocklyCommandExecuteSystem.class, System::stop);
  }
}
